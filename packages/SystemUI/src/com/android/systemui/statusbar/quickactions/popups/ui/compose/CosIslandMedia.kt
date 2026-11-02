/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.statusbar.quickactions.popups.ui.compose

import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object CosIslandMedia {
    private var voiceprint: LottieComposition? = null

    suspend fun voiceprint(context: android.content.Context): LottieComposition? =
        voiceprint
            ?: withContext(Dispatchers.IO) {
                    runCatching {
                            val json =
                                context.assets.open("cos/media/voiceprintDark.json").bufferedReader().use {
                                    it.readText()
                                }
                            LottieCompositionFactory.fromJsonStringSync(json, "cos_voiceprint").value
                        }
                        .getOrNull()
                }
                .also { voiceprint = it }
}

@Composable
internal fun Modifier.cosAlbumShake(track: Any?): Modifier {
    val angle = remember { Animatable(0f) }
    var shown by remember { mutableStateOf(track) }
    LaunchedEffect(track) {
        if (shown == track) return@LaunchedEffect
        shown = track
        angle.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 93.4f), initialVelocity = -3140f)
    }
    return graphicsLayer { rotationZ = angle.value }
}

@Composable
internal fun CosCapsuleArtwork(artwork: Drawable, track: Any?, size: Dp) {
    val bitmap = remember(artwork) { artwork.toBitmap(96, 96).asImageBitmap() }
    Image(
        bitmap = bitmap,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(size).clip(CircleShape).cosAlbumShake(track),
    )
}

@Composable
internal fun rememberCosArtTint(artwork: Drawable?): Color {
    var tint by remember { mutableStateOf(Color.Transparent) }
    LaunchedEffect(artwork) {
        if (artwork == null) {
            tint = Color.Transparent
            return@LaunchedEffect
        }
        tint =
            withContext(Dispatchers.Default) {
                runCatching {
                        val palette = Palette.from(artwork.toBitmap(64, 64)).generate()
                        val rgb = palette.dominantSwatch?.rgb ?: return@runCatching Color.Transparent
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(rgb, hsv)
                        hsv[1] = maxOf(hsv[1], 0.6f)
                        hsv[2] = 0.8f
                        Color(android.graphics.Color.HSVToColor(26, hsv))
                    }
                    .getOrDefault(Color.Transparent)
            }
    }
    return animateColorAsState(tint, tween(450), label = "cos_art_tint").value
}

@Composable
internal fun CosVoicePrint(playing: Boolean, size: Dp) {
    val context = LocalContext.current
    var composition by remember { mutableStateOf<LottieComposition?>(null) }
    LaunchedEffect(Unit) { composition = CosIslandMedia.voiceprint(context) }
    val loaded = composition
    if (playing && loaded != null) {
        LottieAnimation(
            composition = loaded,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(size),
        )
    } else {
        Canvas(Modifier.size(size)) {
            val unit = this.size.width / 26f
            for (i in 0 until 6) {
                drawRoundRect(
                    Color.White.copy(alpha = 0.8f),
                    topLeft = Offset((0.5f + 4.5f * i) * unit, 11.75f * unit),
                    size = Size(2.5f * unit, 2.5f * unit),
                    cornerRadius = CornerRadius(1.25f * unit),
                )
            }
        }
    }
}
