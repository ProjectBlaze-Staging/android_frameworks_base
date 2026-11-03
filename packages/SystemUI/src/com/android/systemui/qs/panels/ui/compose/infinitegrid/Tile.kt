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

@file:OptIn(ExperimentalFoundationApi::class)

package com.android.systemui.qs.panels.ui.compose.infinitegrid

import android.content.Context
import android.content.res.Resources
import android.os.Trace
import android.service.quicksettings.Tile.STATE_ACTIVE
import android.service.quicksettings.Tile.STATE_INACTIVE
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import com.android.internal.penguin.glass.LiquidGlass
import com.android.app.tracing.coroutines.launchTraced as launch
import com.android.compose.animation.Expandable
import com.android.compose.animation.bounceable
import com.android.compose.animation.rememberExpandableController
import com.android.compose.animation.scene.ContentScope
import com.android.compose.modifiers.thenIf
import com.android.compose.modifiers.width
import com.android.compose.theme.LocalAndroidColorScheme
import com.android.internal.graphics.ColorUtils
import com.android.mechanics.compose.modifier.verticalFadeContentReveal
import com.android.mechanics.compose.modifier.verticalTactileSurfaceReveal
import com.android.mechanics.effects.VerticalTactileSurfaceRevealEffect
import com.android.systemui.Flags
import com.android.systemui.animation.Expandable
import com.android.systemui.animation.TransitionAnimator.Companion.dynamicTargetResolutionEnabled
import com.android.systemui.common.shared.model.Icon
import com.android.systemui.compose.modifiers.sysuiResTag
import com.android.systemui.haptics.msdl.qs.TileHapticsViewModel
import com.android.systemui.lifecycle.rememberViewModel
import com.android.systemui.qs.flags.QsDetailedView
import com.android.systemui.qs.panels.ui.compose.BounceableInfo
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.ActiveCornerRadius
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.InactiveCornerRadius
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.tileHeight
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.TileArrangementPadding
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.TilePaddingLarge
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.TileStartPadding
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.longPressLabelSettings
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.longPressLabelMoreDetails
import com.android.systemui.qs.panels.ui.viewmodel.AccessibilityUiState
import com.android.systemui.qs.panels.ui.viewmodel.DetailsViewModel
import com.android.systemui.qs.panels.ui.viewmodel.IconProvider
import com.android.systemui.qs.panels.ui.viewmodel.TileUiState
import com.android.systemui.qs.panels.ui.viewmodel.TileViewModel
import com.android.systemui.qs.panels.ui.viewmodel.toIconProvider
import com.android.systemui.qs.panels.ui.viewmodel.toUiState
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.qs.ui.composable.QuickSettingsShade
import com.android.systemui.qs.ui.compose.borderOnFocus
import com.android.systemui.res.R
import kotlinx.coroutines.CoroutineScope

private const val TEST_TAG_SMALL = "qs_tile_small"
private const val TEST_TAG_LARGE = "qs_tile_large"

@Composable
fun TileLazyGrid(
    columns: GridCells,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        state = state,
        columns = columns,
        verticalArrangement = spacedBy(CommonTileDefaults.TileArrangementPadding),
        horizontalArrangement = spacedBy(CommonTileDefaults.TileArrangementPadding),
        contentPadding = contentPadding,
        modifier = modifier,
        content = content,
    )
}

private val TileViewModel.traceName
    get() = spec.toString().takeLast(Trace.MAX_SECTION_NAME_LEN)

/**
 * This composable function is responsible for rendering a tile based on the provided
 * [TileViewModel]. It handles different states of the tile (e.g., available, unavailable),
 * interactions (click, long click), and visual styles (icon only or large tile).
 *
 * @param tile The [TileViewModel] containing the data and logic for the tile.
 * @param iconOnly A boolean indicating whether to display only the icon of the tile or the full
 * tile content (false for large tiles).
 * @param squishiness The float value representing the current squishiness factor of the tile, used
 * for animations.
 * @param coroutineScope The [CoroutineScope] to launch coroutines for animations.
 * @param tileHapticsViewModelFactoryProvider A provider for creating a [TileHapticsViewModel]
 * instance, used for haptic feedback.
 * @param interactionSource An optional [MutableInteractionSource] to track user interactions with
 * the tile, used by the parent composable to animate a bounce effect. Tiles may or may not use
 * this interaction source to control whether they should bounce or not.
 * @param modifier An optional [Modifier] to be applied to the root composable of the tile.
 * @param isVisible Whether the tile is currently visible. Defaults to true.
 * @param requestToggleTextFeedback A lambda function that is invoked when a toggleable icon only
 * tile is clicked, used to request the feedback text.
 * @param detailsViewModel An optional [DetailsViewModel] used to handle navigation to a detailed
 * view when a tile is clicked, if applicable.
 * @param enableRevealEffect If `true`, the tiles will animate using the reveal animation.
 */
@Composable
fun ContentScope.Tile(
    tile: TileViewModel,
    iconOnly: Boolean,
    spanRows: Int = 1,
    squishiness: () -> Float,
    coroutineScope: CoroutineScope,
    bounceableInfo: BounceableInfo?,
    tileHapticsViewModelFactory: TileHapticsViewModel.Factory,
    interactionSource: MutableInteractionSource? = null,
    modifier: Modifier = Modifier,
    isVisible: () -> Boolean = { true },
    requestToggleTextFeedback: (TileSpec) -> Unit = {},
    detailsViewModel: DetailsViewModel?,
    enableRevealEffect: Boolean = false,
) {
    trace(tile.traceName) {
        val currentBounceableInfo by rememberUpdatedState(bounceableInfo)
        val resources = resources()

        val uiState by
            produceState(tile.currentState.toUiState(resources), tile, resources) {
                tile.state.collect { value = it.toUiState(resources) }
            }
        val isClickable = uiState.handlesMainClick

        val icon by
            produceState(tile.currentState.toIconProvider(), tile) {
                tile.state.collect { value = it.toIconProvider() }
            }

        val colors = TileDefaults.getColorForState(uiState, iconOnly)
        val hapticsViewModel: TileHapticsViewModel =
            rememberViewModel(traceName = "TileHapticsViewModel") {
                tileHapticsViewModelFactory.create(tile)
            }

        val density = LocalDensity.current
        val tileHeightDp = tileHeight()
        val height = remember(spanRows, density, tileHeightDp, iconOnly) {
            if (iconOnly) {
                tileHeightDp
            } else {
                with(density) {
                    (tileHeightDp.toPx() * spanRows + TileArrangementPadding.toPx() * (spanRows - 1)).toDp()
                }
            }
        }
        
        val tileShape by TileDefaults.animateTileShapeAsState(state = uiState.visualState, iconOnly = iconOnly)
        val animatedColor by animateColorAsState(colors.background, label = "QSTileBackgroundColor")

        val surfaceRevealModifier: Modifier
        val contentRevealModifier: Modifier
        if (enableRevealEffect) {
            val marginBottom =
                with(LocalDensity.current) { QuickSettingsShade.Dimensions.VerticalPadding.toPx() }

            val animatedCornerRadius by animateDpAsState(TileDefaults.tileRadius(uiState))

            val inactiveCornerRadius = TileDefaults.InactiveIconCornerRadius
            surfaceRevealModifier =
                Modifier.verticalTactileSurfaceReveal(
                    deltaY = marginBottom,
                    effectSpec =
                        remember(inactiveCornerRadius) {
                            VerticalTactileSurfaceRevealEffect(
                                maxCornerSize = { animatedCornerRadius },
                                phase1MarginX = inactiveCornerRadius,
                            )
                        },
                    label = tile.traceName,
                )

            contentRevealModifier =
                Modifier.verticalFadeContentReveal(deltaY = marginBottom, label = tile.traceName)
        } else {
            surfaceRevealModifier = Modifier
            contentRevealModifier = Modifier
        }

        TileExpandable(
            color = { animatedColor },
            shape = tileShape,
            squishiness = if (iconOnly) { { 1f } } else squishiness,
            hapticsViewModel = hapticsViewModel,
            modifier =
                modifier
                    .then(surfaceRevealModifier)
                    .blazeGlassTileBorder(tileShape)
                    .borderOnFocus(color = MaterialTheme.colorScheme.secondary, tileShape.topEnd)
                    .then(
                        if (iconOnly)
                            Modifier.width { tileHeightDp.roundToPx() }
                        else
                            Modifier.fillMaxWidth(0.9f)
                    )
                    .thenIf(currentBounceableInfo != null) {
                        Modifier.bounceable(
                            bounceable = currentBounceableInfo!!.bounceable,
                            previousBounceable = currentBounceableInfo!!.previousTile,
                            nextBounceable = currentBounceableInfo!!.nextTile,
                            orientation = Orientation.Horizontal,
                            bounceEnd = currentBounceableInfo!!.bounceEnd,
                        )
                    },
        ) { expandable ->
            val useLongClickToSettings = !(iconOnly && uiState.handlesToggleClick && isClickable)
            val longClick: (() -> Unit)? =
                {
                        hapticsViewModel?.setTileInteractionState(
                            TileHapticsViewModel.TileInteractionState.LONG_CLICKED
                        )

                        if (useLongClickToSettings) {
                            tile.settingsClick(expandable)
                        } else {
                            tile.mainClick(expandable)
                        }
                    }
                    .takeIf { !useLongClickToSettings }

            val bounceContainer = uiState.isToggleable && (iconOnly || !uiState.handlesToggleClick)
            TileContainer(
                interactionSource = interactionSource.takeIf { bounceContainer },
                height = height,
                onClick = onClick@{
                        if (!isClickable) return@onClick

                        val hasDetails =
                            QsDetailedView.isEnabled &&
                                detailsViewModel?.onTileClicked(tile.spec) == true
                        if (hasDetails) return@onClick

                        if (iconOnly && uiState.handlesToggleClick) {
                            tile.toggleClick()
                        } else {
                            tile.mainClick(expandable)
                        }

                        hapticsViewModel?.setTileInteractionState(
                            TileHapticsViewModel.TileInteractionState.CLICKED
                        )
                        coroutineScope.launch {
                            val detailsVisible =
                                QsDetailedView.isEnabled &&
                                    detailsViewModel?.onTileClicked(tile.spec) == true
                            if (!detailsVisible && !bounceContainer) {
                                tile.mainClick(expandable)
                            }
                        }
                        if (uiState.isToggleable && iconOnly) {
                            requestToggleTextFeedback(tile.spec)
                        }
                    },
                onLongClick = longClick,
                accessibilityUiState = uiState.accessibilityUiState,
                iconOnly = iconOnly,
                isDualTarget = uiState.handlesToggleClick,
                modifier = contentRevealModifier,
            ) {
                val iconProvider: Context.() -> Icon = { getTileIcon(icon = icon) }
                if (iconOnly) {
                    SmallTileContent(
                        iconProvider = iconProvider,
                        color = colors.icon,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    val iconShape by TileDefaults.animateIconShapeAsState(uiState.visualState)
                    val secondaryClick: (() -> Unit)? =
                        {
                            hapticsViewModel?.setTileInteractionState(
                                TileHapticsViewModel.TileInteractionState.CLICKED
                            )
                            tile.toggleClick()
                        }.takeIf { uiState.handlesToggleClick }
                        
                    LargeTileContent(
                        label = uiState.label,
                        secondaryLabel = uiState.secondaryLabel,
                        iconProvider = iconProvider,
                        sideDrawable = uiState.sideDrawable,
                        colors = colors,
                        iconShape = iconShape,
                        toggleClick = secondaryClick,
                        onLongClick = longClick,
                        accessibilityUiState = uiState.accessibilityUiState,
                        squishiness = squishiness,
                        isVisible = isVisible,
                        textScale = { 1f },
                        modifier =
                            Modifier.largeTilePadding(isDualTarget = uiState.handlesToggleClick),
                    )
                }
            }
        }
    }
}

@Composable
private fun TileExpandable(
    color: () -> Color,
    shape: Shape,
    squishiness: () -> Float,
    hapticsViewModel: TileHapticsViewModel?,
    modifier: Modifier = Modifier,
    content: @Composable (Expandable) -> Unit,
) {
    Expandable(
        controller = rememberExpandableController(color = color, shape = shape),
        modifier =
            modifier
                .clip(shape)
                .verticalSquish(squishiness),
        useModifierBasedImplementation = true,
    ) {
        content(hapticsViewModel?.createStateAwareExpandable(it) ?: it)
    }
}

@Composable
fun TileContainer(
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    accessibilityUiState: AccessibilityUiState,
    iconOnly: Boolean,
    isDualTarget: Boolean,
    interactionSource: MutableInteractionSource?,
    height: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .height(height)
                .fillMaxWidth()
                .tileCombinedClickable(
                    onClick = onClick ?: {},
                    onLongClick = onLongClick,
                    accessibilityUiState = accessibilityUiState,
                    interactionSource = interactionSource,
                    iconOnly = iconOnly,
                    isDualTarget = isDualTarget,
                )
                .tileTestTag(iconOnly),
        content = content
    )
}

private fun Context.getTileIcon(icon: IconProvider): Icon {
    return icon.icon?.let {
        if (it is QSTileImpl.ResourceIcon) {
            Icon.Resource(it.resId, null)
        } else {
            Icon.Loaded(it.getDrawable(this), null)
        }
    } ?: Icon.Resource(R.drawable.ic_error_outline, null)
}

fun tileHorizontalArrangement(): Arrangement.Horizontal {
    return spacedBy(space = CommonTileDefaults.TileArrangementPadding, alignment = Alignment.Start)
}

@Composable
fun Modifier.tileCombinedClickable(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    accessibilityUiState: AccessibilityUiState,
    interactionSource: MutableInteractionSource?,
    iconOnly: Boolean,
    isDualTarget: Boolean,
): Modifier {
    val longPressLabel =
        if (iconOnly && isDualTarget) longPressLabelMoreDetails() else longPressLabelSettings()
    return combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            onClickLabel = accessibilityUiState.clickLabel,
            onLongClickLabel = longPressLabel,
            hapticFeedbackEnabled = false, // Haptics handled separately
            interactionSource = interactionSource,
        )
        .semantics {
            val accessibilityRole =
                if (iconOnly && isDualTarget) {
                    Role.Switch
                } else {
                    accessibilityUiState.accessibilityRole
                }
            if (accessibilityRole == Role.Switch) {
                accessibilityUiState.toggleableState?.let { toggleableState = it }
            }
            role = accessibilityRole
            stateDescription = accessibilityUiState.stateDescription
        }
        .thenIf(iconOnly) {
            Modifier.semantics { contentDescription = accessibilityUiState.contentDescription }
        }
}

data class TileColors(
    val background: Color,
    val iconBackground: Color,
    val label: Color,
    val secondaryLabel: Color,
    val icon: Color,
)

private object TileDefaults {
    val ActiveIconCornerRadius = ActiveCornerRadius
    val ActiveTileCornerRadius = ActiveCornerRadius
    val InactiveIconCornerRadius = InactiveCornerRadius
    val InactiveTileCornerRadius = InactiveCornerRadius

    /** Half-alpha shade tile color, derived from the current context's themed color. */
    val shadeTileColor: Color
        @Composable
        @ReadOnlyComposable
        get() {
            val context = LocalContext.current
            val tileColor = context.getColor(R.color.shade_tile_color)
            val tileColorAlpha = ColorUtils.setAlphaComponent(tileColor, (0.5f * 255).toInt())
            return Color(tileColorAlpha)
        }

    /** An active icon tile uses the active color as background */
    @Composable
    @ReadOnlyComposable
    fun activeTileColors(): TileColors =
        TileColors(
            background = MaterialTheme.colorScheme.primary,
            iconBackground = Color.Transparent,
            label = MaterialTheme.colorScheme.onPrimary,
            secondaryLabel = MaterialTheme.colorScheme.onPrimary,
            icon = MaterialTheme.colorScheme.onPrimary,
        )

    @Composable
    @ReadOnlyComposable
    fun activeDualTargetTileColors(): TileColors =
        TileColors(
            background = MaterialTheme.colorScheme.primary,
            iconBackground = Color.Transparent,
            label = MaterialTheme.colorScheme.onPrimary,
            secondaryLabel = MaterialTheme.colorScheme.onPrimary,
            icon = MaterialTheme.colorScheme.onPrimary,
        )

    @Composable
    @ReadOnlyComposable
    fun inactiveDualTargetTileColors(): TileColors =
        TileColors(
            background = shadeTileColor,
            iconBackground = Color.Transparent,
            label = MaterialTheme.colorScheme.onSurface,
            secondaryLabel = MaterialTheme.colorScheme.onSurface,
            icon = MaterialTheme.colorScheme.onSurface,
        )

    @Composable
    @ReadOnlyComposable
    fun inactiveTileColors(): TileColors =
        TileColors(
            background = shadeTileColor,
            iconBackground = Color.Transparent,
            label = MaterialTheme.colorScheme.onSurface,
            secondaryLabel = MaterialTheme.colorScheme.onSurface,
            icon = MaterialTheme.colorScheme.onSurfaceVariant,
        )

    @Composable
    @ReadOnlyComposable
    fun unavailableTileColors(): TileColors {
        return TileColors(
            background = shadeTileColor,
            iconBackground = Color.Transparent,
            label = MaterialTheme.colorScheme.onSurface,
            secondaryLabel = MaterialTheme.colorScheme.onSurface,
            icon = MaterialTheme.colorScheme.onSurface,
        )
    }

    @Composable
    @ReadOnlyComposable
    fun getColorForState(uiState: TileUiState, iconOnly: Boolean): TileColors {
        val style = LiquidGlass.getEffectiveStyle(LocalContext.current)
        val base = when (uiState.visualState) {
            STATE_ACTIVE -> {
                if (uiState.handlesToggleClick && !iconOnly) {
                    activeDualTargetTileColors()
                } else {
                    activeTileColors()
                }
            }
            STATE_INACTIVE -> {
                if (uiState.handlesToggleClick && !iconOnly) {
                    inactiveDualTargetTileColors()
                } else {
                    inactiveTileColors()
                }
            }
            else -> unavailableTileColors()
        }
        return when (style) {
            LiquidGlass.STYLE_IOS_LIQUID_GLASS -> {
                val bgAlpha = if (uiState.visualState == STATE_ACTIVE && (!uiState.handlesToggleClick || iconOnly)) 0.86f else 0.42f
                base.copy(background = base.background.copy(alpha = bgAlpha))
            }
            LiquidGlass.STYLE_COLOROS_FROSTED -> {
                val bgAlpha = if (uiState.visualState == STATE_ACTIVE && (!uiState.handlesToggleClick || iconOnly)) 0.92f else 0.66f
                base.copy(background = base.background.copy(alpha = bgAlpha))
            }
            else -> base
        }
    }

    @Composable
    fun iconRadius(uiState: TileUiState): Dp {
        return when (uiState.visualState) {
            STATE_ACTIVE -> ActiveIconCornerRadius
            STATE_INACTIVE -> InactiveIconCornerRadius
            else -> InactiveIconCornerRadius
        }
    }
    
    @Composable
    fun tileRadius(uiState: TileUiState): Dp {
        return when (uiState.visualState) {
            STATE_ACTIVE -> ActiveTileCornerRadius
            STATE_INACTIVE -> InactiveTileCornerRadius
            else -> InactiveTileCornerRadius
        }
    }

    @Composable
    fun animateTileShapeAsState(state: Int, iconOnly: Boolean = false): State<RoundedCornerShape> {
        val targetRadius = if (iconOnly) {
            InactiveTileCornerRadius
        } else {
            if (state == STATE_ACTIVE) ActiveTileCornerRadius else InactiveTileCornerRadius
        }
        
        return animateShapeAsState(
            targetValue = targetRadius,
            label = "QSTileBackgroundCornerRadius",
        )
    }

    @Composable
    fun animateTileShapeAsState(uiState: TileUiState): State<RoundedCornerShape> {
        return animateShapeAsState(targetValue = tileRadius(uiState), label = "QSTileCornerRadius")
    }

    @Composable
    fun animateIconShapeAsState(state: Int): State<RoundedCornerShape> {
        return animateShapeAsState(
            targetValue = if (state == STATE_ACTIVE) ActiveIconCornerRadius else InactiveIconCornerRadius,
            label = "QSTileIconCornerRadius"
        )
    }

    @Composable
    fun animateShapeAsState(targetValue: Dp, label: String): State<RoundedCornerShape> {
        val animatedCornerRadius by animateDpAsState(targetValue = targetValue, label = label)

        return remember {
            val corner =
                object : CornerSize {
                    override fun toPx(shapeSize: Size, density: Density): Float {
                        return with(density) { animatedCornerRadius.toPx() }
                    }
                }
            mutableStateOf(RoundedCornerShape(corner))
        }
    }
}

/**
 * A composable function that returns the [Resources]. It will be recomposed when [Configuration]
 * gets updated.
 */
@Composable
@ReadOnlyComposable
private fun resources(): Resources {
    LocalConfiguration.current
    return LocalResources.current
}

@Composable
private fun Modifier.blazeGlassTileBorder(shape: Shape): Modifier {
    val style = LiquidGlass.getEffectiveStyle(LocalContext.current)
    return when (style) {
        LiquidGlass.STYLE_IOS_LIQUID_GLASS -> {
            this.border(
                width = 1.25.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x6EFFFFFF),
                        Color(0x18FFFFFF),
                        Color(0x4570B8FF),
                    )
                ),
                shape = shape,
            )
        }
        LiquidGlass.STYLE_COLOROS_FROSTED -> {
            this.border(
                width = 0.85.dp,
                color = Color(0x28FFFFFF),
                shape = shape,
            )
        }
        else -> this
    }
}
