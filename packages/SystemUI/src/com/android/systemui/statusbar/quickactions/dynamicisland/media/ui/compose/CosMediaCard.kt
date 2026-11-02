/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.statusbar.quickactions.dynamicisland.media.ui.compose

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import com.android.systemui.common.shared.model.Icon as UiIcon
import com.android.systemui.common.ui.compose.Icon as UiIconView
import com.android.systemui.media.controls.shared.model.MediaAction
import com.android.systemui.statusbar.quickactions.dynamicisland.media.shared.model.MediaControlChipModel
import com.android.systemui.qs.composefragment.cosSeekGlow
import com.android.systemui.statusbar.quickactions.popups.ui.compose.rememberAlbumArtAccent
import com.android.systemui.statusbar.quickactions.popups.ui.compose.rememberCosArtTint
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private object CosMediaCard {
    val Corner = 26.dp
    val Fill = Color(0xFF1B1B1B)
    val Stroke = Color(0x40959595)
    val ArtSize = 44.dp
    val ArtCorner = 10.dp
    val Margin = 16.dp
    val TitleHeight = 24.dp
    val ContentHeight = 20.dp
    val Secondary = Color(0x8AFFFFFF)
    val ButtonSize = 36.dp
    val ButtonArea = 42.dp
    val ButtonSpacing = 24.dp
    val Track = Color(0x26FFFFFF)

    val Move = CubicBezierEasing(0.3f, 0f, 0.1f, 1f)
    val PressDown = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val PressUp = CubicBezierEasing(0f, 0f, 0.2f, 1f)

    private val compositions = ConcurrentHashMap<String, Result<LottieComposition>>()

    suspend fun lottie(context: Context, name: String): LottieComposition? =
        compositions
            .getOrPut(name) {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val json =
                            context.assets.open("cos/media/$name").bufferedReader().use {
                                it.readText()
                            }
                        LottieCompositionFactory.fromJsonStringSync(json, "cos_media_$name").value!!
                    }
                }
            }
            .getOrNull()
}

@Composable
private fun rememberCosLottie(name: String): LottieComposition? {
    val context = LocalContext.current
    var composition by remember(name) { mutableStateOf<LottieComposition?>(null) }
    LaunchedEffect(name) { composition = CosMediaCard.lottie(context, name) }
    return composition
}

@Composable
fun CosMediaCard(model: MediaControlChipModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val glassStyle = com.android.internal.penguin.glass.LiquidGlass.getEffectiveStyle(context)
    val artwork = remember(model.artworkIcon) { (model.artworkIcon as? UiIcon.Loaded)?.drawable }
    val tint = rememberCosArtTint(artwork)
    val shape = RoundedCornerShape(CosMediaCard.Corner)
    val baseFill =
        when (glassStyle) {
            com.android.internal.penguin.glass.LiquidGlass.STYLE_IOS_LIQUID_GLASS ->
                Color(0x9E101218)
            com.android.internal.penguin.glass.LiquidGlass.STYLE_COLOROS_FROSTED ->
                Color(0xBA181A20)
            else -> CosMediaCard.Fill
        }
    val borderBrush =
        remember(glassStyle) {
            if (glassStyle == com.android.internal.penguin.glass.LiquidGlass.STYLE_IOS_LIQUID_GLASS) {
                androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0x99FFFFFF), Color(0x28FFFFFF), Color(0x6670B8FF))
                )
            } else {
                androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(CosMediaCard.Stroke, CosMediaCard.Stroke)
                )
            }
        }
    val strokeW =
        if (glassStyle == com.android.internal.penguin.glass.LiquidGlass.STYLE_IOS_LIQUID_GLASS) {
            1.4.dp
        } else {
            1.dp
        }
    Column(
        modifier =
            modifier
                .widthIn(min = 320.dp, max = 400.dp)
                .clip(shape)
                .background(baseFill)
                .background(tint)
                .border(strokeW, borderBrush, shape)
                .padding(bottom = CosMediaCard.Margin)
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = CosMediaCard.Margin,
                        end = CosMediaCard.Margin,
                        top = CosMediaCard.Margin,
                    )
                    .pointerInput(model.openApp) { detectTapGestures { model.openApp?.invoke() } },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CosCardArtwork(model)
            Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
                CosSwitchText(
                    text = model.songName?.toString().orEmpty(),
                    color = Color.White,
                    size = 16,
                    bold = true,
                    height = CosMediaCard.TitleHeight,
                )
                Spacer(Modifier.height(2.dp))
                CosSwitchText(
                    text = model.artistName?.toString().orEmpty(),
                    color = CosMediaCard.Secondary,
                    size = 14,
                    bold = false,
                    height = CosMediaCard.ContentHeight,
                )
            }
            CosCardVoicePrint(model.isPlaying)
        }

        if (model.durationMs > 0L) {
            CosSeekBar(
                model = model,
                glow = rememberAlbumArtAccent(artwork, fallback = Color.White).toArgb(),
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = CosMediaCard.Margin)
                        .padding(top = 6.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement =
                Arrangement.spacedBy(CosMediaCard.ButtonSpacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CosLottieButton(model.previousAction, "previous_anim.json")
            CosPlayPause(model)
            CosLottieButton(model.nextAction, "next_anim.json")
        }
    }
}

@Composable
private fun CosCardArtwork(model: MediaControlChipModel) {
    val angle = remember { Animatable(0f) }
    var shown by remember { mutableStateOf(model.artworkIcon) }
    LaunchedEffect(model.artworkIcon) {
        if (shown == model.artworkIcon) return@LaunchedEffect
        shown = model.artworkIcon
        angle.snapTo(0f)
        angle.animateTo(
            0f,
            spring(dampingRatio = 0.5f, stiffness = 93.4f),
            initialVelocity = -3140f,
        )
    }
    val artwork = model.artworkIcon ?: model.appIcon
    Box(
        Modifier.size(CosMediaCard.ArtSize)
            .graphicsLayer { rotationZ = angle.value }
            .clip(RoundedCornerShape(CosMediaCard.ArtCorner))
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        if (artwork != null) {
            UiIconView(icon = artwork, modifier = Modifier.size(CosMediaCard.ArtSize), tint = Color.Unspecified)
        }
    }
}

@Composable
private fun CosSwitchText(text: String, color: Color, size: Int, bold: Boolean, height: Dp) {
    Crossfade(
        targetState = text,
        animationSpec = tween(300, easing = CosMediaCard.Move),
        label = "cos_media_text",
    ) { words ->
        Text(
            text = words,
            color = color,
            fontSize = size.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.height(height),
        )
    }
}

@Composable
private fun CosCardVoicePrint(playing: Boolean) {
    val composition = rememberCosLottie("voiceprintDark.json")
    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        if (playing && composition != null) {
            LottieAnimation(
                composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(24.dp),
            )
        } else {
            Canvas(Modifier.size(24.dp)) {
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
}

@Composable
private fun CosSeekBar(model: MediaControlChipModel, glow: Int, modifier: Modifier = Modifier) {
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val duration = model.durationMs.coerceAtLeast(1L)
    val shownFraction =
        if (dragging) dragFraction else (model.positionMs.toFloat() / duration).coerceIn(0f, 1f)
    val thickness by
        animateDpAsState(if (dragging) 6.dp else 4.dp, tween(150, easing = CosMediaCard.Move))
    Row(modifier.height(14.dp), verticalAlignment = Alignment.CenterVertically) {
        CosTime((shownFraction * duration).toLong())
        Canvas(
            Modifier.weight(1f)
                .height(14.dp)
                .padding(horizontal = 4.dp)
                .cosSeekGlow(shownFraction, model.isPlaying, dragging, glow)
                .then(
                    if (model.canBeScrubbed && model.seekTo != null) {
                        Modifier.pointerInput(duration) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    dragging = true
                                    dragFraction = (it.x / size.width).coerceIn(0f, 1f)
                                },
                                onDragEnd = {
                                    model.seekTo?.invoke((dragFraction * duration).toLong())
                                    dragging = false
                                },
                                onDragCancel = { dragging = false },
                                onHorizontalDrag = { change, _ ->
                                    dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                },
                            )
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            val h = thickness.toPx()
            val top = (size.height - h) / 2f
            val radius = CornerRadius(h / 2f)
            drawRoundRect(CosMediaCard.Track, Offset(0f, top), Size(size.width, h), radius)
            drawRoundRect(Color.White, Offset(0f, top), Size(size.width * shownFraction, h), radius)
        }
        CosTime(model.durationMs)
    }
}

@Composable
private fun CosTime(ms: Long) {
    val seconds = (ms / 1000).coerceAtLeast(0)
    Text(
        text = "%d:%02d".format(seconds / 60, seconds % 60),
        color = CosMediaCard.Secondary,
        fontSize = 10.sp,
        maxLines = 1,
    )
}

@Composable
private fun Modifier.cosPress(enabled: Boolean, onClick: () -> Unit): Modifier {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    return this.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    scope.launch { scale.animateTo(0.92f, tween(200, easing = CosMediaCard.PressDown)) }
                    tryAwaitRelease()
                    scope.launch { scale.animateTo(1f, tween(340, easing = CosMediaCard.PressUp)) }
                },
                onTap = { onClick() },
            )
        }
}

@Composable
private fun CosLottieButton(action: MediaAction?, animation: String) {
    val composition = rememberCosLottie(animation)
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val enabled = action?.action != null
    Box(
        Modifier.size(CosMediaCard.ButtonArea)
            .graphicsLayer { alpha = if (enabled) 1f else 0.3f }
            .cosPress(enabled) {
                action?.action?.run()
                val playing = composition ?: return@cosPress
                scope.launch {
                    progress.snapTo(0f)
                    progress.animateTo(1f, tween(playing.duration.toInt(), easing = LinearEasing))
                    progress.snapTo(0f)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (composition != null) {
            LottieAnimation(
                composition,
                progress = { progress.value },
                modifier = Modifier.size(CosMediaCard.ButtonSize),
            )
        }
    }
}

@Composable
private fun CosPlayPause(model: MediaControlChipModel) {
    val toPlay = rememberCosLottie("playTransitionDark.json")
    val toPause = rememberCosLottie("pauseTransitionDark.json")
    val progress = remember { Animatable(1f) }
    var shownPlaying by remember { mutableStateOf(model.isPlaying) }
    LaunchedEffect(model.isPlaying) {
        if (shownPlaying == model.isPlaying) return@LaunchedEffect
        shownPlaying = model.isPlaying
        val turn = if (model.isPlaying) toPause else toPlay
        progress.snapTo(0f)
        progress.animateTo(1f, tween(turn?.duration?.toInt() ?: 300, easing = LinearEasing))
    }
    val composition = if (shownPlaying) toPause else toPlay
    val action = model.playOrPause
    Box(
        Modifier.size(CosMediaCard.ButtonArea).cosPress(action?.action != null) {
            action?.action?.run()
        },
        contentAlignment = Alignment.Center,
    ) {
        if (composition != null) {
            LottieAnimation(
                composition,
                progress = { progress.value },
                modifier = Modifier.size(CosMediaCard.ButtonSize),
            )
        }
    }
}
