/*
 * Copyright (C) 2024-2026 Lunaris AOSP
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

package com.android.systemui.statusbar.quickactions.popups.ui.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.internal.penguin.glass.LiquidGlass
import com.android.systemui.statusbar.quickactions.popups.shared.DynamicIslandFeatureSettings.POPUP_COLOR_MODE_BLUR
import com.android.systemui.statusbar.quickactions.popups.shared.DynamicIslandFeatureSettings.POPUP_COLOR_MODE_SOLID_BLACK
import com.android.systemui.statusbar.quickactions.popups.shared.DynamicIslandFeatureSettings.observeDynamicIslandPopupColorMode
import com.android.systemui.statusbar.quickactions.popups.shared.DynamicIslandFeatureSettings.readDynamicIslandPopupColorMode

@Composable
private fun Modifier.liquidGlassSurface(shape: Shape, style: Int): Modifier {
    val rimBrush = remember(style) {
        if (style == LiquidGlass.STYLE_IOS_LIQUID_GLASS) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x99FFFFFF),
                    Color(0x28FFFFFF),
                    Color(0x6670B8FF),
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x48FFFFFF),
                    Color(0x22FFFFFF),
                )
            )
        }
    }
    val fillColor =
        if (style == LiquidGlass.STYLE_IOS_LIQUID_GLASS) {
            Color(0x9E101218)
        } else {
            Color(0xBA1A1C22)
        }
    val strokeWidth = if (style == LiquidGlass.STYLE_IOS_LIQUID_GLASS) 1.4.dp else 1.dp
    return drawBehind {
        drawOutline(
            shape.createOutline(size, layoutDirection, this),
            fillColor,
        )
    }.border(strokeWidth, rimBrush, shape)
}

@Composable
fun rememberPopupColorMode(): Int {
    val context = LocalContext.current
    val initial = remember(context) { context.contentResolver.readDynamicIslandPopupColorMode() }
    val flow = remember(context) { observeDynamicIslandPopupColorMode(context) }
    val mode by flow.collectAsState(initial = initial)
    val style = LiquidGlass.getEffectiveStyle(context)
    if (style == LiquidGlass.STYLE_IOS_LIQUID_GLASS || style == LiquidGlass.STYLE_COLOROS_FROSTED) {
        return POPUP_COLOR_MODE_BLUR
    }
    return mode
}

@Composable
fun PopupSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 12.dp,
    colorMode: Int = rememberPopupColorMode(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val glassStyle = LiquidGlass.getEffectiveStyle(context)
    val cameraClearance = LocalPopupCameraClearance.current
    val body: @Composable () -> Unit = {
        Box(Modifier.padding(top = cameraClearance)) { content() }
    }
    when {
        glassStyle != LiquidGlass.STYLE_MATERIAL3_LITE || colorMode == POPUP_COLOR_MODE_BLUR -> {
            Box(modifier = modifier.liquidGlassSurface(shape, glassStyle).clip(shape)) {
                CompositionLocalProvider(
                    LocalContentColor provides Color.White
                ) {
                    body()
                }
            }
        }
        colorMode == POPUP_COLOR_MODE_SOLID_BLACK -> {
            Surface(
                color = Color.Black,
                contentColor = Color.White,
                shape = shape,
                shadowElevation = shadowElevation,
                modifier = modifier,
            ) { body() }
        }
        else -> {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = shape,
                shadowElevation = shadowElevation,
                modifier = modifier,
            ) { body() }
        }
    }
}

val LocalPopupCameraClearance = compositionLocalOf { 0.dp }
