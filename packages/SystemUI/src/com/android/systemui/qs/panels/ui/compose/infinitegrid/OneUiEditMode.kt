/*
 * Copyright (C) 2026 RisingOS (revived) Android Project
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

package com.android.systemui.qs.panels.ui.compose.infinitegrid

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.compose.theme.LocalAndroidColorScheme
import com.android.systemui.animation.Expandable
import com.android.systemui.plugins.qs.QSTile
import com.android.systemui.qs.panels.shared.model.FloatingTile
import com.android.systemui.qs.panels.shared.model.SectionType
import com.android.systemui.qs.panels.ui.compose.DraggableGrid
import com.android.systemui.qs.panels.ui.compose.DraggableTile
import com.android.systemui.qs.panels.ui.compose.FloatingTileDragState
import com.android.systemui.qs.panels.ui.compose.solveEvenEdgeGrid
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.res.R
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun OneUITileContainer(
    isExpanded: Boolean,
    collapsedRows: Int,
    onExpandChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    isDropHover: Boolean = false,
    tileMargin: Dp = 12.dp,
    cellSizeScale: Float = 1f,
    evenEdgeSpacing: Boolean = false,
    animateSize: Boolean = true,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val totalWidth = maxWidth

        val columns = 4
        val contentPaddingV = 8.dp
        val contentPaddingH = 16.dp

        val availableWidth = totalWidth - (contentPaddingH * 2)
        val (naturalCellSize, rowGap, _) =
            solveEvenEdgeGrid(availableWidth, tileMargin, cellSizeScale, columns, evenEdgeSpacing)

        val expandBarH = 16.dp
        val tilesH =
            (naturalCellSize * collapsedRows) + (rowGap * (collapsedRows - 1).coerceAtLeast(0))

        val tileAreaH = tilesH + (contentPaddingV * 2) + 4.dp
        val collapsedH = tileAreaH + expandBarH

        val cornerRadius by animateDpAsState(
            targetValue = when {
                isExpanded         -> 24.dp
                collapsedRows == 1 -> 200.dp
                else               -> 28.dp
            },
            animationSpec = if (animateSize) {
                tween(durationMillis = 420, easing = FastOutSlowInEasing)
            } else {
                snap()
            },
            label = "OneUICornerRadius",
        )
        val containerShape = RoundedCornerShape(cornerRadius)

        val contentAlpha = remember { Animatable(1f) }
        LaunchedEffect(isExpanded, animateSize) {
            if (animateSize) {
                contentAlpha.snapTo(0.82f)
                contentAlpha.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
            } else {
                contentAlpha.snapTo(1f)
            }
        }

        val primary = MaterialTheme.colorScheme.primary

        val hoverPulse = remember { Animatable(0f) }
        LaunchedEffect(isDropHover) {
            if (isDropHover) {
                hoverPulse.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                )
            } else {
                hoverPulse.stop()
                hoverPulse.animateTo(0f, tween(durationMillis = 200))
            }
        }

        val editOverlayVisible = isEditMode && !isDropHover
        val editOverlayAlpha by animateFloatAsState(
            targetValue = if (editOverlayVisible) 1f else 0f,
            animationSpec = tween(durationMillis = if (editOverlayVisible) 260 else 140, easing = FastOutSlowInEasing),
            label = "OneUIEditOverlayAlpha",
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    when {
                        isDropHover -> Modifier
                            .border(
                                3.dp,
                                primary.copy(alpha = 0.4f + hoverPulse.value * 0.6f),
                                containerShape,
                            )
                        isEditMode -> Modifier.border(2.dp, primary.copy(alpha = 0.5f), containerShape)
                        else -> Modifier
                    }
                )

                .background(
                    color = if (isDropHover)
                        primary.copy(alpha = 0.08f + hoverPulse.value * 0.07f)
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                    shape = containerShape
                )
                .then(
                    if (animateSize) {
                        Modifier.animateContentSize(tween(durationMillis = 420, easing = FastOutSlowInEasing))
                    } else {
                        Modifier.clipToBounds()
                    }
                )
                .then(
                    if (isExpanded) Modifier.wrapContentHeight()
                    else Modifier.height(collapsedH)
                )
        ) {
            val barReserve = when {
                !isExpanded -> 0.dp
                isEditMode -> 16.dp
                else -> 28.dp
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!isExpanded) Modifier.height(collapsedH)
                        else Modifier.wrapContentHeight()
                    )
                    .padding(bottom = barReserve),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = contentPaddingH)
                        .then(
                            if (isExpanded) {
                                Modifier.padding(vertical = contentPaddingV)
                            } else {
                                Modifier.heightIn(max = tilesH).clipToBounds()
                            }
                        )
                        .alpha(contentAlpha.value)
                        .alpha(1f - editOverlayAlpha * 0.55f),
                    contentAlignment = Alignment.Center,
                ) {
                    Box {
                        content()
                    }
                }

                if (editOverlayAlpha > 0f) {
                    TextButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .zIndex(1f)
                            .alpha(editOverlayAlpha)
                            .scale(0.85f + 0.15f * editOverlayAlpha),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                .copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(stringResource(R.string.qs_edit))
                    }
                }
            }

            if (!isEditMode) {
                OneUIExpandBar(
                    isExpanded = isExpanded,
                    onExpandChange = onExpandChange,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
fun OneUIExpandBar(
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val threshold = 40f
    val interactionSource = remember { MutableInteractionSource() }

    val handleWidth by animateDpAsState(
        targetValue = if (isExpanded) 32.dp else 44.dp,
        animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
        label = "expand_handle_w",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isExpanded) 28.dp else 14.dp)
            .background(Color.Transparent)
            .pointerInput(isExpanded) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (abs(dragOffset) > threshold) {
                            if (dragOffset < 0 && !isExpanded) onExpandChange(true)
                            else if (dragOffset > 0 && isExpanded) onExpandChange(false)
                        }
                        dragOffset = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                    }
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onExpandChange(!isExpanded)
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(handleWidth)
                .height(3.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    RoundedCornerShape(50),
                )
        )
    }
}

@Composable
fun OneUiInsideTileArea(
    insideSpecs: List<TileSpec>,
    allTilesMap: Map<TileSpec, QSTile>,
    isEditMode: Boolean,
    containerBounds: Rect,
    onTileClick: (TileSpec, Expandable) -> Unit,
    onReorder: (List<TileSpec>) -> Unit,
    onDragOut: (TileSpec) -> Unit,
    modifier: Modifier = Modifier,
    isDropTarget: Boolean = false,
    tileBrightness: @Composable (isVertical: Boolean) -> Unit = {},
    layoutRootCoordinates: LayoutCoordinates? = null,
    compactTiles: Boolean = false,
    gridSpacing: Dp = 12.dp,
    cellSizeScale: Float = 1f,
    evenEdgeSpacing: Boolean = false,
    interactionsEnabled: Boolean = true,
    animateSize: Boolean = true,
) {

    val floatingTiles = remember(insideSpecs) {
        insideSpecs.map { spec -> FloatingTile(spec, SectionType.TILES, 1, 1) }
    }

    if (floatingTiles.isEmpty() && !isEditMode && !isDropTarget) return

    var previousCount by remember { mutableIntStateOf(insideSpecs.size) }
    val justAdded = insideSpecs.size > previousCount
    LaunchedEffect(insideSpecs.size) { previousCount = insideSpecs.size }

    val landingScale = remember { Animatable(1f) }
    LaunchedEffect(justAdded) {
        if (justAdded) {
            landingScale.snapTo(0.92f)
            landingScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
            )
        }
    }

    var localRootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val isFullScreenEdit = containerBounds == Rect.Zero
    val activeDropTarget = isDropTarget && isFullScreenEdit

    Box(
        modifier = modifier
            .scale(landingScale.value)
            .then(
                if (animateSize) {
                    Modifier.animateContentSize(tween(durationMillis = 280, easing = FastOutSlowInEasing))
                } else {
                    Modifier
                }
            )
            .onGloballyPositioned { localRootCoordinates = it },
    ) {
        DraggableGrid(
            tiles = floatingTiles,
            allTilesMap = allTilesMap,
            isEditMode = isEditMode,
            sectionType = SectionType.TILES,
            columns = 4,
            isDropTarget = activeDropTarget,
            onTileClick = { tile, expandable -> if (!isEditMode) onTileClick(tile.spec, expandable) },
            onResize = { _, _, _ ->  },
            onRemove = { tileId ->
                val spec = floatingTiles.find { it.spec.spec == tileId }?.spec ?: return@DraggableGrid
                onDragOut(spec)
            },
            onMove = { reorderedTiles -> onReorder(reorderedTiles.map { it.spec }) },
            onDragEnd = { spec ->
                FloatingTileDragState.endDrag()
            },
            tileBrightness = tileBrightness,
            layoutRootCoordinates = localRootCoordinates ?: layoutRootCoordinates,
            useAospStyle = true,
            isOneUi = true,
            cellSizeScale = cellSizeScale,
            gridSpacingParam = gridSpacing,
            evenEdgeSpacing = evenEdgeSpacing,
            interactionsEnabled = interactionsEnabled,
        )

        val isHoveringThisContainer = activeDropTarget && FloatingTileDragState.isDragging

        if (isHoveringThisContainer && localRootCoordinates != null) {
            val draggingSpec = FloatingTileDragState.draggingTileSpec
            val ghost = FloatingTileDragState.ghostFloatingTile
            if (draggingSpec != null && ghost != null && ghost.spec == draggingSpec) {
                val rootX = localRootCoordinates!!.positionInRoot().x
                val rootY = localRootCoordinates!!.positionInRoot().y

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (FloatingTileDragState.tileTopLeft.x - rootX).roundToInt(),
                                (FloatingTileDragState.tileTopLeft.y - rootY).roundToInt(),
                            )
                        }
                        .size(FloatingTileDragState.draggedTileSize)
                        .zIndex(100f)
                ) {
                    DraggableTile(
                        tile = ghost,
                        qsTile = allTilesMap[draggingSpec],
                        onClick = {},
                        gridCellSize = 0.dp,
                        isEditMode = true,
                        isResizing = false,
                        elevation = 8.dp,
                        onRemove = {},
                        onResizeStart = {},
                        onUpdateGhost = { _, _ -> },
                        onPreviewResize = { _, _ -> },
                        onResizeEnd = {},
                        tileBrightness = tileBrightness,
                        editTile = FloatingTileDragState.ghostEditTile,
                        useAospStyle = true,
                        isOneUi = true,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OneUiEditScreen(
    insideSpecs: List<TileSpec>,
    allTilesMap: Map<TileSpec, QSTile>,
    onReorder: (List<TileSpec>) -> Unit,
    onTileRemoved: (TileSpec) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val surfaceEffect2 = LocalAndroidColorScheme.current.surfaceEffect2
    val primaryColor = MaterialTheme.colorScheme.primary
    var screenRootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                ),
                title = {
                    Text(
                        text = stringResource(R.string.qs_edit_tiles),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        modifier = Modifier.padding(start = 24.dp),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDone,
                        modifier = Modifier.drawBehind { drawCircle(surfaceEffect2) },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            tint = Color.White,
                            contentDescription = stringResource(
                                com.android.internal.R.string.action_bar_up_description
                            ),
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 48.dp, bottom = 8.dp, start = 18.dp),
            )
        },
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.qs_label_container_margin)
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .onGloballyPositioned { screenRootCoordinates = it },
        ) {
            OneUiEditGridHeader(isEmpty = insideSpecs.isEmpty())

            if (insideSpecs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No tiles yet.\nDrag tiles in from the main screen to add them here.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier.padding(32.dp),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = EditModeTileDefaults.CurrentTilesGridPadding + 16.dp,
                        )
                        .border(
                            width = 2.dp,
                            color = primaryColor,
                            shape = RoundedCornerShape(EditModeTileDefaults.GridBackgroundCornerRadius),
                        )
                        .drawBehind {
                            drawRoundRect(
                                color = primaryColor,
                                cornerRadius = CornerRadius(
                                    EditModeTileDefaults.GridBackgroundCornerRadius.toPx()
                                ),
                                alpha = 0.15f,
                            )
                        }
                        .padding(EditModeTileDefaults.CurrentTilesGridPadding),
                ) {
                    OneUiInsideTileArea(
                        insideSpecs = insideSpecs,
                        allTilesMap = allTilesMap,
                        isEditMode = true,
                        isDropTarget = true,
                        containerBounds = Rect.Zero,
                        onTileClick = { _, _ -> },
                        onReorder = onReorder,
                        onDragOut = onTileRemoved,
                        layoutRootCoordinates = screenRootCoordinates,
                        gridSpacing = OneUiEditScreenDefaults.GridSpacing,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private object OneUiEditScreenDefaults {
    val GridSpacing = 20.dp
}

@Composable
private fun OneUiEditGridHeader(isEmpty: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
    ) {
        Text(
            text = if (isEmpty)
                "Drag a tile onto this screen to add it here"
            else
                stringResource(R.string.select_to_rearrange_tiles),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            color = Color.White,
        )
    }
}
