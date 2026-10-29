/*
 * Copyright (C) 2025 the RisingOS Revived Android Project
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
package com.android.systemui.keyguard.ui.view.layout.sections

import android.content.Context
import android.os.UserHandle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Barrier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.android.systemui.Dependency
import com.android.systemui.clocks.ClockStyle
import com.android.systemui.clocks.KeyguardClockSourceManager
import com.android.systemui.keyguard.domain.interactor.KeyguardBlueprintInteractor
import com.android.systemui.keyguard.shared.model.KeyguardSection
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.keyguard.ui.view.layout.blueprints.transitions.IntraBlueprintTransition
import com.android.systemui.res.R
import com.android.systemui.scene.shared.flag.SceneContainerFlag
import com.android.systemui.tuner.TunerService
import com.android.systemui.util.Utils
import com.android.systemui.util.settings.SecureSettings
import dagger.Lazy
import javax.inject.Inject

@SysUISingleton
class KeyguardClockStyleSection
@Inject
constructor(
    private val context: Context,
    private val secureSettings: SecureSettings,
    private val blueprintInteractor: Lazy<KeyguardBlueprintInteractor>,
) : KeyguardSection(), TunerService.Tunable {
    
    private var clockStyleView: ClockStyle? = null
    private var isTunableRegistered: Boolean = false

    private val TAG = "KeyguardClockStyleSection"
    
    override fun onTuningChanged(key: String?, newValue: String?) {
        if (key == ClockStyle.CLOCK_STYLE_KEY || key == ClockStyle.CLOCK_SOURCE_KEY) {
            val isCustomActive = KeyguardClockSourceManager.isCustomClockActive(context.contentResolver)
            clockStyleView?.visibility = if (isCustomActive) View.VISIBLE else View.GONE
            clockStyleView?.alpha = if (isCustomActive) 1f else 0f
            if (isCustomActive) {
                clockStyleView?.onTimeChanged()
            }
            try {
                blueprintInteractor.get().refreshBlueprint(IntraBlueprintTransition.Type.DefaultTransition)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh blueprint on clock style change", e)
            }
        }
    }

    override fun addViews(constraintLayout: ConstraintLayout) {
        if (!isTunableRegistered) {
            Dependency.get(TunerService::class.java).addTunable(
                this,
                ClockStyle.CLOCK_STYLE_KEY,
                ClockStyle.CLOCK_SOURCE_KEY,
            )
            isTunableRegistered = true
        }

        // Defensively remove any existing clock_ls or ClockStyle views
        for (i in constraintLayout.childCount - 1 downTo 0) {
            val child = constraintLayout.getChildAt(i)
            if (child.id == R.id.clock_ls || child is ClockStyle) {
                constraintLayout.removeViewAt(i)
            }
        }
        
        val isCustomActive = KeyguardClockSourceManager.isCustomClockActive(context.contentResolver)

        val inflater = android.view.LayoutInflater.from(context)
        clockStyleView = inflater.inflate(R.layout.keyguard_clock_style, null) as ClockStyle
        clockStyleView?.apply {
            id = R.id.clock_ls
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            visibility = if (isCustomActive) View.VISIBLE else View.GONE
            alpha = if (isCustomActive) 1f else 0f
        }
        
        clockStyleView?.let { constraintLayout.addView(it) }
    }
    
    override fun bindData(constraintLayout: ConstraintLayout) {
        clockStyleView?.let { clockView ->
            clockView.onTimeChanged()
            clockView.requestLayout()
        }
    }
    
    override fun applyConstraints(constraintSet: ConstraintSet) {
        val isCustomActive = KeyguardClockSourceManager.isCustomClockActive(context.contentResolver)

        constraintSet.apply {
            // Clock positioning - TOP of hierarchy
            connect(R.id.clock_ls, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(R.id.clock_ls, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

            val statusBarHeaderHeight = Utils.getStatusBarHeaderHeightKeyguard(context)
            if (SceneContainerFlag.isEnabled && constraintSet.getConstraint(R.id.keyguard_header) != null) {
                connect(R.id.clock_ls, ConstraintSet.TOP, R.id.keyguard_header, ConstraintSet.BOTTOM)
            } else {
                connect(R.id.clock_ls, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, statusBarHeaderHeight)
            }

            constrainHeight(R.id.clock_ls, ConstraintSet.WRAP_CONTENT)
            constrainWidth(R.id.clock_ls, ConstraintSet.MATCH_CONSTRAINT)
            setMargin(R.id.clock_ls, ConstraintSet.START, 0)
            setMargin(R.id.clock_ls, ConstraintSet.END, 0)
            setElevation(R.id.clock_ls, 1f)
            setVisibility(R.id.clock_ls, if (isCustomActive) ConstraintSet.VISIBLE else ConstraintSet.GONE)
            setAlpha(R.id.clock_ls, if (isCustomActive) 1f else 0f)

            if (isCustomActive) {
                createBarrier(
                    R.id.smart_space_barrier_bottom,
                    Barrier.BOTTOM,
                    0,
                    *intArrayOf(R.id.clock_ls),
                )
                createBarrier(
                    R.id.smart_space_barrier_top,
                    Barrier.TOP,
                    0,
                    *intArrayOf(R.id.clock_ls),
                )
            }
        }
    }
    
    override fun removeViews(constraintLayout: ConstraintLayout) {
        if (isTunableRegistered) {
            Dependency.get(TunerService::class.java).removeTunable(this)
            isTunableRegistered = false
        }
        for (i in constraintLayout.childCount - 1 downTo 0) {
            val child = constraintLayout.getChildAt(i)
            if (child.id == R.id.clock_ls || child is ClockStyle) {
                constraintLayout.removeViewAt(i)
            }
        }
        clockStyleView = null
    }
}
