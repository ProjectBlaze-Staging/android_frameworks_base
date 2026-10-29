/*
 * Copyright (C) 2026 Project Blaze / BlazeAOSP
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

package com.android.systemui.clocks

import android.content.ContentResolver
import android.os.UserHandle
import android.provider.Settings

object KeyguardClockSourceManager {
    const val CLOCK_SOURCE_SYSTEM = 0
    const val CLOCK_SOURCE_CUSTOM = 1

    /**
     * Reads the authoritative clock source, performing automatic migration on legacy installs:
     * - If LOCKSCREEN_CLOCK_SOURCE is already set (0 or 1), return it.
     * - If unset (-1) and old LOCK_SCREEN_CUSTOM_CLOCK_STYLE > 0, migrate to CUSTOM (1)
     *   and remember style in LAST_CHOSEN_CUSTOM_CLOCK_STYLE.
     * - If unset and old style <= 0, default to SYSTEM (0).
     */
    @JvmStatic
    fun getClockSource(resolver: ContentResolver, userId: Int = UserHandle.USER_CURRENT): Int {
        val rawSource = try {
            Settings.Secure.getIntForUser(
                resolver,
                Settings.Secure.LOCKSCREEN_CLOCK_SOURCE,
                -1,
                userId
            )
        } catch (e: Exception) {
            -1
        }
        if (rawSource != -1) {
            return rawSource
        }
        val legacyStyle = try {
            Settings.Secure.getIntForUser(
                resolver,
                Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE,
                0,
                userId
            )
        } catch (e: Exception) {
            0
        }
        if (legacyStyle > 0) {
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LOCKSCREEN_CLOCK_SOURCE,
                CLOCK_SOURCE_CUSTOM,
                userId
            )
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LAST_CHOSEN_CUSTOM_CLOCK_STYLE,
                legacyStyle,
                userId
            )
            return CLOCK_SOURCE_CUSTOM
        } else {
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LOCKSCREEN_CLOCK_SOURCE,
                CLOCK_SOURCE_SYSTEM,
                userId
            )
            return CLOCK_SOURCE_SYSTEM
        }
    }

    @JvmStatic
    fun isCustomClockActive(resolver: ContentResolver, userId: Int = UserHandle.USER_CURRENT): Boolean {
        return getClockSource(resolver, userId) == CLOCK_SOURCE_CUSTOM
    }

    /**
     * Resolves the active custom style to render when in CUSTOM mode,
     * restoring from LAST_CHOSEN_CUSTOM_CLOCK_STYLE if necessary.
     */
    @JvmStatic
    fun getActiveCustomClockStyle(resolver: ContentResolver, userId: Int = UserHandle.USER_CURRENT): Int {
        if (!isCustomClockActive(resolver, userId)) {
            return 0
        }
        var style = try {
            Settings.Secure.getIntForUser(
                resolver,
                Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE,
                0,
                userId
            )
        } catch (e: Exception) {
            0
        }
        if (style <= 0) {
            style = try {
                Settings.Secure.getIntForUser(
                    resolver,
                    Settings.Secure.LAST_CHOSEN_CUSTOM_CLOCK_STYLE,
                    1,
                    userId
                )
            } catch (e: Exception) {
                1
            }
            if (style <= 0) style = 1
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE,
                style,
                userId
            )
        }
        return style
    }

    /**
     * Sets the clock source mode (0 = SYSTEM, 1 = CUSTOM) cleanly preserving remembered style.
     */
    @JvmStatic
    fun setClockSource(resolver: ContentResolver, source: Int, userId: Int = UserHandle.USER_CURRENT) {
        if (source == CLOCK_SOURCE_CUSTOM) {
            var style = try {
                Settings.Secure.getIntForUser(
                    resolver,
                    Settings.Secure.LAST_CHOSEN_CUSTOM_CLOCK_STYLE,
                    0,
                    userId
                )
            } catch (e: Exception) {
                0
            }
            if (style <= 0) {
                style = try {
                    Settings.Secure.getIntForUser(
                        resolver,
                        Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE,
                        1,
                        userId
                    )
                } catch (e: Exception) {
                    1
                }
                if (style <= 0) style = 1
            }
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LOCKSCREEN_CLOCK_SOURCE,
                CLOCK_SOURCE_CUSTOM,
                userId
            )
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE,
                style,
                userId
            )
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LAST_CHOSEN_CUSTOM_CLOCK_STYLE,
                style,
                userId
            )
        } else {
            val currentStyle = try {
                Settings.Secure.getIntForUser(
                    resolver,
                    Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE,
                    0,
                    userId
                )
            } catch (e: Exception) {
                0
            }
            if (currentStyle > 0) {
                Settings.Secure.putIntForUser(
                    resolver,
                    Settings.Secure.LAST_CHOSEN_CUSTOM_CLOCK_STYLE,
                    currentStyle,
                    userId
                )
            }
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LOCKSCREEN_CLOCK_SOURCE,
                CLOCK_SOURCE_SYSTEM,
                userId
            )
            Settings.Secure.putIntForUser(
                resolver,
                Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLE,
                0,
                userId
            )
        }
    }
}
