/*
 * Copyright (C) 2026 The PenguinOS Project
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.penguin

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class CosFeature(val key: String, val defaultOn: Boolean = false) {
    AppMotion("penguin_cos_app_motion", defaultOn = true),
    Recents("penguin_cos_recents", defaultOn = true),
    Shade("penguin_cos_shade", defaultOn = true),
    QuickSettings("penguin_cos_qs", defaultOn = true),
    MediaLights("penguin_cos_media", defaultOn = true),
    LockscreenMedia("penguin_cos_lockscreen_media", defaultOn = true),
    LockscreenGlass("penguin_cos_lockscreen_glass", defaultOn = true),
    Clock("penguin_cos_clock", defaultOn = true),
    Volume("penguin_cos_volume", defaultOn = true),
    Island("penguin_cos_island", defaultOn = true);

    companion object {
        const val ALL = "penguin_cos_experience"
    }
}

object CosFeatures {
    private val states = HashMap<CosFeature, MutableStateFlow<Boolean>>()

    @JvmStatic
    fun isOn(context: Context, feature: CosFeature): Boolean {
        val resolver = context.contentResolver
        if (feature == CosFeature.Island) {
            return Settings.Secure.getInt(resolver, feature.key, 1) == 1
        }
        val all = Settings.Secure.getInt(resolver, CosFeature.ALL, if (feature.defaultOn) 1 else 0)
        return Settings.Secure.getInt(resolver, feature.key, all) == 1
    }

    @JvmStatic
    fun state(context: Context, feature: CosFeature): StateFlow<Boolean> =
        synchronized(states) {
            states.getOrPut(feature) {
                val app = context.applicationContext
                MutableStateFlow(isOn(app, feature)).also { flow ->
                    val observer =
                        object : ContentObserver(Handler(Looper.getMainLooper())) {
                            override fun onChange(selfChange: Boolean) {
                                flow.value = isOn(app, feature)
                            }
                        }
                    val resolver = app.contentResolver
                    resolver.registerContentObserver(
                        Settings.Secure.getUriFor(CosFeature.ALL),
                        false,
                        observer,
                    )
                    resolver.registerContentObserver(
                        Settings.Secure.getUriFor(feature.key),
                        false,
                        observer,
                    )
                }
            }
        }
}

@Composable
fun rememberCosFeature(feature: CosFeature): Boolean {
    val on by CosFeatures.state(LocalContext.current, feature).collectAsState()
    return on
}
