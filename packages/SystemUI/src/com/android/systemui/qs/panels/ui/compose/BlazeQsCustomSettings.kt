/*
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.qs.panels.ui.compose

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.provider.Settings
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lineageos.providers.LineageSettings

@Immutable
data class BlazeQsSettings(
    val qsColumns: Int = 4,
    val qsRows: Int = 2,
    val qqsRows: Int = 2,
    val tileShape: Int = 0,
    val hideLabels: Boolean = false,
    val tileHaptic: Boolean = false,
    val showBrightnessSlider: Int = 1,
    val brightnessSliderAtBottom: Boolean = false,
    val showAutoBrightness: Boolean = true,
    val brightnessSliderHaptic: Boolean = false,
) {
    fun resolveTileShape(defaultCornerRadius: Dp = 28.dp): Shape = when (tileShape) {
        1 -> RoundedCornerShape(percent = 50) // Hap (Pill)
        2 -> RoundedCornerShape(16.dp)        // Yuvarlatılmış Kare (Rounded Square)
        3 -> CircleShape                      // Daire (Circle)
        4 -> RoundedCornerShape(24.dp)        // Squircle (Süper Elips)
        else -> RoundedCornerShape(defaultCornerRadius)
    }
}

private fun readBlazeInt(cr: ContentResolver, key: String, defaultVal: Int): Int {
    try {
        val v = Settings.System.getIntForUser(cr, key, -999, UserHandle.USER_CURRENT)
        if (v != -999) return v
    } catch (_: Throwable) {}
    try {
        val v = Settings.Secure.getIntForUser(cr, key, -999, UserHandle.USER_CURRENT)
        if (v != -999) return v
    } catch (_: Throwable) {}
    try {
        val v = LineageSettings.Secure.getIntForUser(cr, key, -999, UserHandle.USER_CURRENT)
        if (v != -999) return v
    } catch (_: Throwable) {}
    try {
        val v = LineageSettings.System.getIntForUser(cr, key, -999, UserHandle.USER_CURRENT)
        if (v != -999) return v
    } catch (_: Throwable) {}
    return defaultVal
}

private fun loadBlazeQsSettings(context: Context, isLandscape: Boolean): BlazeQsSettings {
    val cr = context.contentResolver
    val rawCols = if (isLandscape) {
        readBlazeInt(cr, "qs_columns_landscape", 4)
    } else {
        readBlazeInt(cr, "qs_columns_portrait", 4)
    }
    // Map legacy AOSP 2-column pill default to 4 base grid cells so 1x1 circle tiles
    // and 2x1 pill tiles maintain proper proportions and never expand to half-screen circles.
    val cols = (if (rawCols <= 2) 4 else rawCols).coerceIn(3, 6)

    val rows = if (isLandscape) {
        readBlazeInt(cr, "qs_rows_landscape", 2)
    } else {
        readBlazeInt(cr, "qs_rows_portrait", 4)
    }.coerceIn(1, 6)

    val qqsRows = if (isLandscape) {
        readBlazeInt(cr, "qqs_rows_landscape", 1)
    } else {
        readBlazeInt(cr, "qqs_rows_portrait", 2)
    }.coerceIn(1, 4)

    val tileShape = readBlazeInt(cr, "qs_tile_shape", 0)
    val hideLabels = readBlazeInt(cr, "qs_tile_label_hide", 0) != 0
    val tileHaptic = readBlazeInt(cr, "qs_tile_haptic", 1) != 0
    val showSlider = readBlazeInt(cr, "qs_show_brightness_slider", 1)
    val sliderPos = readBlazeInt(cr, "qs_brightness_slider_position", 0)
    val showAuto = readBlazeInt(cr, "qs_show_auto_brightness", 1) != 0
    val sliderHaptic = readBlazeInt(cr, "qs_brightness_slider_haptic", 1) != 0

    return BlazeQsSettings(
        qsColumns = cols,
        qsRows = rows,
        qqsRows = qqsRows,
        tileShape = tileShape,
        hideLabels = hideLabels,
        tileHaptic = tileHaptic,
        showBrightnessSlider = showSlider,
        brightnessSliderAtBottom = sliderPos == 1,
        showAutoBrightness = showAuto,
        brightnessSliderHaptic = sliderHaptic,
    )
}

private object BlazeQsSettingsStore {
    private var initialized = false
    val portraitState = mutableStateOf(BlazeQsSettings())
    val landscapeState = mutableStateOf(BlazeQsSettings())

    fun ensureInitialized(context: Context) {
        if (initialized) return
        val appCtx = context.applicationContext ?: context
        initialized = true
        portraitState.value = loadBlazeQsSettings(appCtx, isLandscape = false)
        landscapeState.value = loadBlazeQsSettings(appCtx, isLandscape = true)

        val cr = appCtx.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                portraitState.value = loadBlazeQsSettings(appCtx, isLandscape = false)
                landscapeState.value = loadBlazeQsSettings(appCtx, isLandscape = true)
            }
        }
        val systemKeys = listOf(
            "qs_columns_portrait",
            "qs_columns_landscape",
            "qs_rows_portrait",
            "qs_rows_landscape",
            "qqs_rows_portrait",
            "qqs_rows_landscape",
            "qs_tile_shape",
            "qs_tile_label_hide",
            "qs_tile_haptic",
            "qs_show_brightness_slider",
            "qs_brightness_slider_position",
            "qs_show_auto_brightness",
            "qs_brightness_slider_haptic",
        )
        try {
            systemKeys.forEach { key ->
                cr.registerContentObserver(Settings.System.getUriFor(key), false, observer, UserHandle.USER_ALL)
                cr.registerContentObserver(Settings.Secure.getUriFor(key), false, observer, UserHandle.USER_ALL)
                cr.registerContentObserver(LineageSettings.System.getUriFor(key), false, observer, UserHandle.USER_ALL)
                cr.registerContentObserver(LineageSettings.Secure.getUriFor(key), false, observer, UserHandle.USER_ALL)
            }
        } catch (_: Throwable) {}
    }
}

@Composable
fun rememberBlazeQsSettings(): BlazeQsSettings {
    val context = LocalContext.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    remember(context) {
        BlazeQsSettingsStore.ensureInitialized(context)
        true
    }
    return if (isLandscape) {
        BlazeQsSettingsStore.landscapeState.value
    } else {
        BlazeQsSettingsStore.portraitState.value
    }
}
