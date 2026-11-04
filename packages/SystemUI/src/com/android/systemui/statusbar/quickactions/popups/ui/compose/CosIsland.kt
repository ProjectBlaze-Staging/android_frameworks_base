/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.statusbar.quickactions.popups.ui.compose

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal object CosIsland {
    val MinHeight = 30.dp
    val MaxWidth = 202.dp
    val MinWidth = 100.dp
    val Padding = 5.dp
    val IconSize = 20.dp
    val TextSize = 12.sp
    val TextColor = Color(0xE6FFFFFF)

    val ExtraWidth = 16.dp

    val SwitchWidth = 80.dp

    val StrokeWidth = 1.3.dp
    val StrokeColor = Color(0x40959595)
    val MoonWidth = 4.dp
    val MoonGap = 2.dp
    val MoonColor = Color(0xE64A4A4A)
    val ContentBlur = 9.dp

    val Move = CubicBezierEasing(0.3f, 0f, 0.1f, 1f)
    val SwitchGrow = CubicBezierEasing(0.17f, 0.23f, 0.83f, 0.95f)
    val SwitchSettle = CubicBezierEasing(0.17f, 0.56f, 0.1f, 1f)
    private val PressDown = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    private val PressUp = CubicBezierEasing(0f, 0f, 0.2f, 1f)

    fun height(hole: Dp): Dp = maxOf(MinHeight, hole + 4.dp)

    internal suspend fun press(scale: Animatable<Float, *>, down: Boolean) {
        if (down) scale.animateTo(0.98f, tween(200, easing = PressDown))
        else scale.animateTo(1f, tween(340, easing = PressUp))
    }
}

@Composable
internal fun CosIslandShell(
    key: Any,
    visible: Boolean,
    hole: Dp,
    moons: Boolean,
    onGone: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val holePx = with(density) { hole.toPx() }
    val heightPx = with(density) { CosIsland.height(hole).toPx() }
    val extraPx = with(density) { CosIsland.ExtraWidth.toPx() }
    val switchPx = with(density) { CosIsland.SwitchWidth.toPx() }
    val maxWidthPx = with(density) { CosIsland.MaxWidth.roundToPx() }
    val blurPx = with(density) { CosIsland.ContentBlur.toPx() }

    val width = remember { Animatable(0f) }
    val height = remember { Animatable(0f) }
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val blur = remember { Animatable(blurPx) }
    val press = remember { Animatable(1f) }
    val moonAlpha = remember { Animatable(0f) }

    var natural by remember { mutableStateOf(IntSize.Zero) }
    var shownKey by remember { mutableStateOf(key) }
    var busy by remember { mutableStateOf(true) }
    var shownContent by remember { mutableStateOf(content) }
    if (key == shownKey) {
        SideEffect { shownContent = content }
    }
    val body = if (key == shownKey) content else shownContent
    val currentKey by rememberUpdatedState(key)
    val currentVisible by rememberUpdatedState(visible)
    val currentOnGone by rememberUpdatedState(onGone)

    LaunchedEffect(moons) {
        moonAlpha.animateTo(if (moons) 1f else 0f, tween(300, easing = CosIsland.Move))
    }

    suspend fun nextNatural(): IntSize {
        withFrameNanos {}
        withFrameNanos {}
        return snapshotFlow { natural }.first { it.width > 0 }
    }

    suspend fun appear() = coroutineScope {
        val target = nextNatural().width.toFloat()
        launch { width.animateTo(holePx, tween(50, easing = CosIsland.Move)) }
        height.animateTo(holePx, tween(50, easing = CosIsland.Move))
        launch { width.animateTo(target + extraPx, tween(300, easing = CosIsland.Move)) }
        launch { height.animateTo(heightPx, tween(300, easing = CosIsland.Move)) }
        launch { alpha.animateTo(1f, tween(250, delayMillis = 50, easing = CosIsland.Move)) }
        launch { blur.animateTo(0f, tween(250, delayMillis = 50, easing = CosIsland.Move)) }
        scale.animateTo(1.1f, tween(300, easing = CosIsland.Move))
        launch { width.animateTo(target, tween(500, easing = CosIsland.Move)) }
        scale.animateTo(1f, tween(500, easing = CosIsland.Move))
    }

    suspend fun disappear() = coroutineScope {
        val linear300 = tween<Float>(300, easing = LinearEasing)
        val linear200 = tween<Float>(200, easing = LinearEasing)
        launch { width.animateTo(holePx, linear300) }
        launch { height.animateTo(holePx, linear300) }
        launch { alpha.animateTo(0f, linear200) }
        launch { blur.animateTo(blurPx, linear200) }
        scale.animateTo(0.1f, linear300)
        launch { width.animateTo(0f, tween(50, easing = LinearEasing)) }
        height.animateTo(0f, tween(50, easing = LinearEasing))
    }

    suspend fun switchTo(newKey: Any) = coroutineScope {
        val old = natural.width.toFloat().coerceAtLeast(1f)
        val close = tween<Float>(200, easing = LinearEasing)
        launch { width.animateTo(switchPx, close) }
        launch { height.animateTo(heightPx, close) }
        launch { blur.animateTo(blurPx, close) }
        launch { scale.animateTo((switchPx / old).coerceAtMost(1f), close) }
        alpha.animateTo(0f, close)
        shownKey = newKey
        val target = nextNatural().width.toFloat()
        scale.snapTo((switchPx / target).coerceAtMost(1f))
        launch { alpha.animateTo(1f, tween(200, easing = CosIsland.SwitchGrow)) }
        launch { blur.animateTo(0f, tween(200, easing = CosIsland.SwitchGrow)) }
        launch { scale.animateTo(1.1f, tween(200, easing = CosIsland.SwitchGrow)) }
        width.animateTo(target + extraPx, tween(200, easing = CosIsland.SwitchGrow))
        launch { scale.animateTo(1f, tween(500, easing = CosIsland.SwitchSettle)) }
        width.animateTo(target, tween(500, easing = CosIsland.SwitchSettle))
    }

    suspend fun swallowSwell() = coroutineScope {
        val target = (if (natural.width > 0) natural.width.toFloat() else width.value).coerceAtLeast(holePx)
        busy = true
        try {
            launch {
                width.animateTo(
                    (target - extraPx * 0.35f).coerceAtLeast(holePx),
                    tween(65, easing = CosIsland.Move),
                )
            }
            scale.animateTo(0.92f, tween(65, easing = CosIsland.Move))
            launch {
                width.animateTo(
                    (target + extraPx * 2.15f).coerceAtMost(maxWidthPx.toFloat()),
                    tween(210, easing = CosIsland.SwitchGrow),
                )
            }
            launch {
                height.animateTo(
                    heightPx * 1.12f,
                    tween(210, easing = CosIsland.SwitchGrow),
                )
            }
            scale.animateTo(1.18f, tween(210, easing = CosIsland.SwitchGrow))
            launch { width.animateTo(target, tween(460, easing = CosIsland.SwitchSettle)) }
            launch { height.animateTo(heightPx, tween(460, easing = CosIsland.SwitchSettle)) }
            scale.animateTo(1f, tween(460, easing = CosIsland.SwitchSettle))
        } finally {
            busy = false
        }
    }

    val context = LocalContext.current
    var swallowSeq by remember {
        mutableIntStateOf(
            runCatching {
                Settings.Global.getInt(context.contentResolver, "blaze_island_swallow_seq", 0)
            }.getOrDefault(0)
        )
    }
    var initialSwallowSeq by remember { mutableIntStateOf(swallowSeq) }
    DisposableEffect(context) {
        val resolver = context.contentResolver
        val uri = Settings.Global.getUriFor("blaze_island_swallow_seq")
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    swallowSeq =
                        runCatching {
                            Settings.Global.getInt(resolver, "blaze_island_swallow_seq", 0)
                        }.getOrDefault(0)
                }
            }
        if (uri != null) {
            resolver.registerContentObserver(uri, false, observer)
        }
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    LaunchedEffect(swallowSeq) {
        if (swallowSeq != initialSwallowSeq && currentVisible) {
            initialSwallowSeq = swallowSeq
            swallowSwell()
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { currentVisible to currentKey }
            .collectLatest { (shown, newKey) ->
                busy = true
                when {
                    !shown -> {
                        disappear()
                        currentOnGone()
                    }
                    newKey != shownKey && alpha.value > 0.5f -> switchTo(newKey)
                    else -> {
                        shownKey = newKey
                        if (alpha.value < 1f || width.value < 1f) appear()
                    }
                }
                busy = false
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { natural.width to busy }
            .collectLatest { (w, isBusy) ->
                if (!isBusy && w > 0 && currentVisible) {
                    width.animateTo(w.toFloat(), tween(300, easing = CosIsland.Move))
                }
            }
    }

    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(50)
    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = press.value
                    scaleY = press.value
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        scope.launch { CosIsland.press(press, true) }
                        do {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                        } while (event.changes.any { it.pressed })
                        scope.launch { CosIsland.press(press, false) }
                    }
                }
                .drawWithContent {
                    drawContent()
                }
    ) {
        Layout(
            content = {
                Box(
                    Modifier.wrapContentSize()
                        .onSizeChanged { if (key == shownKey) natural = it }
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            this.alpha = alpha.value
                            val radius = blur.value
                            renderEffect =
                                if (radius > 0.5f) BlurEffect(radius, radius, TileMode.Decal)
                                else null
                        }
                ) {
                    body()
                }
            },
            modifier =
                Modifier.graphicsLayer {
                        this.shape = shape
                        clip = true
                    }
                    .drawWithContent {
                        drawRoundRect(
                            Color.Black,
                            cornerRadius = CornerRadius(size.height / 2f),
                        )
                        drawContent()
                        val stroke = CosIsland.StrokeWidth.toPx()
                        if (size.minDimension > stroke * 2) {
                            val rimBrush =
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors =
                                        listOf(
                                            Color(0x88FFFFFF),
                                            CosIsland.StrokeColor,
                                            Color(0x5570B8FF),
                                        )
                                )
                            drawRoundRect(
                                brush = rimBrush,
                                topLeft = Offset(stroke / 2f, stroke / 2f),
                                size = Size(size.width - stroke, size.height - stroke),
                                cornerRadius = CornerRadius((size.height - stroke) / 2f),
                                style = Stroke(stroke),
                            )
                        }
                    },
        ) { measurables, _ ->
            val placeable =
                measurables.first().measure(Constraints(maxWidth = maxWidthPx))
            val w = width.value.roundToInt().coerceAtLeast(0)
            val h = height.value.roundToInt().coerceAtLeast(0)
            layout(w, h) {
                placeable.place((w - placeable.width) / 2, (h - placeable.height) / 2)
            }
        }
    }
}

private fun drawMoons(scope: androidx.compose.ui.graphics.drawscope.DrawScope, alpha: Float) =
    with(scope) {
        val r = size.height / 2f
        val gap = CosIsland.MoonGap.toPx()
        val thick = CosIsland.MoonWidth.toPx()
        for (side in listOf(-1f, 1f)) {
            val edge = if (side < 0) r else size.width - r
            val inner = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(Offset(edge + side * gap, r), r))
            }
            val outer = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(Offset(edge + side * (gap + thick), r), r))
            }
            val moon = Path().apply { op(outer, inner, PathOperation.Difference) }
            drawPath(moon, CosIsland.MoonColor, alpha = alpha)
        }
    }
