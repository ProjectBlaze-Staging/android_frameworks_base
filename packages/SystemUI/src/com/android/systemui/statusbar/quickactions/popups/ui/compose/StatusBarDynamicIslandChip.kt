/*
 * Copyright (C) 2026 The Android Open Source Project
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

import android.view.DisplayCutout
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.font.FontWeight
import com.android.systemui.qs.composefragment.cosStatusGlow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.systemui.common.shared.model.Icon as IconModel
import com.android.systemui.common.ui.compose.Icon
import com.android.systemui.res.R
import com.android.systemui.statusbar.quickactions.popups.shared.DynamicIslandFeatureSettings
import com.android.systemui.statusbar.quickactions.popups.shared.DynamicIslandFeatureSettings.observeDynamicIslandScale
import com.android.systemui.statusbar.quickactions.popups.ui.model.PopupChipModel
import com.android.systemui.statusbar.quickactions.popups.ui.model.PopupContentModel
import com.android.systemui.statusbar.quickactions.popups.ui.model.SystemEventKind
import com.android.systemui.statusbar.quickactions.screenrecord.shared.model.ScreenRecordPopupModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import com.android.systemui.res.R as PenguinR
import kotlinx.coroutines.launch

/** Single centered status bar capsule styled like a compact dynamic island. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatusBarDynamicIslandChip(
    viewModel: PopupChipModel.Shown,
    pageCount: Int,
    cutoutSpec: DynamicIslandCutoutSpec,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    onChipBoundsChanged: (Rect) -> Unit = {},
    cos: Boolean = false,
) {
    val isMediaChip = viewModel.popupContent is PopupContentModel.Media
    val chipShape = RoundedCornerShape(50)
    val colors = viewModel.colors
    val (widthScale, heightScale) = rememberDynamicIslandSizeScale()
    val chipContentColor =
        colors.chipContent(
            isPopupShown = viewModel.isPopupShown,
            colorScheme = MaterialTheme.colorScheme,
        )
    val chipOutline =
        colors.chipOutline(
            isPopupShown = viewModel.isPopupShown,
            colorScheme = MaterialTheme.colorScheme,
        )
    val view = LocalView.current
    val boundsModifier =
        Modifier.onGloballyPositioned { coordinates ->
            onChipBoundsChanged(coordinates.boundsInScreen(view))
        }
    val hapticOnTap: () -> Unit = {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        onTap()
    }
    val mediaOpenApp: (() -> Unit)? =
        (viewModel.popupContent as? PopupContentModel.Media)
            ?.takeIf { it.model.isPlaying }
            ?.model
            ?.openApp
    val hapticOnLongPress: () -> Unit = {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        mediaOpenApp?.invoke()
    }
    if (!cos && viewModel.popupContent.isUtilityStatusContent() && viewModel.icons.isNotEmpty()) {
        UtilityStatusIslandChip(
            viewModel = viewModel,
            onTap = hapticOnTap,
            cutoutSpec = cutoutSpec,
            widthScale = widthScale,
            heightScale = heightScale,
            chipContentColor = chipContentColor,
            chipOutline = chipOutline,
            modifier = modifier.then(boundsModifier),
        )
        return
    }

    val compactWidth = compactIslandWidthFor(viewModel.popupContent)?.times(widthScale)
    val hasInlineTimer = viewModel.popupContent is PopupContentModel.Stopwatch
    val trailingDecorationWidth =
        when (val popupContent = viewModel.popupContent) {
            is PopupContentModel.Media ->
                if (popupContent.model.isPlaying) {
                    14.dp
                } else if (pageCount > 1) {
                    11.dp
                } else {
                    0.dp
                }
            is PopupContentModel.ScreenRecord -> 11.dp
            else -> {
                if (pageCount > 1) 11.dp else 0.dp
            }
        }
    val leadingDecorationWidth =
        when {
            viewModel.icons.isEmpty() -> 0.dp
            else -> 18.dp + (8.dp * (viewModel.icons.size - 1))
        }
    val maxTextWidth =
        ((CompactIslandMaxWidth * widthScale) - 24.dp - leadingDecorationWidth - trailingDecorationWidth)
            .coerceAtLeast(56.dp)

    val collapseState = rememberDynamicIslandCollapseState(viewModel.isPopupShown)
    val cosMedia = (viewModel.popupContent as? PopupContentModel.Media)?.takeIf { cos }?.model
    val cosArtTint =
        if (cosMedia != null) {
            rememberCosArtTint(
                remember(cosMedia.artworkIcon) {
                    (cosMedia.artworkIcon as? IconModel.Loaded)?.drawable
                }
            )
        } else {
            Color.Transparent
        }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 900f),
            label = "chipPressScale",
        )

    // COS chips sit centred on the camera; keep their icon and text on either side of it.
    val cosCameraGap =
        cutoutSpec.embeddedGapWidth.takeIf {
            cos && cutoutSpec.cutoutCenterX != null
        }

    CameraSplitRow(
        leadingCount = viewModel.icons.size,
        cameraGap = cosCameraGap,
        modifier =
            modifier
                .then(boundsModifier)
                .then(
                    if (cos) {
                        Modifier.height(CosIsland.height(cutoutSpec.holeSize))
                            .widthIn(min = CosIsland.MinWidth, max = CosIsland.MaxWidth)
                            .background(cosArtTint)
                    } else {
                        Modifier.defaultMinSize(minHeight = 32.dp * heightScale)
                            .widthIn(
                                min = compactWidth ?: 0.dp,
                                max = compactWidth ?: (CompactIslandMaxWidth * widthScale),
                            )
                            .graphicsLayer {
                                scaleX = collapseState.scaleX * pressScale
                                scaleY = collapseState.scaleY * pressScale
                            }
                            .clip(chipShape)
                            .background(Color.Black)
                            .cosStatusGlow(viewModel.statusGlowKey(), IslandGlowCorner, capsule = true)
                            .border(width = 1.dp, color = chipOutline, shape = chipShape)
                    }
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = hapticOnTap,
                    onLongClick = mediaOpenApp?.let { { hapticOnLongPress() } },
                )
                .then(
                    if (cos) {
                        Modifier.padding(horizontal = CosIsland.Padding)
                    } else {
                        Modifier.padding(
                                start = 8.dp * widthScale,
                                end = 12.dp * widthScale,
                                top = 7.dp * heightScale,
                                bottom = 7.dp * heightScale,
                            )
                            .graphicsLayer { alpha = collapseState.contentAlpha }
                    }
                ),
        horizontalArrangement =
            if (isMediaChip) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        viewModel.icons.forEachIndexed { index, chipIcon ->
            val isArtworkLike =
                index == 0 &&
                    (viewModel.popupContent is PopupContentModel.Media ||
                        viewModel.popupContent is PopupContentModel.LiveScore)
            val cosArtwork =
                (cosMedia?.artworkIcon as? IconModel.Loaded)?.drawable?.takeIf { isArtworkLike }
            if (cosArtwork != null) {
                CosCapsuleArtwork(cosArtwork, cosMedia.artworkIcon, CosIsland.IconSize)
                return@forEachIndexed
            }
            val media = viewModel.popupContent as? PopupContentModel.Media
            if (!cos && isArtworkLike && media != null) {
                MediaArtFlip(track = media.model.songName?.toString(), artwork = chipIcon.icon) { icon ->
                    Icon(
                        icon = icon,
                        modifier = Modifier.size(18.dp).clip(CircleShape),
                        tint = Color.Unspecified,
                    )
                }
                return@forEachIndexed
            }
            Icon(
                icon = chipIcon.icon,
                modifier = Modifier
                    .size(
                        when {
                            cos && isArtworkLike -> CosIsland.IconSize
                            cos -> CosIsland.IconSize * 0.8f
                            isArtworkLike -> 18.dp
                            else -> 16.dp
                        }
                    )
                    .then(
                        if (isArtworkLike) {
                            Modifier.clip(CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .then(
                        if (isArtworkLike && cosMedia != null) {
                            Modifier.cosAlbumShake(cosMedia.artworkIcon)
                        } else {
                            Modifier
                        }
                    ),
                tint = if (isArtworkLike || !chipIcon.tint) Color.Unspecified else chipContentColor,
            )
        }

        rememberChipText(viewModel)
            ?.takeIf {
                !isMediaChip &&
                    viewModel.popupContent !is PopupContentModel.ScreenRecord &&
                    !hasInlineTimer &&
                    it.isNotBlank()
            }
            ?.let { text ->
                Text(
                    text = text,
                    style =
                        if (cos) {
                            MaterialTheme.typography.labelLarge.copy(
                                fontSize = CosIsland.TextSize,
                                fontWeight = FontWeight.Medium,
                            )
                        } else {
                            MaterialTheme.typography.labelLarge
                        },
                    color = if (cos) CosIsland.TextColor else chipContentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = maxTextWidth),
                )
            }

        when (val popupContent = viewModel.popupContent) {
            is PopupContentModel.Media -> {
                val artworkDrawable = remember(popupContent.model.artworkIcon) {
                    (popupContent.model.artworkIcon as? IconModel.Loaded)?.drawable
                }
                if (cos) {
                    CosVoicePrint(playing = popupContent.model.isPlaying, size = CosIsland.IconSize)
                } else if (popupContent.model.isPlaying) {
                    AudioReactiveBars(
                        isPlaying = true,
                        color = IslandAccents.Music,
                        artworkDrawable = artworkDrawable,
                    )
                } else if (pageCount > 1) {
                    SwipeHint(color = chipContentColor.copy(alpha = 0.72f))
                }
            }
            is PopupContentModel.ScreenRecord ->
                when (val model = popupContent.model) {
                    is ScreenRecordPopupModel.Starting ->
                        StatusContent(
                            text = viewModel.chipText.orEmpty(),
                            color = chipContentColor,
                            showSwipeHint = pageCount > 1,
                        )
                    is ScreenRecordPopupModel.Recording ->
                        StatusContent(
                            text = viewModel.chipText.orEmpty(),
                            color = chipContentColor,
                            showSwipeHint = pageCount > 1,
                        )
                }
            is PopupContentModel.Stopwatch ->
                StatusContent(
                    text =
                        rememberStopwatchText(popupContent.model),
                    color = chipContentColor,
                    showSwipeHint = pageCount > 1,
                )
            else -> {
                if (pageCount > 1) {
                    SwipeHint(color = chipContentColor.copy(alpha = 0.72f))
                }
            }
        }
    }
}

@Composable
private fun UtilityStatusIslandChip(
    viewModel: PopupChipModel.Shown,
    onTap: () -> Unit,
    cutoutSpec: DynamicIslandCutoutSpec,
    widthScale: Float = 1f,
    heightScale: Float = 1f,
    chipContentColor: Color,
    chipOutline: Color,
    modifier: Modifier = Modifier,
) {
    val collapseState = rememberDynamicIslandCollapseState(viewModel.isPopupShown)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 900f),
            label = "utilityChipPressScale",
        )
    val isSystemEvent = viewModel.popupContent is PopupContentModel.SystemEvent
    val isFlashlight = viewModel.popupContent is PopupContentModel.Flashlight
    val hasBalancedSegments = isSystemEvent || isFlashlight
    val deviceImage = (viewModel.popupContent as? PopupContentModel.SystemEvent)
        ?.takeIf { it.kind == SystemEventKind.Bluetooth }?.image
    val deviceImageSize = 20.dp * widthScale
    val deviceImageSpace = if (deviceImage != null) deviceImageSize + 6.dp * widthScale else 0.dp
    val omitCameraGap = when (val content = viewModel.popupContent) {
        is PopupContentModel.Stopwatch -> true
        is PopupContentModel.SystemEvent ->
            content.kind == SystemEventKind.Bluetooth ||
                content.kind == SystemEventKind.Charging ||
                content.kind == SystemEventKind.Unlock ||
                content.kind == SystemEventKind.Timer
        else -> false
    }
    val cameraGapWidth = if (omitCameraGap) 0.dp else cutoutSpec.embeddedGapWidth
    val liveChipText = rememberChipText(viewModel).orEmpty()
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textEndPadding =
        (when {
            isSystemEvent -> 12.dp
            isFlashlight -> 10.dp
            else -> 6.dp
        }) * widthScale
    val eventTextWidth = if (isSystemEvent) {
        val measured = textMeasurer.measure(liveChipText,
            style = MaterialTheme.typography.labelLarge, maxLines = 1)
        (with(density) { measured.size.width.toDp() } +
            6.dp * widthScale + textEndPadding + deviceImageSpace)
            .coerceAtMost(92.dp * widthScale)
    } else 0.dp
    val rightSegmentWidth =
        (when (viewModel.popupContent) {
            is PopupContentModel.Alarm -> 72.dp
            is PopupContentModel.SystemEvent -> 60.dp
            else -> 80.dp
        } * widthScale).coerceAtLeast(eventTextWidth)
    val connectedIslandWidth = if (isSystemEvent) {
        rightSegmentWidth * 2 + cameraGapWidth
    } else
        ((CompactUtilityConnectedIslandChromeWidth * widthScale) +
                cameraGapWidth +
                rightSegmentWidth)
            .coerceIn(
                (if (omitCameraGap) 0.dp else CompactUtilityConnectedIslandMinWidth) * widthScale,
                CompactUtilityConnectedIslandMaxWidth * widthScale,
            )
    val utilityText =
        when (val popupContent = viewModel.popupContent) {
            is PopupContentModel.ScreenRecord ->
                when (val model = popupContent.model) {
                    is ScreenRecordPopupModel.Starting -> "${model.secondsUntilStarted}s"
                    is ScreenRecordPopupModel.Recording ->
                        rememberElapsedDurationText(model.startElapsedRealtimeMs)
                }
            is PopupContentModel.Stopwatch ->
                rememberStopwatchText(popupContent.model)
            is PopupContentModel.Alarm -> viewModel.chipText.orEmpty()
            is PopupContentModel.Flashlight -> viewModel.chipText.orEmpty()
            is PopupContentModel.SystemEvent -> if (popupContent.kind == SystemEventKind.Timer)
                rememberTimerText(popupContent) else liveChipText
            else -> ""
        }
    Row(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = collapseState.scaleX * pressScale
                    scaleY = collapseState.scaleY * pressScale
                }
                .defaultMinSize(minHeight = 32.dp * heightScale)
                .then(
                    if (isFlashlight) {
                        Modifier.width(IntrinsicSize.Max)
                    } else {
                        Modifier.width(connectedIslandWidth)
                    }
                )
                .then(if (isSystemEvent) Modifier.semantics {
                    contentDescription = viewModel.contentDescription.orEmpty()
                } else Modifier)
                .clip(RoundedCornerShape(50))
                .background(Color.Black)
                .border(width = 1.dp, color = chipOutline, shape = RoundedCornerShape(50))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onTap,
                )
                .graphicsLayer { alpha = collapseState.contentAlpha },
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                (if (isFlashlight) {
                    Modifier.weight(1f)
                } else {
                    Modifier.width(if (isSystemEvent) rightSegmentWidth else 30.dp * widthScale)
                })
                    .padding(start = (if (isSystemEvent) 12.dp else 10.dp) * widthScale),
            contentAlignment = Alignment.CenterStart,
        ) {
            val chipIcon = viewModel.icons.first()
            if (isSystemEvent) {
                val event = viewModel.popupContent as PopupContentModel.SystemEvent
                if (event.kind == SystemEventKind.Charging) {
                    ChargingChipGlyph(
                        pulse = event.pulse,
                        modifier = Modifier.size(18.dp * widthScale),
                    )
                } else {
                    Icon(
                        icon = chipIcon.icon,
                        modifier = Modifier.size(18.dp * widthScale)
                            .then(
                                if (event.kind == SystemEventKind.Notification && !chipIcon.tint)
                                    Modifier.clip(CircleShape)
                                else Modifier
                            ),
                        tint = if (chipIcon.tint) chipContentColor else Color.Unspecified,
                    )
                }
            } else {
                CollapsedGlyphBadge(
                    icon = chipIcon.icon,
                    accent = islandChipAccentFor(viewModel.popupContent) ?: chipContentColor,
                    content = viewModel.popupContent,
                    badgeSize = 20.dp * widthScale,
                    iconSize = 13.dp * widthScale,
                )
            }
        }
        Spacer(modifier = Modifier.width(cameraGapWidth))
        Box(
            modifier =
                (if (isFlashlight) Modifier.weight(1f) else Modifier.width(rightSegmentWidth))
                    .padding(
                        start = 6.dp * widthScale,
                        top = 7.dp * heightScale,
                        bottom = 7.dp * heightScale,
                        end = textEndPadding,
                    ),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp * widthScale, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = utilityText,
                    style = MaterialTheme.typography.labelLarge,
                    color = chipContentColor,
                    maxLines = 1,
                    softWrap = !isFlashlight,
                    overflow = if (isFlashlight) TextOverflow.Clip else TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = if (isFlashlight) Modifier else Modifier.weight(1f),
                )
                deviceImage?.let { artwork ->
                    Icon(icon = artwork, tint = Color.Unspecified,
                        modifier = Modifier.size(deviceImageSize))
                }
            }
        }
        if (!hasBalancedSegments) Spacer(modifier = Modifier.width(10.dp * widthScale))
    }
}

private val IslandGlowCorner = 100.dp

private fun PopupChipModel.Shown.statusGlowKey(): Any =
    when (val content = popupContent) {
        is PopupContentModel.Media -> chipId to content.model.songName?.toString()
        is PopupContentModel.SystemEvent -> chipId to (content.kind to content.title)
        else -> chipId to content::class
    }

@Composable
private fun ChargingChipGlyph(pulse: Boolean, modifier: Modifier = Modifier) {
    val alpha = if (pulse) {
        val transition = rememberInfiniteTransition(label = "charging_chip")
        val animatedAlpha by transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse,
            ),
            label = "charging_bolt_alpha",
        )
        animatedAlpha
    } else 1f
    Icon(
        icon = IconModel.Resource(PenguinR.drawable.dynamic_island_charging, null),
        tint = IslandAccents.Battery,
        modifier = modifier.graphicsLayer { this.alpha = alpha },
    )
}

@Composable
private fun StatusContent(
    text: String,
    color: Color,
    showSwipeHint: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            maxLines = 1,
        )
        if (showSwipeHint) {
            SwipeHint(color = color.copy(alpha = 0.72f))
        }
    }
}

@Composable
private fun SwipeHint(color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(2) {
            Box(
                modifier = Modifier.size(width = 3.dp, height = 3.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}

/**
 * A [Row] that, given a [cameraGap], keeps the first [leadingCount] children before a centred gap
 * and the rest after it. Both sides take the same width so the gap stays over the camera.
 */
@Composable
private fun CameraSplitRow(
    leadingCount: Int,
    cameraGap: Dp?,
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    content: @Composable () -> Unit,
) {
    if (cameraGap == null) {
        Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
        ) {
            content()
        }
        return
    }
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val gapPx = cameraGap.roundToPx()
        val spacingPx = CameraSplitSpacing.roundToPx()
        val sideMaxPx = ((constraints.maxWidth - gapPx) / 2).coerceAtLeast(0)
        fun measureSide(items: List<Measurable>): List<Placeable> {
            var remaining = sideMaxPx
            return items.map { item ->
                item.measure(
                        Constraints(
                            maxWidth = remaining.coerceAtLeast(0),
                            maxHeight = constraints.maxHeight,
                        )
                    )
                    .also { remaining -= it.width + spacingPx }
            }
        }
        fun List<Placeable>.span() =
            if (isEmpty()) 0 else sumOf { it.width } + spacingPx * (size - 1)

        val leading = measureSide(measurables.take(leadingCount))
        val trailing = measureSide(measurables.drop(leadingCount))
        val side = maxOf(leading.span(), trailing.span())
        val width = (side * 2 + gapPx).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height =
            (leading + trailing).maxOfOrNull { it.height }
                ?.coerceIn(constraints.minHeight, constraints.maxHeight)
                ?: constraints.minHeight
        layout(width, height) {
            var x = 0
            leading.forEach {
                it.placeRelative(x, (height - it.height) / 2)
                x += it.width + spacingPx
            }
            x = width
            trailing.asReversed().forEach {
                x -= it.width
                it.placeRelative(x, (height - it.height) / 2)
                x -= spacingPx
            }
        }
    }
}

private val CameraSplitSpacing = 8.dp

private val CompactIslandMaxWidth = 192.dp
private val CompactMediaIslandWidth = 108.dp
private val CompactTimerIslandWidth = 116.dp
private val CompactRecordingIslandWidth = 88.dp
private val CompactAlarmIslandWidth = 92.dp
private val CompactUtilityIslandWidth = 74.dp
private val CompactUtilityConnectedIslandChromeWidth = 42.dp
private val CompactUtilityConnectedIslandMinWidth = 132.dp
private val CompactUtilityConnectedIslandMaxWidth = 188.dp
private val DynamicIslandEmbeddedGapFallbackWidth = 38.dp
private val DynamicIslandEmbeddedGapMinWidth = 34.dp
private val DynamicIslandEmbeddedGapMaxWidth = 88.dp
private val DynamicIslandEmbeddedGapSidePadding = 10.dp

internal val DynamicIslandCompanionDiameter = 34.dp
internal val DynamicIslandCompanionGap = 8.dp

data class DynamicIslandCutoutSpec(
    val embeddedGapWidth: Dp,
    val horizontalOffset: Dp,
    val cutoutCenterX: Float? = null,
    val holeSize: Dp = 20.dp,
)

@Composable
fun rememberDynamicIslandCutoutSpec(): DynamicIslandCutoutSpec {
    val density = LocalDensity.current
    val view = LocalView.current
    val displayCutout = view.rootWindowInsets?.displayCutout ?: view.display?.cutout
    val topCutout = displayCutout?.topBoundingRectOrNull()
    val rootWidthPx =
        when {
            view.rootView.width > 0 -> view.rootView.width
            view.width > 0 -> view.width
            else -> view.resources.configuration.windowConfiguration.maxBounds.width()
        }

    return with(density) {
        if (topCutout == null || rootWidthPx <= 0) {
            DynamicIslandCutoutSpec(
                embeddedGapWidth = DynamicIslandEmbeddedGapFallbackWidth,
                horizontalOffset = 0.dp,
            )
        } else {
            val embeddedGapWidthDp =
                (topCutout.width().toDp() + (DynamicIslandEmbeddedGapSidePadding * 2))
                    .coerceIn(DynamicIslandEmbeddedGapMinWidth, DynamicIslandEmbeddedGapMaxWidth)
            val horizontalOffsetDp = (topCutout.exactCenterX() - (rootWidthPx / 2f)).toDp()
            DynamicIslandCutoutSpec(
                embeddedGapWidth = embeddedGapWidthDp,
                horizontalOffset = horizontalOffsetDp,
                cutoutCenterX = topCutout.exactCenterX(),
                holeSize = topCutout.width().toDp(),
            )
        }
    }
}

private fun DisplayCutout.topBoundingRectOrNull(): android.graphics.Rect? {
    val top = getBoundingRectTop().takeUnless { it.isEmpty } ?: return null
    val path = cutoutPath ?: return top
    val bounds = android.graphics.RectF()
    path.computeBounds(bounds, true)
    if (bounds.isEmpty || bounds.top > top.bottom) return top
    return android.graphics.Rect(
        bounds.left.toInt(),
        bounds.top.toInt(),
        kotlin.math.ceil(bounds.right).toInt(),
        kotlin.math.ceil(bounds.bottom).toInt(),
    )
}

private fun PopupContentModel.isUtilityStatusContent(): Boolean {
    return this is PopupContentModel.ScreenRecord ||
        this is PopupContentModel.Stopwatch ||
        this is PopupContentModel.Alarm ||
        this is PopupContentModel.Flashlight ||
        (this is PopupContentModel.SystemEvent && kind in setOf(
            SystemEventKind.Notification, SystemEventKind.Ongoing, SystemEventKind.Unlock,
            SystemEventKind.Call, SystemEventKind.Bluetooth, SystemEventKind.Ringer,
            SystemEventKind.RecentApps, SystemEventKind.Hotspot,
            SystemEventKind.Charging, SystemEventKind.Recording, SystemEventKind.Timer,
        ))
}

private fun compactIslandWidthFor(content: PopupContentModel): Dp? {
    return when (content) {
        is PopupContentModel.Media -> CompactMediaIslandWidth
        is PopupContentModel.ScreenRecord ->
            when (content.model) {
                is ScreenRecordPopupModel.Starting -> CompactTimerIslandWidth
                is ScreenRecordPopupModel.Recording -> CompactRecordingIslandWidth
            }
        is PopupContentModel.Stopwatch -> CompactTimerIslandWidth
        is PopupContentModel.Alarm -> CompactAlarmIslandWidth
        is PopupContentModel.Flashlight -> CompactUtilityIslandWidth
        else -> null
    }
}

/** Per-feature glyph accent for the collapsed pill — SmartIsland's colored-glyph motif. */
private fun islandChipAccentFor(content: PopupContentModel): Color? =
    when (content) {
        is PopupContentModel.Flashlight -> IslandAccents.Flashlight
        is PopupContentModel.Alarm -> IslandAccents.Alarm
        is PopupContentModel.Stopwatch -> IslandAccents.Stopwatch
        is PopupContentModel.ScreenRecord -> IslandAccents.Recording
        else -> null
    }

/**
 * Collapsed-pill leading glyph wrapped in SmartIsland's circular accent badge, carrying the
 * per-feature motion SmartIsland shows in its collapsed island: a pulsing recording indicator, a
 * slowly spinning stopwatch, and a gently breathing alarm. Features without a motif (flashlight)
 * render a still glyph. Sizes are passed pre-scaled by the island's width scale so the badge stays
 * within the cutout-split chrome at every scale.
 */
@Composable
private fun CollapsedGlyphBadge(
    icon: IconModel,
    accent: Color,
    content: PopupContentModel,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 20.dp,
    iconSize: Dp = 13.dp,
) {
    Box(
        modifier =
            modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(IslandAccents.badgeFill(accent)),
        contentAlignment = Alignment.Center,
    ) {
        val iconModifier =
            when (content) {
                is PopupContentModel.ScreenRecord -> Modifier.size(iconSize).recordingPulse()
                is PopupContentModel.Stopwatch -> Modifier.size(iconSize).stopwatchSpin()
                is PopupContentModel.Alarm -> Modifier.size(iconSize).alarmBreathe()
                else -> Modifier.size(iconSize)
            }
        Icon(icon = icon, modifier = iconModifier, tint = accent)
    }
}

@Composable
private fun Modifier.recordingPulse(): Modifier {
    val transition = rememberInfiniteTransition(label = "collapsed_glyph_rec")
    val scale by
        transition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.12f,
            animationSpec =
                infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "rec_scale",
        )
    val alpha by
        transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "rec_alpha",
        )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

@Composable
private fun Modifier.stopwatchSpin(): Modifier {
    val transition = rememberInfiniteTransition(label = "collapsed_glyph_spin")
    val spin by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
            label = "spin",
        )
    return graphicsLayer { rotationZ = spin }
}

@Composable
private fun Modifier.alarmBreathe(): Modifier {
    val transition = rememberInfiniteTransition(label = "collapsed_glyph_breathe")
    val breathe by
        transition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec =
                infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "breathe",
        )
    return graphicsLayer {
        scaleX = breathe
        scaleY = breathe
    }
}

/**
 * Small icon-only "peek" chip shown beside the main island when several tasks are active, ported
 * from SmartIsland's secondary bubble. It shows album art for media or the feature's accent-tinted
 * glyph otherwise, pops in with a bouncy scale, and squishes on press. It is an independent popup
 * trigger: tapping it opens that task's popup anchored to the peek's own position and folds the peek
 * away in place, without moving it to the centre or disturbing the main chip.
 */
@Composable
internal fun CompanionChip(
    viewModel: PopupChipModel.Shown,
    diameter: Dp,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    onBoundsChanged: (Rect) -> Unit = {},
) {
    val colors = viewModel.colors
    val chipContentColor =
        colors.chipContent(isPopupShown = false, colorScheme = MaterialTheme.colorScheme)
    val chipOutline =
        colors.chipOutline(isPopupShown = false, colorScheme = MaterialTheme.colorScheme)
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.9f else 1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 900f),
            label = "companionPressScale",
        )
    val appear = remember { Animatable(0.3f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, spring(dampingRatio = 0.68f, stiffness = 480f)) }
    val popupOpen = viewModel.isPopupShown
    val collapse by
        animateFloatAsState(
            targetValue = if (popupOpen) 0f else 1f,
            animationSpec =
                if (popupOpen) {
                    spring(dampingRatio = 0.9f, stiffness = 500f)
                } else {
                    spring(dampingRatio = 0.65f, stiffness = 520f)
                },
            label = "companionCollapse",
        )

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    val s = pressScale * appear.value * collapse
                    scaleX = s
                    scaleY = s
                    alpha = appear.value * collapse
                }
                .size(diameter)
                .onGloballyPositioned { onBoundsChanged(it.boundsInScreen(view)) }
                .clip(CircleShape)
                .background(Color.Black)
                .border(width = 1.dp, color = chipOutline, shape = CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onTap,
                ),
        contentAlignment = Alignment.Center,
    ) {
        val chipIcon = viewModel.icons.firstOrNull()
        val icon = chipIcon?.icon
        val content = viewModel.popupContent
        if (icon != null) {
            if (content is PopupContentModel.Media) {
                Icon(
                    icon = icon,
                    modifier = Modifier.size(diameter * 0.72f).clip(CircleShape),
                    tint = Color.Unspecified,
                )
            } else {
                Icon(
                    icon = icon,
                    modifier = Modifier.size(diameter * 0.46f),
                    tint = if (chipIcon?.tint == false) Color.Unspecified
                        else islandChipAccentFor(content) ?: chipContentColor,
                )
            }
        }
    }
}

@Composable
internal fun rememberDynamicIslandSizeScale(): Pair<Float, Float> {
    val context = LocalContext.current
    val widthScale by
        remember { observeDynamicIslandScale(context, DynamicIslandFeatureSettings.WIDTH_SCALE) }
            .collectAsState(initial = 1f)
    val heightScale by
        remember { observeDynamicIslandScale(context, DynamicIslandFeatureSettings.HEIGHT_SCALE) }
            .collectAsState(initial = 1f)
    return widthScale to heightScale
}

internal data class DynamicIslandCollapseState(
    val scaleX: Float,
    val scaleY: Float,
    val contentAlpha: Float,
)

@Composable
internal fun rememberDynamicIslandCollapseState(isOpen: Boolean): DynamicIslandCollapseState {
    val progress = remember { Animatable(if (isOpen) 1f else 0f, visibilityThreshold = 0.0005f) }
    val currentIsOpen by rememberUpdatedState(isOpen)

    LaunchedEffect(Unit) {
        snapshotFlow { currentIsOpen }
            .drop(1)
            .collectLatest { open ->
                if (open) {
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f),
                    )
                } else {
                    progress.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 520f),
                    )
                }
            }
    }

    val p = progress.value
    val squash = p.coerceIn(0f, 1f)
    val scaleX = (1f - p).coerceAtLeast(0f)
    val scaleY = 1f - 0.18f * squash
    val contentAlpha = ((1f - p) / 0.62f).coerceIn(0f, 1f)
    return DynamicIslandCollapseState(scaleX = scaleX, scaleY = scaleY, contentAlpha = contentAlpha)
}

private fun LayoutCoordinates.boundsInScreen(view: android.view.View): Rect {
    val location = IntArray(2)
    view.getLocationOnScreen(location)
    return boundsInRoot().translate(Offset(location[0].toFloat(), location[1].toFloat()))
}
