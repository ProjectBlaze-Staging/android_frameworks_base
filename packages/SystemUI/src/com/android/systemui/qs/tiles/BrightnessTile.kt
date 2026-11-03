/*
 * Copyright (C) 2026 RisingOS (revived) Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.android.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.tiles

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.internal.logging.MetricsLogger
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.systemui.animation.Expandable
import com.android.systemui.biometrics.Utils.toBitmap
import com.android.systemui.brightness.domain.model.GammaBrightness
import com.android.systemui.brightness.ui.viewmodel.BrightnessSliderViewModel
import com.android.systemui.brightness.ui.viewmodel.Drag
import com.android.systemui.common.shared.model.Icon as SysIcon
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.plugins.ActivityStarter
import com.android.systemui.plugins.FalsingManager
import com.android.systemui.plugins.qs.QSTile.BooleanState
import com.android.systemui.plugins.statusbar.StatusBarStateController
import com.android.systemui.qs.QSHost
import com.android.systemui.qs.QsEventLogger
import com.android.systemui.qs.logging.QSLogger
import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.res.R
import com.android.systemui.util.policy.PolicyRestriction
import javax.inject.Inject
import kotlinx.coroutines.launch

class BrightnessTile @Inject constructor(
    host: QSHost,
    uiEventLogger: QsEventLogger,
    @Background backgroundLooper: Looper,
    @Main mainHandler: Handler,
    falsingManager: FalsingManager,
    metricsLogger: MetricsLogger,
    statusBarStateController: StatusBarStateController,
    activityStarter: ActivityStarter,
    qsLogger: QSLogger,
    brightnessViewModelFactory: BrightnessSliderViewModel.Factory,
) : QSTileImpl<BooleanState>(
    host, uiEventLogger, backgroundLooper, mainHandler,
    falsingManager, metricsLogger, statusBarStateController,
    activityStarter, qsLogger,
) {
    companion object {
        const val TILE_SPEC = "brightness"
        private val DISPLAY_SETTINGS = Intent("android.settings.DISPLAY_SETTINGS")
    }

    val brightnessViewModel: BrightnessSliderViewModel =
        brightnessViewModelFactory.create(supportsMirroring = false)

    override fun newTileState() = BooleanState()

    override fun getLongClickIntent(): Intent = DISPLAY_SETTINGS

    override fun handleClick(expandable: Expandable?) = Unit

    override fun handleUpdateState(state: BooleanState, arg: Any?) {
        state.value = true
        state.state = Tile.STATE_ACTIVE
        state.label = mContext.getString(R.string.quick_settings_brightness_label)
        state.icon = ResourceIcon.get(R.drawable.ic_qs_brightness)
        state.contentDescription = state.label
    }

    override fun getTileLabel(): CharSequence =
        mContext.getString(R.string.quick_settings_brightness_label)

    override fun getMetricsCategory(): Int = MetricsEvent.VIEW_UNKNOWN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightnessTileContent(
    viewModel: BrightnessSliderViewModel,
    isVertical: Boolean,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val gamma = viewModel.currentBrightness.value
    if (gamma == BrightnessSliderViewModel.initialValue.value) return

    DisposableEffect(Unit) {
        onDispose { viewModel.setIsDragging(false) }
    }

    val restriction by viewModel.policyRestriction.collectAsStateWithLifecycle(
        initialValue = PolicyRestriction.NoRestriction,
    )
    val overriddenByApp by viewModel.brightnessOverriddenByWindow.collectAsStateWithLifecycle()

    val floatRange =
        viewModel.minBrightness.value.toFloat()..viewModel.maxBrightness.value.toFloat()

    val enabled = restriction !is PolicyRestriction.Restricted && !isEditMode

    val coroutineScope = rememberCoroutineScope()

    val sliderPainter: Painter = rememberVectorPainter(Icons.Rounded.Brightness6)

    val autoIconRes by remember(gamma, floatRange, viewModel.autoMode) {
        derivedStateOf {
            val percentage =
                (gamma.toFloat() - floatRange.start) * 100f / (floatRange.endInclusive - floatRange.start)
            BrightnessSliderViewModel.getIconForPercentage(percentage, viewModel.autoMode)
        }
    }

    val context = LocalContext.current
    val autoBrightnessPainter: Painter by produceState<Painter>(
        initialValue = ColorPainter(Color.Transparent),
        key1 = autoIconRes,
        key2 = context,
    ) {
        val icon: SysIcon.Loaded? = viewModel.loadImage(autoIconRes, context)
        if (icon != null) {
            val bitmap = icon.drawable.toBitmap()?.asImageBitmap()
            if (bitmap != null) {
                this@produceState.value = BitmapPainter(bitmap)
            }
        }
    }

    if (isVertical) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                TileGlassSliderCore(
                    value = gamma.toFloat(),
                    valueRange = floatRange,
                    onValueChange = { newValue ->
                        if (enabled && !overriddenByApp) {
                            viewModel.setIsDragging(true)
                            coroutineScope.launch {
                                viewModel.onDrag(Drag.Dragging(GammaBrightness(newValue.toInt())))
                            }
                        }
                    },
                    onValueChangeFinished = { finalValue ->
                        if (enabled && !overriddenByApp) {
                            viewModel.setIsDragging(false)
                            coroutineScope.launch {
                                viewModel.onDrag(Drag.Stopped(GammaBrightness(finalValue.toInt())))
                            }
                        }
                    },
                    enabled = enabled,
                    painter = sliderPainter,
                    isVertical = true,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .then(
                        if (enabled) Modifier.clickable {
                            coroutineScope.launch { viewModel.onIconClick() }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = autoBrightnessPainter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                TileGlassSliderCore(
                    value = gamma.toFloat(),
                    valueRange = floatRange,
                    onValueChange = { newValue ->
                        if (enabled && !overriddenByApp) {
                            viewModel.setIsDragging(true)
                            coroutineScope.launch {
                                viewModel.onDrag(Drag.Dragging(GammaBrightness(newValue.toInt())))
                            }
                        }
                    },
                    onValueChangeFinished = { finalValue ->
                        if (enabled && !overriddenByApp) {
                            viewModel.setIsDragging(false)
                            coroutineScope.launch {
                                viewModel.onDrag(Drag.Stopped(GammaBrightness(finalValue.toInt())))
                            }
                        }
                    },
                    enabled = enabled,
                    painter = sliderPainter,
                    isVertical = false,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .then(
                        if (enabled) Modifier.clickable {
                            coroutineScope.launch { viewModel.onIconClick() }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = autoBrightnessPainter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun TileGlassSliderCore(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: ((Float) -> Unit)?,
    enabled: Boolean,
    painter: Painter,
    isVertical: Boolean,
) {
    val view = LocalView.current
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
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                    try {
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })
                    } finally {
                        view.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                }
            }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .pointerInput(enabled, isVertical, valueRange) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
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
                detectDragGestures(
                    onDragStart = {
                        startFraction = dragFraction ?: rawFraction
                        dragFraction = startFraction
                    },
                    onDragEnd = {
                        val finalFraction = dragFraction ?: rawFraction
                        dragFraction = null
                        onValueChangeFinished?.invoke(valueRange.start + finalFraction * (valueRange.endInclusive - valueRange.start))
                    },
                    onDragCancel = {
                        val finalFraction = dragFraction ?: rawFraction
                        dragFraction = null
                        onValueChangeFinished?.invoke(valueRange.start + finalFraction * (valueRange.endInclusive - valueRange.start))
                    },
                    onDrag = { change, dragAmount ->
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
