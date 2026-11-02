/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.biometrics

import android.content.Context
import android.os.RemoteException
import android.os.SystemProperties
import android.os.Trace
import android.provider.Settings
import com.android.systemui.util.concurrency.Execution
import java.io.File

private const val TAG = "UdfpsDisplayMode"

/**
 * UdfpsDisplayMode configures the display for optimal UDFPS operation. For example, sets the
 * display refresh rate that's optimal for UDFPS and triggers multi-vendor Optical FOD HBM nodes.
 */
class UdfpsDisplayMode
constructor(
    private val context: Context,
    private val execution: Execution,
    private val authController: AuthController,
    private val logger: UdfpsLogger
) : UdfpsDisplayModeProvider {

    // The request is reset to null after it's processed.
    private var currentRequest: Request? = null

    private fun writeSysfsSafe(path: String, value: String) {
        try {
            val file = File(path)
            if (file.exists() && file.canWrite()) {
                file.writeText(value)
            }
        } catch (_: Throwable) {}
    }

    private fun applyVendorUdfpsHbm(enabled: Boolean) {
        val vendorMode = Settings.Global.getInt(
            context.contentResolver,
            "blaze_treble_udfps_mode",
            0
        )
        if (vendorMode <= 0) return

        try {
            SystemProperties.set("sys.blaze.udfps_hbm", if (enabled) vendorMode.toString() else "0")
        } catch (_: Throwable) {}

        when (vendorMode) {
            1 -> {
                // Xiaomi / Redmi / POCO (Goodix & FPC Optical FOD Local HBM)
                writeSysfsSafe("/sys/class/drm/card0-DSI-1/disp_param", if (enabled) "0x20000" else "0xE0000")
            }
            2 -> {
                // OnePlus / Oppo / Realme (ColorOS Oplus Display FOD HBM)
                val v = if (enabled) "1" else "0"
                writeSysfsSafe("/sys/kernel/oplus_display/dimlayer_hbm", v)
                writeSysfsSafe("/sys/kernel/oplus_display/notify_fppress", v)
            }
            3 -> {
                // Samsung (Exynos / Snapdragon Optical & Ultrasonic FOD)
                writeSysfsSafe("/sys/class/sec/tsp/cmd", if (enabled) "fod_enable,1,1,0" else "fod_enable,0,0,0")
            }
        }
    }

    override fun enable(onEnabled: Runnable?) {
        execution.isMainThread()
        logger.v(TAG, "enable")

        if (currentRequest != null) {
            logger.e(TAG, "enable | already requested")
            return
        }
        applyVendorUdfpsHbm(true)
        if (authController.udfpsRefreshRateCallback == null) {
            logger.e(TAG, "enable | mDisplayManagerCallback is null")
            onEnabled?.run()
            return
        }

        Trace.beginSection("UdfpsDisplayMode.enable")

        // Track this request in one object.
        val request = Request(context.displayId)
        currentRequest = request

        try {
            // This method is a misnomer. It has nothing to do with HBM, its purpose is to set
            // the appropriate display refresh rate.
            authController.udfpsRefreshRateCallback!!.onRequestEnabled(request.displayId)
            logger.v(TAG, "enable | requested optimal refresh rate for UDFPS")
        } catch (e: RemoteException) {
            logger.e(TAG, "enable", e)
        }

        onEnabled?.run() ?: logger.w(TAG, "enable | onEnabled is null")
        Trace.endSection()
    }

    override fun disable(onDisabled: Runnable?) {
        execution.isMainThread()
        logger.v(TAG, "disable")
        applyVendorUdfpsHbm(false)

        val request = currentRequest
        if (request == null) {
            logger.w(TAG, "disable | already disabled")
            return
        }

        Trace.beginSection("UdfpsDisplayMode.disable")

        try {
            // Allow DisplayManager to unset the UDFPS refresh rate.
            authController.udfpsRefreshRateCallback!!.onRequestDisabled(request.displayId)
            logger.v(TAG, "disable | removed the UDFPS refresh rate request")
        } catch (e: RemoteException) {
            logger.e(TAG, "disable", e)
        }

        currentRequest = null
        onDisabled?.run() ?: logger.w(TAG, "disable | onDisabled is null")
        Trace.endSection()
    }
}

/** Tracks a request to enable the UDFPS mode. */
private data class Request(val displayId: Int)
