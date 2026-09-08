/*
 * Copyright (C) 2022 The Android Open Source Project
 * Copyright (C) 2026 The BlazeAOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.systemui.shared.clocks

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Vibrator
import android.view.LayoutInflater
import com.android.systemui.customization.R
import com.android.systemui.customization.clocks.ClockContextImpl
import com.android.systemui.customization.clocks.ClockLogger
import com.android.systemui.customization.clocks.TimeKeeper
import com.android.systemui.customization.clocks.TimeKeeperImpl
import com.android.systemui.customization.clocks.TypefaceCache
import com.android.systemui.plugins.keyguard.ui.clocks.AxisPresetConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockAxisStyle
import com.android.systemui.plugins.keyguard.ui.clocks.ClockController
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFontAxis.Companion.merge
import com.android.systemui.plugins.keyguard.ui.clocks.ClockMessageBuffers
import com.android.systemui.plugins.keyguard.ui.clocks.ClockMetadata
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPickerConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockProvider
import com.android.systemui.plugins.keyguard.ui.clocks.ClockSettings
import com.android.systemui.shared.clocks.controller.FlexClockController
import com.android.systemui.shared.clocks.controller.FlexClockController.Companion.buildPresetGroup
import com.android.systemui.shared.clocks.controller.FlexClockController.Companion.getDefaultAxes

private val TAG = DefaultClockProvider::class.simpleName
const val DEFAULT_CLOCK_ID = "DEFAULT"
const val FLEX_CLOCK_ID = "DIGITAL_CLOCK_FLEX"
const val METRO_CLOCK_ID = "DIGITAL_CLOCK_METRO"
const val CALLIGRAPHY_CLOCK_ID = "DIGITAL_CLOCK_CALLIGRAPHY"
const val BUBBLE_CLOCK_ID = "DIGITAL_CLOCK_CLOCK_BUBBLE"
const val GOING_DIGITAL_CLOCK_ID = "ANALOG_CLOCK_GOING_DIGITAL"
const val WEATHER_CLOCK_ID = "DIGITAL_CLOCK_NUMERIC_WEATHER"
const val BOLD_CLOCK_ID = "DIGITAL_CLOCK_BOLD"
const val HANDWRITTEN_CLOCK_ID = "DIGITAL_CLOCK_HANDWRITTEN"
const val MINIMAL_CLOCK_ID = "DIGITAL_CLOCK_MINIMAL"
const val STOPWATCH_CLOCK_ID = "ANALOG_CLOCK_STOPWATCH"
const val VERSUS_CLOCK_ID = "ANALOG_CLOCK_VERSUS"
const val TYPOGRAPHIC_CLOCK_ID = "DIGITAL_CLOCK_TYPOGRAPHIC"

/** Provides default and extended system clock designs for BlazeAOSP */
class DefaultClockProvider
@JvmOverloads
constructor(
    val layoutInflater: LayoutInflater,
    val resources: Resources,
    private val vibrator: Vibrator?,
    private val timeKeeperFactory: () -> TimeKeeper = { TimeKeeperImpl() },
) : ClockProvider {
    private var messageBuffers: ClockMessageBuffers? = null

    override fun initialize(buffers: ClockMessageBuffers?) {
        messageBuffers = buffers
    }

    override fun getClocks(): List<ClockMetadata> {
        return listOf(
            ClockMetadata(DEFAULT_CLOCK_ID),
            ClockMetadata(METRO_CLOCK_ID),
            ClockMetadata(CALLIGRAPHY_CLOCK_ID),
            ClockMetadata(BUBBLE_CLOCK_ID),
            ClockMetadata(GOING_DIGITAL_CLOCK_ID),
            ClockMetadata(FLEX_CLOCK_ID),
            ClockMetadata(WEATHER_CLOCK_ID),
            ClockMetadata(BOLD_CLOCK_ID),
            ClockMetadata(HANDWRITTEN_CLOCK_ID),
            ClockMetadata(MINIMAL_CLOCK_ID),
            ClockMetadata(STOPWATCH_CLOCK_ID),
            ClockMetadata(VERSUS_CLOCK_ID),
            ClockMetadata(TYPOGRAPHIC_CLOCK_ID),
        )
    }

    override fun createClock(ctx: Context, settings: ClockSettings): ClockController {
        if (getClocks().all { it.clockId != settings.clockId }) {
            throw IllegalArgumentException("${settings.clockId} is unsupported by $TAG")
        }

        val buffers = messageBuffers ?: ClockMessageBuffers(ClockLogger.DEFAULT_MESSAGE_BUFFER)
        val fontAxes = getDefaultAxes(settings).merge(settings.axes)
        val clockSettings = settings.copy(axes = ClockAxisStyle(fontAxes))

        val targetTypeface = when (settings.clockId) {
            METRO_CLOCK_ID -> Typeface.create("sans-serif-condensed", Typeface.BOLD)
            CALLIGRAPHY_CLOCK_ID -> Typeface.create("serif", Typeface.ITALIC)
            BUBBLE_CLOCK_ID -> Typeface.create("casual", Typeface.BOLD)
            GOING_DIGITAL_CLOCK_ID -> Typeface.create("monospace", Typeface.BOLD)
            WEATHER_CLOCK_ID -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
            BOLD_CLOCK_ID -> Typeface.create("sans-serif-black", Typeface.BOLD)
            HANDWRITTEN_CLOCK_ID -> Typeface.create("cursive", Typeface.NORMAL)
            MINIMAL_CLOCK_ID -> Typeface.create("sans-serif-thin", Typeface.NORMAL)
            STOPWATCH_CLOCK_ID -> Typeface.create("monospace", Typeface.NORMAL)
            VERSUS_CLOCK_ID -> Typeface.create("sans-serif-condensed-light", Typeface.BOLD)
            TYPOGRAPHIC_CLOCK_ID -> Typeface.create("serif", Typeface.BOLD)
            else -> FLEX_TYPEFACE
        }

        val typefaceCache =
            TypefaceCache<Unit>(buffers.infraMessageBuffer, NUM_CLOCK_FONT_ANIMATION_STEPS) {
                targetTypeface
            }
        return FlexClockController(
            FlexClockContext(
                typefaceCache,
                ClockContextImpl(
                    ctx,
                    resources,
                    clockSettings,
                    buffers.infraMessageBuffer,
                    vibrator,
                    timeKeeperFactory(),
                    isAnimationEnabled = true,
                ),
            ),
            buffers,
        )
    }

    override fun getClockPickerConfig(settings: ClockSettings): ClockPickerConfig {
        if (getClocks().all { it.clockId != settings.clockId }) {
            throw IllegalArgumentException("${settings.clockId} is unsupported by $TAG")
        }

        val (name, description) = when (settings.clockId) {
            METRO_CLOCK_ID -> Pair("Metro", "Metro digital clock style")
            CALLIGRAPHY_CLOCK_ID -> Pair("Calligraphy", "Elegant calligraphy clock style")
            BUBBLE_CLOCK_ID -> Pair("Bubble", "Bubble digital clock style")
            GOING_DIGITAL_CLOCK_ID -> Pair("Going Digital", "Going digital clock style")
            FLEX_CLOCK_ID -> Pair("Flex", "Flexible digital clock style")
            WEATHER_CLOCK_ID -> Pair("Weather", "Weather numeric clock style")
            BOLD_CLOCK_ID -> Pair("Bold", "Bold digital clock style")
            HANDWRITTEN_CLOCK_ID -> Pair("Handwritten", "Handwritten clock style")
            MINIMAL_CLOCK_ID -> Pair("Minimal", "Minimal clock style")
            STOPWATCH_CLOCK_ID -> Pair("Stopwatch", "Stopwatch analog clock style")
            VERSUS_CLOCK_ID -> Pair("Versus", "Versus analog clock style")
            TYPOGRAPHIC_CLOCK_ID -> Pair("Typographic", "Typographic clock style")
            else -> Pair(
                resources.getString(R.string.clock_default_name),
                resources.getString(R.string.clock_default_description)
            )
        }

        val fontAxes = getDefaultAxes(settings).merge(settings.axes)
        return ClockPickerConfig(
            settings.clockId ?: DEFAULT_CLOCK_ID,
            name,
            description,
            resources.getDrawable(R.drawable.clock_default_thumbnail, null),
            isReactiveToTone = true,
            axes = fontAxes,
            presetConfig =
                AxisPresetConfig(
                        listOf(
                            buildPresetGroup(resources, isRound = true),
                            buildPresetGroup(resources, isRound = false),
                        )
                    )
                    .let { cfg -> cfg.copy(current = cfg.findStyle(ClockAxisStyle(fontAxes))) },
        )
    }

    companion object {
        // 750ms @ 120hz -> 90 frames of animation
        // In practice, 30 looks good enough and limits our memory usage
        const val NUM_CLOCK_FONT_ANIMATION_STEPS = 30

        val FLEX_TYPEFACE: Typeface by lazy {
            // TODO(b/364680873): Move constant to config_clockFontFamily when shipping
            Typeface.create("google-sans-flex-clock", Typeface.NORMAL)
        }
    }
}
