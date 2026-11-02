/*
 * Copyright (C) 2026 The Android Open Source Project
 * Copyright (C) 2026 The BlazeAOSP Project
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

package com.android.systemui.statusbar.quickactions.popups.shared

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONArray

object DynamicIslandFeatureSettings {
    const val MASTER_ENABLED = "ax_dynamic_bar_enabled"
    const val LEGACY_MASTER_ENABLED = "status_bar_show_dynamic_island"
    const val KEYGUARD_ENABLED = "ax_dynamic_bar_keyguard_enabled"
    const val EVENTS_JSON = "ax_dynamic_bar_events"
    const val MEDIA_WAVEFORM_SEEKBAR = "media_waveform_seekbar"

    const val BATTERY = "status_bar_dynamic_island_battery"
    const val UNLOCK = "status_bar_dynamic_island_unlock"
    const val BLUETOOTH = "status_bar_dynamic_island_bluetooth"
    const val CALLS = "status_bar_dynamic_island_calls"
    const val NOTIFICATIONS = "status_bar_dynamic_island_notifications"
    const val ONGOING_ACTIVITIES = "status_bar_dynamic_island_ongoing_activities"
    const val HOTSPOT = "status_bar_dynamic_island_hotspot"
    const val VPN = "status_bar_dynamic_island_vpn"
    const val RINGER = "status_bar_dynamic_island_ringer"
    const val CLIPBOARD = "status_bar_dynamic_island_clipboard"
    const val TIMER = "status_bar_dynamic_island_timer"
    const val AUDIO_RECORDING = "status_bar_dynamic_island_audio_recording"
    const val NOW_PLAYING = "status_bar_dynamic_island_now_playing"
    const val RECENT_APPS = "status_bar_dynamic_island_recent_apps"

    const val MEDIA_CONTROLS = "status_bar_dynamic_island_media_controls"
    const val SCREEN_RECORDING = "status_bar_dynamic_island_screen_recording"
    const val ALARMS = "status_bar_dynamic_island_alarms"
    const val FLASHLIGHT = "status_bar_dynamic_island_flashlight"
    const val STOPWATCH = "status_bar_dynamic_island_stopwatch"
    const val LIVE_SCORES = "status_bar_dynamic_island_live_scores"
    const val SHOW_LYRICS = "status_bar_dynamic_island_lyrics"
    const val WIDTH_SCALE = "status_bar_dynamic_island_width_scale"
    const val HEIGHT_SCALE = "status_bar_dynamic_island_height_scale"
    const val POPUP_COLOR_MODE = "status_bar_dynamic_island_popup_color_mode"
    const val LOCKSCREEN = "status_bar_dynamic_island_lockscreen"

    const val SCALE_MIN = 0.7f
    const val SCALE_MAX = 1.4f
    private const val SCALE_PERCENT_DEFAULT = 100

    const val POPUP_COLOR_MODE_DEFAULT = 0
    const val POPUP_COLOR_MODE_BLUR = 1
    const val POPUP_COLOR_MODE_SOLID_BLACK = 2

    private val KEY_TO_AX_EVENT = mapOf(
        BATTERY to "charging",
        UNLOCK to "biometric_unlock",
        BLUETOOTH to "bluetooth",
        CALLS to "notification",
        NOTIFICATIONS to "notification",
        ONGOING_ACTIVITIES to "promoted_ongoing",
        HOTSPOT to "hotspot",
        VPN to "vpn",
        RINGER to "ringer",
        CLIPBOARD to "clipboard",
        TIMER to "timer",
        AUDIO_RECORDING to "audio_recording",
        NOW_PLAYING to "media",
        RECENT_APPS to "app_switch",
        MEDIA_CONTROLS to "media",
        SCREEN_RECORDING to "screen_recording",
        ALARMS to "alarm",
        FLASHLIGHT to "torch",
        STOPWATCH to "stopwatch",
        LIVE_SCORES to "sports",
    )

    fun ContentResolver.readMasterDynamicIslandEnabled(): Boolean {
        val axEnabled = Settings.Secure.getIntForUser(
            this,
            MASTER_ENABLED,
            1,
            UserHandle.USER_CURRENT,
        )
        val legacyEnabled = Settings.System.getIntForUser(
            this,
            LEGACY_MASTER_ENABLED,
            1,
            UserHandle.USER_CURRENT,
        )
        return axEnabled != 0 && legacyEnabled != 0
    }

    private fun ContentResolver.readDisabledAxEvents(): Set<String> {
        val json = Settings.Secure.getStringForUser(this, EVENTS_JSON, UserHandle.USER_CURRENT)
            ?: return emptySet()
        if (json.isBlank()) return emptySet()
        return try {
            val arr = JSONArray(json)
            buildSet {
                for (i in 0 until arr.length()) {
                    add(arr.getString(i))
                }
            }
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun ContentResolver.readDynamicIslandPopupColorMode(): Int {
        return Settings.System.getIntForUser(
            this,
            POPUP_COLOR_MODE,
            POPUP_COLOR_MODE_DEFAULT,
            UserHandle.USER_CURRENT,
        )
    }

    fun observeDynamicIslandPopupColorMode(context: Context): Flow<Int> =
        callbackFlow {
            val observer =
                object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        trySend(context.contentResolver.readDynamicIslandPopupColorMode())
                    }
                }
            context.contentResolver.registerContentObserver(
                Settings.System.getUriFor(POPUP_COLOR_MODE),
                false,
                observer,
                UserHandle.USER_ALL,
            )
            trySend(context.contentResolver.readDynamicIslandPopupColorMode())
            awaitClose { context.contentResolver.unregisterContentObserver(observer) }
        }

    fun ContentResolver.readDynamicIslandFeatureEnabled(
        key: String,
        defaultValue: Boolean = true,
    ): Boolean {
        if (key == MASTER_ENABLED || key == LEGACY_MASTER_ENABLED) {
            return readMasterDynamicIslandEnabled()
        }
        if (key == LOCKSCREEN) {
            val axKeyguard = Settings.Secure.getIntForUser(
                this,
                KEYGUARD_ENABLED,
                1,
                UserHandle.USER_CURRENT,
            ) != 0
            val sysKeyguard = Settings.System.getIntForUser(
                this,
                LOCKSCREEN,
                1,
                UserHandle.USER_CURRENT,
            ) != 0
            return axKeyguard && sysKeyguard
        }
        val axEvent = KEY_TO_AX_EVENT[key]
        if (axEvent != null && axEvent in readDisabledAxEvents()) {
            return false
        }
        return Settings.System.getIntForUser(
            this,
            key,
            if (defaultValue) 1 else 0,
            UserHandle.USER_CURRENT,
        ) != 0
    }

    fun ContentResolver.readDynamicIslandScale(key: String): Float {
        val percent = Settings.System.getIntForUser(
            this,
            key,
            SCALE_PERCENT_DEFAULT,
            UserHandle.USER_CURRENT,
        )
        return (percent / 100f).coerceIn(SCALE_MIN, SCALE_MAX)
    }

    fun observeDynamicIslandFeatureEnabled(
        context: Context,
        key: String,
        defaultValue: Boolean = true,
    ): Flow<Boolean> =
        callbackFlow {
            val observer =
                object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        trySend(
                            context.contentResolver.readDynamicIslandFeatureEnabled(
                                key,
                                defaultValue,
                            )
                        )
                    }
                }

            context.contentResolver.registerContentObserver(
                Settings.System.getUriFor(key),
                false,
                observer,
                UserHandle.USER_ALL,
            )
            if (key == MASTER_ENABLED || key == LEGACY_MASTER_ENABLED) {
                context.contentResolver.registerContentObserver(
                    Settings.Secure.getUriFor(MASTER_ENABLED),
                    false,
                    observer,
                    UserHandle.USER_ALL,
                )
                context.contentResolver.registerContentObserver(
                    Settings.System.getUriFor(LEGACY_MASTER_ENABLED),
                    false,
                    observer,
                    UserHandle.USER_ALL,
                )
            }
            context.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(EVENTS_JSON),
                false,
                observer,
                UserHandle.USER_ALL,
            )
            if (key == LOCKSCREEN) {
                context.contentResolver.registerContentObserver(
                    Settings.Secure.getUriFor(KEYGUARD_ENABLED),
                    false,
                    observer,
                    UserHandle.USER_ALL,
                )
            }
            trySend(context.contentResolver.readDynamicIslandFeatureEnabled(key, defaultValue))
            awaitClose { context.contentResolver.unregisterContentObserver(observer) }
        }

    fun observeDynamicIslandScale(
        context: Context,
        key: String,
    ): Flow<Float> =
        callbackFlow {
            val observer =
                object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        trySend(context.contentResolver.readDynamicIslandScale(key))
                    }
                }

            context.contentResolver.registerContentObserver(
                Settings.System.getUriFor(key),
                false,
                observer,
                UserHandle.USER_ALL,
            )
            trySend(context.contentResolver.readDynamicIslandScale(key))
            awaitClose { context.contentResolver.unregisterContentObserver(observer) }
        }
}
