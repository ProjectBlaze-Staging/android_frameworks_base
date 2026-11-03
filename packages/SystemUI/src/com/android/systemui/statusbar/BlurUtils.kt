/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.res.Resources
import android.gui.EarlyWakeupInfo
import android.os.Binder
import android.os.Build
import android.os.SystemProperties
import android.os.Trace
import android.os.Trace.TRACE_TAG_APP
import android.util.IndentingPrintWriter
import android.util.Log
import android.util.MathUtils
import android.view.CrossWindowBlurListeners
import android.view.CrossWindowBlurListeners.CROSS_WINDOW_BLUR_SUPPORTED
import android.view.SurfaceControl
import android.view.SyncRtSurfaceTransactionApplier
import android.view.ViewRootImpl
import androidx.annotation.VisibleForTesting
import com.android.systemui.Dumpable
import com.android.systemui.Flags
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.dump.DumpManager
import com.android.systemui.keyguard.ui.transitions.BlurConfig
import com.android.systemui.res.R
import java.io.PrintWriter
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sin

@SysUISingleton
open class BlurUtils
@Inject
constructor(
    @Main resources: Resources,
    blurConfig: BlurConfig,
    private val crossWindowBlurListeners: CrossWindowBlurListeners,
    dumpManager: DumpManager,
) : Dumpable {
    val minBlurRadius = resources.getDimensionPixelSize(R.dimen.min_window_blur_radius).toFloat()
    val maxBlurRadius =
        if (Flags.notificationShadeBlur()) {
            blurConfig.maxBlurRadiusPx
        } else {
            resources.getDimensionPixelSize(R.dimen.max_window_blur_radius).toFloat()
        }

    private var lastAppliedBlur = 0
    private var lastTargetViewRootImpl: ViewRootImpl? = null
    private var _transactionApplier = SyncRtSurfaceTransactionApplier(null)
    @VisibleForTesting
    open val transactionApplier: SyncRtSurfaceTransactionApplier
        get() = _transactionApplier

    private var earlyWakeupEnabled = false
    private val earlyWakeupInfo = EarlyWakeupInfo()
    private var persistentEarlyWakeupRequired = false

    init {
        dumpManager.registerDumpable(this)
        earlyWakeupInfo.token = Binder()
        earlyWakeupInfo.trace = BlurUtils::class.java.name
    }

    @VisibleForTesting
    open fun createTransaction(): SurfaceControl.Transaction = SurfaceControl.Transaction()

    fun blurRadiusOfRatio(ratio: Float): Float {
        if (ratio == 0f) {
            return 0f
        }
        val style = com.android.internal.penguin.glass.LiquidGlass.getEffectiveStyle(null)
        if (style == com.android.internal.penguin.glass.LiquidGlass.STYLE_MATERIAL3_LITE) {
            return 0f
        }
        val scale =
            if (style == com.android.internal.penguin.glass.LiquidGlass.STYLE_IOS_LIQUID_GLASS) {
                1.25f
            } else {
                0.78f
            }
        val enhancedRatio = applyOneUIBlurCurve(ratio)
        return MathUtils.lerp(minBlurRadius, maxBlurRadius * scale, enhancedRatio)
    }

    private fun applyOneUIBlurCurve(ratio: Float): Float {
        return when {
            ratio < 0.3f -> {
                val t = ratio / 0.3f
                t * t * (3f - 2f * t) * 0.3f
            }
            ratio < 0.7f -> {
                val t = (ratio - 0.3f) / 0.4f
                0.3f + (t.pow(1.8f)) * 0.5f
            }
            else -> {
                val t = (ratio - 0.7f) / 0.3f
                val overshoot = sin(t * Math.PI.toFloat() * 0.5f) * 0.05f
                0.8f + (t * t * (3f - 2f * t) * 0.2f) + overshoot
            }
        }.coerceIn(0f, 1f)
    }

    fun blurRadiusOfRatioForAod(ratio: Float): Float {
        if (ratio == 0f) {
            return 0f
        }
        return MathUtils.lerp(minBlurRadius, maxBlurRadius / 2, ratio)
    }

    fun ratioOfBlurRadius(blur: Float): Float {
        if (blur == 0f) {
            return 0f
        }
        return MathUtils.map(
            minBlurRadius,
            maxBlurRadius,
            0f,
            1f,
            blur,
        )
    }

    /**
     * This method should be called before [applyBlur] so that, if needed, we can set the
     * early-wakeup flag in SurfaceFlinger.
     */
    fun prepareBlur(radius: Int) {
        if (!shouldBlur(radius) || earlyWakeupEnabled) return

        if (lastAppliedBlur == 0 && radius != 0) {
            immediateEarlyWakeupStart(PREPARE_BLUR_TRACE_NAME)
        }
    }

    fun applyBlur(viewRootImpl: ViewRootImpl?, radius: Int, opaque: Boolean, scale: Float = 1.0f) {
        if (viewRootImpl == null || !viewRootImpl.surfaceControl.isValid) {
            return
        }
        try {
            updateTransactionApplier(viewRootImpl)

            if (shouldBlur(radius)) {
                applyLayeredBlur(viewRootImpl, radius, opaque, scale)
                lastAppliedBlur = radius
            } else {
                val builder = SyncRtSurfaceTransactionApplier.SurfaceParams.Builder(viewRootImpl.surfaceControl)
                builder.withOpaque(opaque)
                transactionApplier.scheduleApply(builder.build())
            }
        } catch (t: Throwable) {
            Log.w(TAG, "applyBlur transaction skipped due to surface state", t)
        }
    }

    private fun applyLayeredBlur(viewRootImpl: ViewRootImpl, radius: Int, opaque: Boolean, scale: Float) {
        val builder = SyncRtSurfaceTransactionApplier.SurfaceParams.Builder(viewRootImpl.surfaceControl)

        val enhancedRadius = (radius * 1.2f).toInt()
        builder.withBackgroundBlurRadius(enhancedRadius)

        if (shouldScaleWithTransaction()) {
            builder.withBackgroundBlurScale(scale)
        }

        if (lastAppliedBlur == 0 && radius != 0) {
            Trace.instantForTrack(TRACE_TAG_APP, TRACK_NAME, "notifyRendererForGpuLoadUp")
            viewRootImpl.notifyRendererForGpuLoadUp("applyBlur")

            if (!earlyWakeupEnabled) {
                earlyWakeupStartNextFrame(builder, APPLY_BLUR_TRACE_NAME)
            }
        }

        if (earlyWakeupEnabled && lastAppliedBlur != 0 && radius == 0 && !persistentEarlyWakeupRequired) {
            earlyWakeupEndNextFrame(builder, APPLY_BLUR_TRACE_NAME)
        }

        builder.withOpaque(opaque)
        transactionApplier.scheduleApply(builder.build())
    }

    private fun updateTransactionApplier(viewRootImpl: ViewRootImpl) {
        if (lastTargetViewRootImpl == viewRootImpl) return
        _transactionApplier = SyncRtSurfaceTransactionApplier(viewRootImpl.view)
        lastTargetViewRootImpl = viewRootImpl
    }

    private fun v(verboseLog: String) {
        if (isLoggable) Log.v(TAG, verboseLog)
    }

    @SuppressLint("MissingPermission")
    private fun immediateEarlyWakeupStart(traceName: String) {
        earlyWakeupInfo.trace = traceName
        Trace.asyncTraceForTrackBegin(TRACE_TAG_APP, TRACK_NAME, "immediateEarlyWakeupStart", 0)
        Trace.instantForTrack(TRACE_TAG_APP, TRACK_NAME, "immediateEarlyWakeupStart")
        // Using a sync transaction to switch surfaceflinger work duration immediately before the
        // first frame of non-zero blur is applied. Relying on SyncRtSurfaceTransactionApplier might
        // make this switch happen on the first non-zero blur frame.
        createTransaction().setEarlyWakeupStart(earlyWakeupInfo).apply()
        earlyWakeupEnabled = true
    }

    @SuppressLint("MissingPermission")
    private fun immediateEarlyWakeupEnd(traceName: String) {
        earlyWakeupInfo.trace = traceName
        Trace.asyncTraceForTrackEnd(TRACE_TAG_APP, TRACK_NAME, 0)
        Trace.instantForTrack(TRACE_TAG_APP, TRACK_NAME, "immediateEarlyWakeupEnd")
        createTransaction().setEarlyWakeupEnd(earlyWakeupInfo).apply()
        earlyWakeupEnabled = false
    }

    @SuppressLint("MissingPermission")
    private fun earlyWakeupStartNextFrame(
        builder: SyncRtSurfaceTransactionApplier.SurfaceParams.Builder,
        traceName: String,
    ) {
        v("earlyWakeupStart from $traceName")
        earlyWakeupInfo.trace = traceName
        Trace.asyncTraceForTrackBegin(TRACE_TAG_APP, TRACK_NAME, "earlyWakeupStartNextFrame", 0)
        Trace.instantForTrack(TRACE_TAG_APP, TRACK_NAME, "earlyWakeupStartNextFrame")
        builder.withEarlyWakeupStart(earlyWakeupInfo)
        earlyWakeupEnabled = true
    }

    @SuppressLint("MissingPermission")
    private fun earlyWakeupEndNextFrame(
        builder: SyncRtSurfaceTransactionApplier.SurfaceParams.Builder,
        traceName: String,
    ) {
        v("earlyWakeupEnd from $traceName")
        earlyWakeupInfo.trace = traceName
        Trace.asyncTraceForTrackEnd(TRACE_TAG_APP, TRACK_NAME, 0)
        Trace.instantForTrack(TRACE_TAG_APP, TRACK_NAME, "earlyWakeupEndNextFrame")
        builder.withEarlyWakeupEnd(earlyWakeupInfo)
        earlyWakeupEnabled = false
    }

    private fun shouldBlur(radius: Int): Boolean {
        return supportsBlursOnWindows() ||
            ((Flags.notificationShadeBlur() || Flags.bouncerUiRevamp()) &&
                supportsBlursOnWindowsBase() &&
                lastAppliedBlur > 0 &&
                radius == 0)
    }

    private fun shouldScaleWithTransaction(): Boolean {
        return false // spatialModelAppPushback system flags unsupported in scope
    }

    open fun supportsBlursOnWindows(): Boolean {
        return supportsBlursOnWindowsBase() &&
            crossWindowBlurListeners != null &&
            crossWindowBlurListeners.isCrossWindowBlurEnabled
    }

    private fun supportsBlursOnWindowsBase(): Boolean {
        return CROSS_WINDOW_BLUR_SUPPORTED &&
            ActivityManager.isHighEndGfx() &&
            !SystemProperties.getBoolean("persist.sysui.disableBlur", false)
    }

    override fun dump(pw: PrintWriter, args: Array<out String>) {
        IndentingPrintWriter(pw, "  ").let {
            it.println("BlurUtils (OneUI Enhanced):")
            it.increaseIndent()
            it.println("minBlurRadius: $minBlurRadius")
            it.println("maxBlurRadius: $maxBlurRadius")
            it.println("supportsBlursOnWindows: ${supportsBlursOnWindows()}")
            it.println("CROSS_WINDOW_BLUR_SUPPORTED: $CROSS_WINDOW_BLUR_SUPPORTED")
            it.println("isHighEndGfx: ${ActivityManager.isHighEndGfx()}")
        }
    }

    fun setPersistentEarlyWakeup(persistentWakeup: Boolean, viewRootImpl: ViewRootImpl?) {
        persistentEarlyWakeupRequired = persistentWakeup
        if (viewRootImpl == null || !supportsBlursOnWindows()) return

        if (persistentEarlyWakeupRequired) {
            if (earlyWakeupEnabled) return
            Trace.instantForTrack(
                TRACE_TAG_APP,
                TRACK_NAME,
                "setPersistentEarlyWakeup earlyWakeupStart",
            )
            immediateEarlyWakeupStart(SET_PERSISTENT_EARLY_WAKEUP_TRACE_NAME)
        } else {
            if (!earlyWakeupEnabled) return
            if (lastAppliedBlur > 0) {
                Log.w(TAG, "resetEarlyWakeup invoked when lastAppliedBlur $lastAppliedBlur is non-zero")
            }
            Trace.instantForTrack(
                TRACE_TAG_APP,
                TRACK_NAME,
                "setPersistentEarlyWakeup earlyWakeupEnd",
            )
            immediateEarlyWakeupEnd(SET_PERSISTENT_EARLY_WAKEUP_TRACE_NAME)
        }
    }

    companion object {
        const val TRACK_NAME = "BlurUtils"
        private const val TAG = "BlurUtils"
        private val PREPARE_BLUR_TRACE_NAME = BlurUtils::class.java.name + "::prepareBlur"
        private val APPLY_BLUR_TRACE_NAME = BlurUtils::class.java.name + "::applyBlur"
        private val SET_PERSISTENT_EARLY_WAKEUP_TRACE_NAME =
            BlurUtils::class.java.name + "::setPersistentEarlyWakeup"
        private val isLoggable = Log.isLoggable(TAG, Log.VERBOSE) || Build.IS_ENG
    }
}