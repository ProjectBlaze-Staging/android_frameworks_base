/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.systemui.brightness.ui.compose

import android.content.Context
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.compose.lifecycle.DisposableEffectWithLifecycle
import com.android.compose.ui.graphics.drawInOverlay
import com.android.systemui.biometrics.Utils.toBitmap
import com.android.systemui.brightness.domain.model.GammaBrightness
import com.android.systemui.brightness.ui.viewmodel.BrightnessSliderViewModel
import com.android.systemui.brightness.ui.viewmodel.Drag
import com.android.systemui.common.shared.model.Icon as SysIcon
import com.android.systemui.compose.modifiers.sysuiResTag
import com.android.systemui.haptics.slider.SeekableSliderTrackerConfig
import com.android.systemui.haptics.slider.SliderHapticFeedbackConfig
import com.android.systemui.haptics.slider.compose.ui.SliderHapticsViewModel
import com.android.systemui.lifecycle.rememberViewModel
import com.android.systemui.qs.ui.compose.borderOnFocus
import com.android.systemui.res.R
import com.android.systemui.util.policy.PolicyRestriction
import kotlinx.coroutines.launch

@Composable
@VisibleForTesting
fun BrightnessSlider(
    gammaValue: Int,
    valueRange: IntRange,
    autoMode: Boolean,
    iconResProvider: (Float) -> Int,
    imageLoader: suspend (Int, Context) -> SysIcon.Loaded?,
    restriction: PolicyRestriction,
    onRestrictedClick: (PolicyRestriction.Restricted) -> Unit,
    onDrag: (Int) -> Unit,
    onStop: (Int) -> Unit,
    onIconClick: suspend () -> Unit,
    overriddenByAppState: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showToast: () -> Unit = {},
    hapticsViewModelFactory: SliderHapticsViewModel.Factory,
    dimensions: BrightnessSliderDimensions = BrightnessSliderDimensions.Default,
    isVertical: Boolean = false,
) {
    val floatValueRange = valueRange.first.toFloat()..valueRange.last.toFloat()
    val isRestricted = restriction is PolicyRestriction.Restricted
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentShowToast by rememberUpdatedState(showToast)

    val interactionSource = remember { MutableInteractionSource() }
    val hapticsViewModel: SliderHapticsViewModel =
        rememberViewModel(traceName = "SliderHapticsViewModel") {
            hapticsViewModelFactory.create(
                interactionSource,
                floatValueRange,
                Orientation.Horizontal,
                SliderHapticFeedbackConfig(maxVelocityToScale = 1f),
                SeekableSliderTrackerConfig(),
            )
        }

    val sliderPainter = rememberVectorPainter(Icons.Rounded.Brightness6)
    val autoIconRes by remember(gammaValue, floatValueRange, autoMode) {
        derivedStateOf {
            val percentage = (gammaValue.toFloat() - floatValueRange.start) * 100f /
                    (floatValueRange.endInclusive - floatValueRange.start)
            iconResProvider(percentage)
        }
    }

    val autoBrightnessPainter: Painter by produceState<Painter>(
        initialValue = ColorPainter(Color.Transparent),
        key1 = autoIconRes,
        key2 = context,
    ) {
        val icon: SysIcon.Loaded? = imageLoader(autoIconRes, context)
        if (icon != null) {
            val bitmap = icon.drawable.toBitmap()?.asImageBitmap()
            if (bitmap != null) {
                this@produceState.value = BitmapPainter(bitmap)
            }
        }
    }

    val hasAutoBrightness = context.resources.getBoolean(
        com.android.internal.R.bool.config_automatic_brightness_available
    )

    val onInteraction: () -> Boolean = {
        if (overriddenByAppState) {
            currentShowToast()
            true
        } else {
            false
        }
    }

    val sliderCore = @Composable {
        GlassSliderCore(
            value = gammaValue.toFloat(),
            valueRange = floatValueRange,
            onValueChange = { newValue ->
                if (enabled && !overriddenByAppState) {
                    hapticsViewModel.onValueChange(newValue)
                    onDrag(newValue.toInt())
                }
            },
            onValueChangeFinished = { finalValue ->
                if (enabled && !overriddenByAppState) {
                    hapticsViewModel.onValueChangeEnded()
                    onStop(finalValue.toInt())
                }
            },
            onInteraction = onInteraction,
            enabled = enabled,
            painter = sliderPainter,
            isVertical = isVertical
        )
    }

    val autoButton = @Composable {
        Box(
            modifier = Modifier
                .then(if (isVertical) Modifier.fillMaxWidth() else Modifier.fillMaxHeight())
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .then(
                    if (enabled) Modifier.clickable {
                        coroutineScope.launch { onIconClick() }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = autoBrightnessPainter,
                contentDescription = stringResource(R.string.accessibility_adaptive_brightness),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }

    val sliderContentDescription = stringResource(R.string.accessibility_brightness)
    val baseModifier = modifier
        .fillMaxSize()
        .padding(
            horizontal = if (isVertical) 12.dp else 16.dp,
            vertical = if (isVertical) 16.dp else 12.dp
        )
        .then(
            if (isRestricted) {
                Modifier.clickable {
                    onRestrictedClick(restriction as PolicyRestriction.Restricted)
                }
            } else Modifier
        )

    if (isVertical) {
        Column(
            modifier = baseModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        this.contentDescription = sliderContentDescription
                    },
                contentAlignment = Alignment.Center,
            ) {
                sliderCore()
            }

            if (hasAutoBrightness) {
                Spacer(modifier = Modifier.height(10.dp))
                autoButton()
            }
        }
    } else {
        Row(
            modifier = baseModifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics(mergeDescendants = true) {
                        this.contentDescription = sliderContentDescription
                    },
                contentAlignment = Alignment.Center,
            ) {
                sliderCore()
            }

            if (hasAutoBrightness) {
                Spacer(modifier = Modifier.width(10.dp))
                autoButton()
            }
        }
    }
}

@Composable
fun GlassSliderCore(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: ((Float) -> Unit)?,
    onInteraction: () -> Boolean,
    enabled: Boolean,
    painter: Painter,
    isVertical: Boolean,
) {
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val rawFraction = ((value - valueRange.start) /
            (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)

    val fraction by animateFloatAsState(
        targetValue = dragFraction ?: rawFraction,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tile_brightness_fraction",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .pointerInput(enabled, isVertical, valueRange) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    if (onInteraction()) return@detectTapGestures
                    val ratio = if (isVertical) {
                        (1f - offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                    } else {
                        (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    }
                    val newValue = valueRange.start + ratio * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                    onValueChangeFinished?.invoke(newValue)
                }
            }
            .pointerInput(enabled, isVertical, valueRange) {
                if (!enabled) return@pointerInput
                var startFraction = 0f
                var isDraggingAllowed = false
                detectDragGestures(
                    onDragStart = {
                        if (onInteraction()) {
                            isDraggingAllowed = false
                        } else {
                            isDraggingAllowed = true
                            startFraction = dragFraction ?: rawFraction
                            dragFraction = startFraction
                        }
                    },
                    onDragEnd = {
                        if (!isDraggingAllowed) return@detectDragGestures
                        val finalFraction = dragFraction ?: rawFraction
                        dragFraction = null
                        onValueChangeFinished?.invoke(valueRange.start + finalFraction * (valueRange.endInclusive - valueRange.start))
                    },
                    onDragCancel = {
                        if (!isDraggingAllowed) return@detectDragGestures
                        val finalFraction = dragFraction ?: rawFraction
                        dragFraction = null
                        onValueChangeFinished?.invoke(valueRange.start + finalFraction * (valueRange.endInclusive - valueRange.start))
                    },
                    onDrag = { change, dragAmount ->
                        if (!isDraggingAllowed) return@detectDragGestures
                        change.consume()
                        val delta = if (isVertical) {
                            -dragAmount.y / size.height.toFloat()
                        } else {
                            dragAmount.x / size.width.toFloat()
                        }
                        startFraction = (startFraction + delta).coerceIn(0f, 1f)
                        dragFraction = startFraction
                        onValueChange(
                            valueRange.start + startFraction *
                                    (valueRange.endInclusive - valueRange.start)
                        )
                    },
                )
            },
    ) {
        val minDim = if (isVertical) constraints.maxWidth else constraints.maxHeight
        val totalDim = if (isVertical) constraints.maxHeight else constraints.maxWidth

        val fillDim = (totalDim * fraction).coerceAtLeast(minDim.toFloat())
        val fillFraction = if (totalDim > 0) fillDim / totalDim.toFloat() else 0f

        if (isVertical) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(fillFraction)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(fillFraction)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun BrightnessSliderContainer(
    viewModel: BrightnessSliderViewModel,
    modifier: Modifier = Modifier,
    containerColors: ContainerColors,
    dimensions: BrightnessSliderDimensions = BrightnessSliderDimensions.Default,
) {
    val gamma = viewModel.currentBrightness.value
    if (gamma == BrightnessSliderViewModel.initialValue.value) {
        return
    }
    
    val autoMode = viewModel.autoMode
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val restriction by viewModel.policyRestriction.collectAsStateWithLifecycle(
        initialValue = PolicyRestriction.NoRestriction
    )
    val overriddenByAppState by viewModel.brightnessOverriddenByWindow.collectAsStateWithLifecycle()
    var dragging by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(false) }

    DisposableEffectWithLifecycle(Unit) {
        enabled = true
        onDispose {
            dragging = false
            viewModel.setIsDragging(false)
            enabled = false
        }
    }

    val isRestricted = restriction is PolicyRestriction.Restricted

    Box(
        modifier = modifier
            .padding(
                vertical = dimensions.verticalPadding,
                horizontal = 16.dp
            )
            .sysuiResTag("brightness_slider")
    ) {
        BrightnessSlider(
            enabled = enabled && !isRestricted,
            gammaValue = gamma,
            valueRange = viewModel.minBrightness.value..viewModel.maxBrightness.value,
            autoMode = autoMode,
            iconResProvider = { percentage -> BrightnessSliderViewModel.getIconForPercentage(percentage, autoMode) },
            imageLoader = viewModel::loadImage,
            restriction = restriction,
            onRestrictedClick = viewModel::showPolicyRestrictionDialog,
            onDrag = {
                viewModel.setIsDragging(true)
                dragging = true
                coroutineScope.launch { viewModel.onDrag(Drag.Dragging(GammaBrightness(it))) }
            },
            onStop = {
                viewModel.setIsDragging(false)
                dragging = false
                coroutineScope.launch { viewModel.onDrag(Drag.Stopped(GammaBrightness(it))) }
            },
            onIconClick = { viewModel.onIconClick() },
            modifier = Modifier
                .borderOnFocus(
                    color = MaterialTheme.colorScheme.secondary,
                    cornerSize = CornerSize(50),
                )
                .then(if (viewModel.showMirror) Modifier.drawInOverlay() else Modifier)
                .fillMaxWidth()
                .height(dimensions.thumbHeight + 24.dp) 
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(percent = 50),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(percent = 50)
                )
                .clip(RoundedCornerShape(percent = 50))
                .pointerInteropFilter {
                    if (
                        it.actionMasked == MotionEvent.ACTION_UP ||
                        it.actionMasked == MotionEvent.ACTION_CANCEL
                    ) {
                        viewModel.emitBrightnessTouchForFalsing()
                    }
                    false
                },
            hapticsViewModelFactory = viewModel.hapticsViewModelFactory,
            overriddenByAppState = overriddenByAppState,
            showToast = {
                viewModel.showToast(
                    context,
                    com.android.internal.R.string.brightness_unable_adjust_msg,
                )
            },
            dimensions = dimensions,
            isVertical = false
        )
    }
}

data class ContainerColors(val idleColor: Color, val mirrorColor: Color) {
    companion object {
        fun singleColor(color: Color) = ContainerColors(color, color)

        val defaultContainerColor: Color
            @Composable @ReadOnlyComposable get() = colorResource(R.color.shade_panel_fallback)
    }
}

data class BrightnessSliderDimensions(
    val iconSize: DpSize,
    val thumbHeight: Dp,
    val thumbWidth: Dp,
    val trackHeight: Dp,
    val verticalPadding: Dp,
    val backgroundRoundedCorner: Dp,
    val backgroundFrameWidth: Dp,
    val backgroundFrameHeight: Dp,
) {
    companion object {
        val Default =
            BrightnessSliderDimensions(
                iconSize = DpSize(28.dp, 28.dp),
                thumbHeight = 52.dp,
                thumbWidth = 4.dp,
                trackHeight = 40.dp,
                verticalPadding = 6.dp,
                backgroundRoundedCorner = 24.dp,
                backgroundFrameWidth = 10.dp,
                backgroundFrameHeight = 6.dp,
            )
    }
}
