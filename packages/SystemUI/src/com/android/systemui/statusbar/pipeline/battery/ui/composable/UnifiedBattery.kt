/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.pipeline.battery.ui.composable

import android.content.Context
import android.database.ContentObserver
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastFirstOrNull
import com.android.systemui.common.ui.compose.load
import com.android.systemui.compose.modifiers.sysuiResTag
import com.android.systemui.statusbar.phone.domain.interactor.IsAreaDark
import com.android.systemui.statusbar.pipeline.battery.shared.ui.BatteryColors
import com.android.systemui.statusbar.pipeline.battery.shared.ui.BatteryFrame
import com.android.systemui.statusbar.pipeline.battery.shared.ui.BatteryGlyph
import com.android.systemui.statusbar.pipeline.battery.shared.ui.PathSpec
import com.android.systemui.statusbar.pipeline.battery.ui.viewmodel.BatteryViewModel
import com.android.systemui.statusbar.pipeline.battery.ui.viewmodel.BatteryViewModel.Companion.glyphRepresentation
import kotlin.math.ceil
import kotlin.math.roundToInt

private fun readCustomBatteryInt(context: Context, key: String, defaultVal: Int): Int {
    val cr = context.contentResolver
    try {
        val v = Settings.System.getIntForUser(cr, key, -999, UserHandle.USER_CURRENT)
        if (v != -999) return v
    } catch (_: Throwable) {}
    try {
        val res: Bundle? = cr.call(Uri.parse("content://lineageos.platform/system"), "GET_system", key, Bundle())
        val s = res?.getString("value")
        if (!s.isNullOrEmpty()) return s.toInt()
    } catch (_: Throwable) {}
    return defaultVal
}

/**
 * Draws a battery directly on to a [Canvas]. The canvas is scaled to fill its container, and the
 * resulting battery is scaled using a FIT_CENTER type scaling that preserves the aspect ratio.
 */
@Composable
fun BatteryCanvas(
    path: PathSpec,
    innerWidth: Float,
    innerHeight: Float,
    glyphs: List<BatteryGlyph>,
    level: Int?,
    isFull: Boolean,
    colorsProvider: () -> BatteryColors,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
) {

    val totalWidth by
        remember(glyphs) {
            mutableFloatStateOf(
                if (glyphs.isEmpty()) {
                    0f
                } else {
                    // Pads in between each glyph, skipping the first
                    glyphs.drop(1).fold(glyphs.first().width) { acc: Float, next: BatteryGlyph ->
                        acc + INTER_GLYPH_PADDING_PX + next.width
                    }
                }
            )
        }

    Canvas(
        modifier = modifier.fillMaxSize().sysuiResTag(BatteryViewModel.TEST_TAG),
        contentDescription = contentDescription,
    ) {
        val scale = path.scaleTo(size.width, size.height)
        val colors = colorsProvider()

        scale(scale, pivot = Offset.Zero) {
            if (isFull) {
                // Saves a layer since we don't need background here
                drawPath(path = path.path, color = colors.fill)
            } else {
                // First draw the body
                val bgColor =
                    if (glyphs.isEmpty()) {
                        colors.backgroundOnly
                    } else {
                        colors.backgroundWithGlyph
                    }
                drawPath(path.path, bgColor)
                // Then draw the body, clipped to the fill level
                if (level != null && level > 0) {
                    clipRect(0f, 0f, level.scaledLevel(), innerHeight) {
                        drawRoundRect(
                            color = colors.fill,
                            topLeft = Offset.Zero,
                            size = Size(width = innerWidth, height = innerHeight),
                            cornerRadius = CornerRadius(2f),
                        )
                    }
                }
            }

            // Now draw the glyphs
            var horizontalOffset = (BatteryFrame.innerWidth - totalWidth) / 2
            for (glyph in glyphs) {
                // Move the glyph to the right spot
                val verticalOffset = (BatteryFrame.innerHeight - glyph.height) / 2
                inset(
                    // Never try and inset more than half of the available size - see b/400246091.
                    minOf(horizontalOffset, size.width / 2),
                    minOf(verticalOffset, size.height / 2),
                ) {
                    glyph.draw(this, colors)
                }

                horizontalOffset += glyph.width + INTER_GLYPH_PADDING_PX
            }
        }
    }
}

// Experimentally-determined value
private const val INTER_GLYPH_PADDING_PX = 0.8f

/** Calculate the right-edge of the clip for the fill-rect, based on a level of [0-100] */
private fun Int.scaledLevel(): Float {
    val endSide = BatteryFrame.innerWidth
    return ceil((toFloat() / 100f) * endSide)
}

/**
 * A battery icon that will optionally display the percentage inside. Battery state attributions are
 * layered on top with a cutout path around them for visibility.
 *
 * This icon is designed to be parameterized on the height. The only valid way to use it is by
 * explicitly setting `Modifier.height()`, and using `Modifier.wrapContentWidth()` together.
 */
@Composable
fun UnifiedBattery(
    viewModel: BatteryViewModel,
    isDarkProvider: () -> IsAreaDark,
    modifier: Modifier,
) {
    val context = LocalContext.current
    var bounds by remember { mutableStateOf(Rect()) }
    var batteryStyle by remember {
        mutableIntStateOf(readCustomBatteryInt(context, "status_bar_battery_style", 0))
    }
    var percentMode by remember {
        mutableIntStateOf(readCustomBatteryInt(context, "status_bar_show_battery_percent", -1))
    }

    DisposableEffect(context) {
        val cr = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                batteryStyle = readCustomBatteryInt(context, "status_bar_battery_style", 0)
                percentMode = readCustomBatteryInt(context, "status_bar_show_battery_percent", -1)
            }
        }
        try {
            cr.registerContentObserver(
                Settings.System.getUriFor("status_bar_battery_style"),
                false,
                observer,
                UserHandle.USER_ALL
            )
            cr.registerContentObserver(
                Settings.System.getUriFor("status_bar_show_battery_percent"),
                false,
                observer,
                UserHandle.USER_ALL
            )
            cr.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SHOW_BATTERY_PERCENT),
                false,
                observer,
                UserHandle.USER_ALL
            )
            cr.registerContentObserver(
                Uri.parse("content://lineageos.platform/system"),
                true,
                observer,
                UserHandle.USER_ALL
            )
        } catch (_: Throwable) {}
        onDispose {
            try {
                cr.unregisterContentObserver(observer)
            } catch (_: Throwable) {}
        }
    }

    val colorProvider = {
        if (isDarkProvider().isDarkTheme(bounds)) {
            viewModel.colorProfile.dark
        } else {
            viewModel.colorProfile.light
        }
    }

    val currentLevel = viewModel.level ?: 0
    val effectiveGlyphsProvider: () -> List<BatteryGlyph> = {
        when (percentMode) {
            0, 2 -> emptyList()
            1 -> currentLevel.glyphRepresentation()
            else -> viewModel.glyphList
        }
    }
    val showTextNext = (percentMode == 2) && (batteryStyle != 4) && (batteryStyle != 5)

    Row(
        modifier = modifier
            .sysuiResTag(BatteryViewModel.TEST_TAG)
            .onLayoutRectChanged { relativeLayoutBounds ->
                bounds = with(relativeLayoutBounds.boundsInScreen) { Rect(left, top, right, bottom) }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        when (batteryStyle) {
            5 -> {
                // Hidden battery icon
                Spacer(modifier = Modifier.width(0.dp))
            }
            4 -> {
                // Text-only battery style
                val colors = colorProvider()
                val prefix = if (viewModel.isCharging) "⚡" else ""
                Text(
                    text = "$prefix${currentLevel}%",
                    color = colors.fill,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
            6 -> {
                // Portrait (vertical) battery icon
                PortraitBatteryIcon(
                    level = currentLevel,
                    isCharging = viewModel.isCharging,
                    colorsProvider = colorProvider,
                )
            }
            1, 2, 3, 8, 9 -> {
                // Circle (1/8), Dotted Circle (2/9), Filled Circle (3)
                CircleBatteryIcon(
                    level = currentLevel,
                    style = batteryStyle,
                    isCharging = viewModel.isCharging,
                    showPercentInside = (percentMode == 1),
                    colorsProvider = colorProvider,
                )
            }
            else -> {
                BatteryLayout(
                    attribution = viewModel.attribution,
                    levelProvider = { viewModel.level },
                    isFullProvider = { viewModel.isFull },
                    glyphsProvider = effectiveGlyphsProvider,
                    colorsProvider = colorProvider,
                    modifier = Modifier.fillMaxHeight(),
                    contentDescription = viewModel.contentDescription.load() ?: "",
                )
            }
        }

        if (showTextNext) {
            val colors = colorProvider()
            Text(
                text = "${currentLevel}%",
                color = colors.fill,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PortraitBatteryIcon(
    level: Int,
    isCharging: Boolean,
    colorsProvider: () -> BatteryColors,
) {
    Canvas(modifier = Modifier.fillMaxHeight().width(10.dp)) {
        val colors = colorsProvider()
        val w = size.width
        val h = size.height
        val capW = w * 0.46f
        val capH = h * 0.10f
        val bodyTop = capH + h * 0.04f
        val bodyH = h - bodyTop
        val corner = w * 0.24f

        // Cap
        drawRoundRect(
            color = if (level >= 99) colors.fill else colors.backgroundOnly,
            topLeft = Offset((w - capW) / 2f, 0f),
            size = Size(capW, capH),
            cornerRadius = CornerRadius(capH * 0.4f, capH * 0.4f),
        )
        // Body background
        drawRoundRect(
            color = colors.backgroundOnly,
            topLeft = Offset(0f, bodyTop),
            size = Size(w, bodyH),
            cornerRadius = CornerRadius(corner, corner),
        )
        // Fill from bottom up
        val clamped = level.coerceIn(0, 100) / 100f
        if (clamped > 0f) {
            val fillTop = bodyTop + bodyH * (1f - clamped)
            clipRect(left = 0f, top = fillTop, right = w, bottom = h) {
                drawRoundRect(
                    color = colors.fill,
                    topLeft = Offset(0f, bodyTop),
                    size = Size(w, bodyH),
                    cornerRadius = CornerRadius(corner, corner),
                )
            }
        }
        if (isCharging) {
            val bolt = BatteryGlyph.Bolt
            val s = (bodyH * 0.62f) / bolt.height
            inset((w - bolt.width * s) / 2f, bodyTop + (bodyH - bolt.height * s) / 2f) {
                scale(s, pivot = Offset.Zero) {
                    drawPath(bolt.path, color = colors.attribution)
                }
            }
        }
    }
}

@Composable
private fun CircleBatteryIcon(
    level: Int,
    style: Int,
    isCharging: Boolean,
    showPercentInside: Boolean,
    colorsProvider: () -> BatteryColors,
) {
    Canvas(modifier = Modifier.fillMaxHeight().width(15.dp)) {
        val colors = colorsProvider()
        val d = minOf(size.width, size.height)
        val strokeW = d * 0.16f
        val radius = (d - strokeW) / 2f
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val clamped = level.coerceIn(0, 100) / 100f

        if (style == 3) {
            // Filled circle
            drawCircle(
                color = colors.backgroundOnly,
                radius = d / 2f,
                center = centerOffset,
            )
            if (clamped > 0f) {
                drawCircle(
                    color = colors.fill,
                    radius = (d / 2f) * clamped,
                    center = centerOffset,
                )
            }
        } else {
            // Ring / Dotted Ring
            drawCircle(
                color = colors.backgroundOnly,
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokeW),
            )
            if (clamped > 0f) {
                val strokeStyle = if (style == 2 || style == 9) {
                    Stroke(
                        width = strokeW,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(strokeW * 1.35f, strokeW * 0.95f), 0f)
                    )
                } else {
                    Stroke(width = strokeW, cap = StrokeCap.Round)
                }
                drawArc(
                    color = colors.fill,
                    startAngle = -90f,
                    sweepAngle = 360f * clamped,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = strokeStyle,
                )
            }
        }

        if (isCharging) {
            val bolt = BatteryGlyph.Bolt
            val s = (d * 0.48f) / bolt.height
            inset(centerOffset.x - (bolt.width * s) / 2f, centerOffset.y - (bolt.height * s) / 2f) {
                scale(s, pivot = Offset.Zero) {
                    drawPath(bolt.path, color = if (style == 3) colors.attribution else colors.fill)
                }
            }
        } else if (showPercentInside && level < 100) {
            val glyphs = level.glyphRepresentation()
            val totalW = glyphs.fold(0f) { acc, g -> acc + g.width } + (glyphs.size - 1).coerceAtLeast(0) * 0.6f
            val maxH = glyphs.maxOfOrNull { it.height } ?: 1f
            val s = (d * 0.42f) / maxH
            var xOff = centerOffset.x - (totalW * s) / 2f
            for (g in glyphs) {
                val yOff = centerOffset.y - (g.height * s) / 2f
                inset(xOff, yOff) {
                    scale(s, pivot = Offset.Zero) {
                        drawPath(g.path, color = if (style == 3) colors.attribution else colors.fill)
                    }
                }
                xOff += (g.width + 0.6f) * s
            }
        }
    }
}

@Composable
fun BatteryLayout(
    attribution: BatteryGlyph?,
    levelProvider: () -> Int?,
    isFullProvider: () -> Boolean,
    glyphsProvider: () -> List<BatteryGlyph>,
    colorsProvider: () -> BatteryColors,
    modifier: Modifier,
    contentDescription: String = "",
) {
    Layout(
        content = {
            BatteryBody(
                pathSpec = BatteryFrame.bodyPathSpec,
                levelProvider = levelProvider,
                glyphsProvider = glyphsProvider,
                isFullProvider = isFullProvider,
                colorsProvider = colorsProvider,
                modifier = Modifier.layoutId(BatteryMeasurePolicy.LayoutId.Frame),
                contentDescription = contentDescription,
            )
            if (attribution != null) {
                BatteryAttribution(
                    attr = attribution,
                    colorsProvider = colorsProvider,
                    modifier =
                        Modifier.layoutId(
                            BatteryMeasurePolicy.LayoutId.Attribution(wrapped = attribution)
                        ),
                )
            } else {
                BatteryCap(
                    colorsProvider = colorsProvider,
                    isFullProvider = isFullProvider,
                    glyphsProvider = glyphsProvider,
                    modifier = Modifier.layoutId(BatteryMeasurePolicy.LayoutId.Cap),
                )
            }
        },
        measurePolicy = BatteryMeasurePolicy(),
        // [Offscreen] Enables the BlendMode.Clear usage for the battery attribution
        modifier = modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    )
}

class BatteryMeasurePolicy : MeasurePolicy {
    sealed class LayoutId {
        data object Frame : LayoutId()

        data object Cap : LayoutId()

        // We don't have to depend on the whole [BatteryGlyph] here, we just need to know the
        // size so we can scale and measure appropriately
        data class Attribution(val wrapped: BatteryGlyph) : LayoutId()
    }

    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val batteryFrame = measurables.fastFirst { it.layoutId == LayoutId.Frame }

        // We will scale the entire battery icon based on the given height
        val scale = constraints.maxHeight / BatteryFrame.innerHeight

        val batterySize = BatteryFrame.bodyPathSpec.scaledSize(scale)
        val batteryFramePlaceable =
            batteryFrame.measure(
                constraints =
                    constraints.copy(
                        minWidth = batterySize.width.roundToInt(),
                        maxWidth = batterySize.width.roundToInt(),
                        minHeight = batterySize.height.roundToInt(),
                        maxHeight = batterySize.height.roundToInt(),
                    )
            )

        val cap = measurables.fastFirstOrNull { it.layoutId == LayoutId.Cap }
        val capPlaceable = run {
            cap?.let {
                val size = BatteryFrame.capPathSpec.scaledSize(scale)
                val w = size.width.roundToInt()
                val h = size.height.roundToInt()
                it.measure(
                    constraints =
                        constraints.copy(minWidth = w, maxWidth = w, minHeight = h, maxHeight = h)
                )
            }
        }

        val attr = measurables.fastFirstOrNull { it.layoutId is LayoutId.Attribution }
        val attrPlaceable = run {
            attr?.let {
                val ps = (it.layoutId as LayoutId.Attribution).wrapped
                val size = ps.scaledSize(scale)
                val w = size.width.roundToInt()
                val h = size.height.roundToInt()
                it.measure(
                    constraints =
                        constraints.copy(minWidth = w, maxWidth = w, minHeight = h, maxHeight = h)
                )
            }
        }

        var totalWidth: Int = batteryFramePlaceable.width
        if (attrPlaceable != null) {
            totalWidth += (attrPlaceable.width * 0.8).roundToInt()
        } else if (capPlaceable != null) {
            // 1dp of padding * scale for the cap
            totalWidth += capPlaceable.width + scale.roundToInt()
        }
        val totalHeight = batterySize.height.roundToInt()
        return layout(totalWidth, totalHeight) {
            if (layoutDirection == LayoutDirection.Rtl) {
                val (offsetX, placeable) =
                    when {
                        // Attr overlaps the battery frame by 20% of its own width
                        attrPlaceable != null ->
                            (attrPlaceable.width * (1 - attrOverlap)).roundToInt() to attrPlaceable

                        // Cap has exactly 1dp (scaled) of space after it
                        capPlaceable != null ->
                            capPlaceable.width + scale.roundToInt() to capPlaceable

                        else -> 0 to null
                    }

                // Place the battery frame first so the layers are in the right order
                batteryFramePlaceable.place(offsetX, 0)

                // Then place the cap or attribution. In RTL, it always is left-aligned
                placeable?.apply {
                    placeCenteredVertically(
                        placeable = this,
                        containerHeight = batteryFramePlaceable.height,
                        xOffset = 0,
                    )
                }
            } else {
                batteryFramePlaceable.place(0, 0)

                val (xOffset, placeable) =
                    when {
                        attrPlaceable != null ->
                            (batteryFramePlaceable.width - (attrOverlap * attrPlaceable.width))
                                .roundToInt() to attrPlaceable
                        capPlaceable != null ->
                            (batteryFramePlaceable.width + scale.roundToInt()) to capPlaceable
                        else -> 0 to null
                    }

                placeable?.apply {
                    placeCenteredVertically(
                        placeable = this,
                        containerHeight = batteryFramePlaceable.height,
                        xOffset = xOffset,
                    )
                }
            }
        }
    }

    private fun Placeable.PlacementScope.placeCenteredVertically(
        placeable: Placeable,
        containerHeight: Int,
        xOffset: Int,
    ) {
        placeable.place(x = xOffset, y = placeable.centerYOffset(containerHeight))
    }

    private fun Placeable.centerYOffset(outerHeight: Int) =
        ((outerHeight - height) / 2f).roundToInt()

    companion object {
        // Overlap the attribution by 20%
        private const val attrOverlap = 0.2f
    }
}

/**
 * Draws just the round-rect piece of the battery frame. If [glyphsProvider] is non-empty, then this
 * composable also renders the glyphs centered in the frame.
 *
 * Always shows the fill amount, clipped to the given [levelProvider]
 */
@Composable
fun BatteryBody(
    pathSpec: PathSpec,
    levelProvider: () -> Int?,
    glyphsProvider: () -> List<BatteryGlyph>,
    isFullProvider: () -> Boolean,
    colorsProvider: () -> BatteryColors,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
) {
    Canvas(modifier = modifier, contentDescription = contentDescription) {
        val rtl = layoutDirection == LayoutDirection.Rtl

        val level = levelProvider()
        val colors = colorsProvider()
        val glyphs = glyphsProvider()
        val isFull = isFullProvider()
        val frameColor =
            getBatteryFrameColor(isFull = isFull, hasGlyphs = glyphs.isNotEmpty(), colors = colors)

        val totalGlyphWidth =
            if (glyphs.isEmpty()) {
                0f
            } else {
                // Pads in between each glyph, skipping the first
                glyphs.drop(1).fold(glyphs.first().width) { acc: Float, next: BatteryGlyph ->
                    acc + INTER_GLYPH_PADDING_PX + next.width
                }
            }

        val s = pathSpec.scaleTo(size.width, size.height)
        scale(scale = s, pivot = Offset.Zero) {
            if (isFull) {
                // If full, the frameColor is already the fill color.
                drawPath(pathSpec.path, frameColor)
            } else {
                // 1. Draw body background
                drawPath(pathSpec.path, frameColor)

                // 2. clip the fill to the level if we have it
                if (level != null && level > 0) {
                    clipRect(
                        left = if (!rtl) 0f else BatteryFrame.innerWidth - level.scaledLevel(),
                        top = 0f,
                        right = if (!rtl) level.scaledLevel() else BatteryFrame.innerWidth,
                        bottom = BatteryFrame.innerHeight,
                    ) {
                        // 3. Draw the rounded rect fill fully, it'll be clipped above
                        drawRoundRect(
                            color = colors.fill,
                            topLeft = Offset.Zero,
                            size =
                                Size(
                                    width = BatteryFrame.innerWidth,
                                    height = BatteryFrame.innerHeight,
                                ),
                            CornerRadius(x = BatteryFrame.cornerRadius),
                        )
                    }
                }
            }

            // Next: draw the glyphs
            var horizontalOffset = (BatteryFrame.innerWidth - totalGlyphWidth) / 2f
            for (glyph in glyphs) {
                // Move the glyph to the right spot
                val verticalOffset = (BatteryFrame.innerHeight - glyph.height) / 2
                inset(
                    // Never try and inset more than half of the available size - see b/400246091.
                    minOf(horizontalOffset, size.width / 2),
                    minOf(verticalOffset, size.height / 2),
                ) {
                    glyph.draw(this, colors)
                }
                horizontalOffset += glyph.width + INTER_GLYPH_PADDING_PX
            }
        }
    }
}

@Composable
fun BatteryCap(
    colorsProvider: () -> BatteryColors,
    isFullProvider: () -> Boolean,
    glyphsProvider: () -> List<BatteryGlyph>,
    modifier: Modifier = Modifier,
) {
    val pathSpec = BatteryFrame.capPathSpec
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Canvas(modifier = modifier.scale(scaleX = if (rtl) -1f else 1f, scaleY = 1f)) {
        val colors = colorsProvider()
        val isFull = isFullProvider()
        val hasGlyphs = glyphsProvider().isNotEmpty()
        val color = getBatteryFrameColor(isFull = isFull, hasGlyphs = hasGlyphs, colors = colors)

        val s = pathSpec.scaleTo(size.width, size.height)
        scale(s, pivot = Offset.Zero) { drawPath(pathSpec.path, color = color) }
    }
}

@Composable
fun BatteryAttribution(
    attr: BatteryGlyph,
    colorsProvider: () -> BatteryColors,
    modifier: Modifier = Modifier,
) {
    val stroke = remember { Stroke(width = 2f) }
    // Do not RTL the attribution, because they are text-like. '?' shouldn't be flipped, for example
    Canvas(modifier = modifier) {
        val s = attr.scaleTo(size.width, size.height)
        val colors = colorsProvider()
        scale(s, pivot = Offset.Zero) {
            drawPath(
                path = attr.path,
                color = Color.Black,
                style = stroke,
                blendMode = BlendMode.Clear,
            )
            drawPath(attr.path, color = colors.attribution)
        }
    }
}

/** Determines the correct color for the battery frame (body and cap) based on its state. */
private fun getBatteryFrameColor(
    isFull: Boolean,
    hasGlyphs: Boolean,
    colors: BatteryColors,
): Color {
    return when {
        isFull -> colors.fill
        hasGlyphs -> colors.backgroundWithGlyph
        else -> colors.backgroundOnly
    }
}
