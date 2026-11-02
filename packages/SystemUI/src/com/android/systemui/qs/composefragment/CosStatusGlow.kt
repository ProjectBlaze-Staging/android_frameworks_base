/*
 * Copyright (C) 2026 The PenguinOS Project
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.qs.composefragment

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.cosStatusGlow(
    trigger: Any?,
    cornerRadius: Dp,
    capsule: Boolean = false,
    startAngle: Float = 0.25f,
): Modifier {
    val glowAlpha = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger != null) {
            glowAlpha.snapTo(0.85f)
            glowAlpha.animateTo(0f, tween(900, easing = FastOutSlowInEasing))
        }
    }
    return drawWithContent {
        drawContent()
        val a = glowAlpha.value
        if (a > 0.01f) {
            val strokePx = 1.8.dp.toPx()
            val r = if (capsule) size.height / 2f else cornerRadius.toPx()
            val brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = a),
                    Color(0xFF66B2FF).copy(alpha = a * 0.6f),
                    Color.White.copy(alpha = a * 0.8f),
                )
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(strokePx / 2f, strokePx / 2f),
                size = Size(size.width - strokePx, size.height - strokePx),
                cornerRadius = CornerRadius(r),
                style = Stroke(width = strokePx),
            )
        }
    }
}

@Composable
fun Modifier.cosSeekGlow(
    fraction: Float,
    isPlaying: Boolean,
    isScrubbing: Boolean,
    accentArgb: Int = 0,
): Modifier {
    if (!isPlaying && !isScrubbing) return this
    val accent = if (accentArgb != 0) Color(accentArgb) else Color.White
    return drawWithContent {
        drawContent()
        if (isScrubbing && size.width > 0f) {
            val x = (fraction.coerceIn(0f, 1f) * size.width)
            drawCircle(
                color = accent.copy(alpha = 0.35f),
                radius = size.height * 1.4f,
                center = Offset(x, size.height / 2f),
            )
        }
    }
}
