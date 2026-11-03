/*
 * Copyright (C) 2026 RisingOS (revived) Android Project
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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.zIndex
import com.android.compose.gesture.effect.rememberOffsetOverscrollEffectFactory
import com.android.compose.modifiers.height
import com.android.compose.modifiers.thenIf
import com.android.compose.theme.LocalAndroidColorScheme
import com.android.systemui.common.ui.compose.load
import com.android.systemui.compose.modifiers.sysuiResTag
import com.android.systemui.qs.panels.shared.model.SizedTileImpl
import com.android.systemui.qs.panels.ui.compose.DragAndDropState
import com.android.systemui.qs.panels.ui.compose.DragType
import com.android.systemui.qs.panels.ui.compose.EditTileListState
import com.android.systemui.qs.panels.ui.compose.EditTileListState.Companion.INVALID_INDEX
import com.android.systemui.qs.panels.ui.compose.dragAndDropRemoveZone
import com.android.systemui.qs.panels.ui.compose.dragAndDropTileList
import com.android.systemui.qs.panels.ui.compose.dragAndDropTileSource
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.TileArrangementPadding
import com.android.systemui.qs.panels.ui.compose.selection.MutableSelectionState
import com.android.systemui.qs.panels.ui.compose.selection.StaticTileBadge
import com.android.systemui.qs.panels.ui.compose.selection.rememberSelectionState
import com.android.systemui.qs.panels.ui.model.GridCell
import com.android.systemui.qs.panels.ui.model.SpacerGridCell
import com.android.systemui.qs.panels.ui.model.TileGridCell
import com.android.systemui.qs.panels.ui.viewmodel.EditTileViewModel
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.qs.shared.model.TileCategory
import com.android.systemui.qs.shared.model.groupAndSort
import com.android.systemui.qs.ui.compose.borderOnFocus
import com.android.systemui.res.R
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private object QqsTileType

sealed interface QqsEditAction {
    data class AddTile(val tileSpec: TileSpec) : QqsEditAction

    data class InsertTile(val tileSpec: TileSpec, val position: Int) : QqsEditAction

    data class RemoveTile(val tileSpec: TileSpec) : QqsEditAction

    data class SetTiles(val tileSpecs: List<TileSpec>) : QqsEditAction
}

const val QQS_EDIT_GRID_COLUMNS = 5
const val QQS_EDIT_GRID_MAX_ROWS = 2

const val QQS_EDIT_GRID_MAX_TILES = QQS_EDIT_GRID_COLUMNS * QQS_EDIT_GRID_MAX_ROWS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QqsDefaultEditTileGrid(
    listState: EditTileListState,
    allTiles: List<EditTileViewModel>,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onStopEditing: () -> Unit = {},
    onEditAction: (QqsEditAction) -> Unit = {},
) {
    val selectionState = rememberSelectionState()

    QqsAutoSelectTiles(listState, selectionState)

    LaunchedEffect(selectionState.placementEvent) {
        selectionState.placementEvent?.let { event ->
            listState
                .targetIndexForPlacement(event)
                .takeIf { it != INVALID_INDEX }
                ?.let { onEditAction(QqsEditAction.InsertTile(event.movingSpec, it)) }
        }
    }

    val surfaceEffect2 = LocalAndroidColorScheme.current.surfaceEffect2

    Scaffold(
        modifier = modifier.consumeWindowInsets(WindowInsets.displayCutout).sysuiResTag(QQS_EDIT_MODE_ROOT_TEST_TAG),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors =
                    TopAppBarDefaults.topAppBarColors(
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
                        onClick = onStopEditing,
                        modifier = Modifier.drawBehind { drawCircle(surfaceEffect2) },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            tint = Color.White,
                            contentDescription =
                                stringResource(com.android.internal.R.string.action_bar_up_description),
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                modifier =
                    Modifier.statusBarsPadding().padding(top = 48.dp, bottom = 8.dp, start = 18.dp),
            )
        },
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalOverscrollFactory provides rememberOffsetOverscrollEffectFactory()
        ) {
            QqsAutoScrollGrid(listState, scrollState, innerPadding)

            LaunchedEffect(listState.dragType) {
                if (listState.dragInProgress && listState.dragType == DragType.Add) {
                    scrollState.animateScrollTo(0)
                }
            }

            QqsNavBarInsetScrollZone {
                QqsEdgeFade(scrollState = scrollState) {
                    QqsEditModeScrollableColumn(
                        listState = listState,
                        selectionState = selectionState,
                        innerPadding = innerPadding,
                        scrollState = scrollState,
                        onEditAction = onEditAction,
                    ) {
                        Spacer(Modifier.height(16.dp))

                        QqsCurrentTilesGrid(
                            listState = listState,
                            selectionState = selectionState,
                            onEditAction = onEditAction,
                        )

                        QqsAnimatedAvailableTilesGrid(
                            allTiles = allTiles,
                            listState = listState,
                            selectionState = selectionState,
                            onEditAction = onEditAction,
                            canLayoutTile = true,
                            isAtCapacity = listState.tileSpecs().size >= QQS_EDIT_GRID_MAX_TILES,
                            showAvailableTiles =
                                !(listState.dragInProgress || selectionState.placementEnabled) ||
                                    listState.dragType == DragType.Move,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QqsEditModeScrollableColumn(
    listState: EditTileListState,
    selectionState: MutableSelectionState,
    innerPadding: PaddingValues,
    scrollState: ScrollState,
    onEditAction: (QqsEditAction) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(dimensionResource(id = R.dimen.qs_label_container_margin)),
        modifier =
            modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .clipScrollableContainer(Orientation.Vertical)
                .verticalScroll(scrollState)
                .padding(horizontal = QqsEditGridHorizontalPadding)
                .dragAndDropRemoveZone(listState) { spec, removalEnabled ->
                    if (removalEnabled) {
                        onEditAction(QqsEditAction.RemoveTile(spec))
                    } else {
                        onEditAction(QqsEditAction.SetTiles(listState.tileSpecs()))
                        selectionState.select(spec)
                    }
                },
    ) {
        content()

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
    }
}

@Composable
private fun QqsEdgeFade(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scrollY = scrollState.value
    val maxScroll = scrollState.maxValue

    Box(
        modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()

                val fadeH = QQS_EDGE_FADE_HEIGHT.toPx()

                if (scrollY > 0) {
                    val progress = (scrollY.toFloat() / fadeH).coerceIn(0f, 1f)
                    drawRect(
                        brush =
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black),
                                startY = 0f,
                                endY = fadeH * progress,
                            ),
                        size = size.copy(height = fadeH),
                        blendMode = BlendMode.DstIn,
                    )
                }

                if (scrollY < maxScroll) {
                    val distToBottom = (maxScroll - scrollY).toFloat()
                    val progress = (distToBottom / fadeH).coerceIn(0f, 1f)
                    drawRect(
                        brush =
                            Brush.verticalGradient(
                                colors = listOf(Color.Black, Color.Transparent),
                                startY = size.height - fadeH * progress,
                                endY = size.height,
                            ),
                        topLeft = Offset(0f, size.height - fadeH),
                        size = size.copy(height = fadeH),
                        blendMode = BlendMode.DstIn,
                    )
                }
            }
    ) {
        content()
    }
}

@Composable
private fun QqsNavBarInsetScrollZone(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth()) {
        content()

        Box(
            Modifier.align(Alignment.BottomCenter)
                .zIndex(2f)
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .scrollable(rememberScrollableState { 0f }, orientation = Orientation.Vertical)
        )
    }
}

@Composable
private fun QqsAutoSelectTiles(listState: EditTileListState, selectionState: MutableSelectionState) {
    val specs = listState.tileSpecs()
    val selection = selectionState.selection

    LaunchedEffect(listState.dragInProgress, selection, specs) {
        if (listState.dragInProgress) return@LaunchedEffect

        val selectFirstTile = selection == null || selection !in specs

        if (selectFirstTile) {
            delay(AUTO_SELECT_DEBOUNCE_MILLIS)
            specs.firstOrNull()?.let { firstSpec -> selectionState.select(firstSpec) }
        }
    }
}

@Composable
private fun QqsAutoScrollGrid(
    listState: EditTileListState,
    scrollState: ScrollState,
    padding: PaddingValues,
) {
    val density = LocalDensity.current
    val (top, bottom) =
        remember(density) {
            with(density) {
                padding.calculateTopPadding().roundToPx() to
                    padding.calculateBottomPadding().roundToPx()
            }
        }
    val scrollTarget by
        remember(listState, scrollState, top, bottom) {
            derivedStateOf {
                val position = listState.draggedPosition
                if (position.isSpecified) {
                    val y = position.y.roundToInt()
                    when {
                        y < AUTO_SCROLL_DISTANCE + top -> 0
                        y > scrollState.viewportSize - bottom - AUTO_SCROLL_DISTANCE ->
                            scrollState.maxValue
                        else -> null
                    }
                } else {
                    null
                }
            }
        }
    LaunchedEffect(scrollTarget) {
        scrollTarget?.let {
            val distance = abs(it - scrollState.value)
            scrollState.animateScrollTo(
                it,
                animationSpec =
                    tween(durationMillis = distance * AUTO_SCROLL_SPEED, easing = LinearEasing),
            )
        }
    }
}

@Composable
private fun QqsCurrentTilesGrid(
    listState: EditTileListState,
    selectionState: MutableSelectionState,
    onEditAction: (QqsEditAction) -> Unit,
) {
    val currentListState by rememberUpdatedState(listState)
    val totalRows =
        (listState.tiles.maxOfOrNull { it.row } ?: 0).coerceAtMost(QQS_EDIT_GRID_MAX_ROWS - 1)
    val totalHeight by
        animateDpAsState(
            qqsGridHeight(
                totalRows + 1,
                QqsEditTileDiameter,
                TileArrangementPadding,
                QqsCurrentTilesGridPadding,
            ),
            label = "QqsEditCurrentTilesGridHeight",
        )
    val gridState = rememberLazyGridState()
    var gridContentOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset(0f, 0f)) }
    val coroutineScope = rememberCoroutineScope()

    val primaryColor = MaterialTheme.colorScheme.primary
    QqsTileLazyGrid(
        state = gridState,
        columns = GridCells.Fixed(QQS_EDIT_GRID_COLUMNS),
        contentPadding = PaddingValues(QqsCurrentTilesGridPadding),
        modifier =
            Modifier.fillMaxWidth()
                .height { totalHeight.roundToPx() }
                .border(
                    width = 2.dp,
                    color = primaryColor,
                    shape = RoundedCornerShape(QqsGridBackgroundCornerRadius),
                )
                .dragAndDropTileList(gridState, { gridContentOffset }, listState) { spec ->
                    onEditAction(QqsEditAction.SetTiles(currentListState.tileSpecs()))
                    selectionState.select(spec)
                }
                .onGloballyPositioned { coordinates ->
                    gridContentOffset = coordinates.positionInRoot()
                }
                .drawBehind {
                    drawRoundRect(
                        primaryColor,
                        cornerRadius = CornerRadius(QqsGridBackgroundCornerRadius.toPx()),
                        alpha = .15f,
                    )
                }
                .sysuiResTag(QQS_CURRENT_TILES_GRID_TEST_TAG),
    ) {
        QqsEditTiles(
            listState = listState,
            selectionState = selectionState,
            gridState = gridState,
            coroutineScope = coroutineScope,
            onRemoveTile = { onEditAction(QqsEditAction.RemoveTile(it)) },
        )
    }
}

@Composable
private fun QqsTileLazyGrid(
    columns: GridCells,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        state = state,
        columns = columns,
        verticalArrangement = spacedBy(TileArrangementPadding),
        horizontalArrangement = spacedBy(TileArrangementPadding),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = modifier,
        content = content,
    )
}

@Composable
private fun QqsAnimatedAvailableTilesGrid(
    allTiles: List<EditTileViewModel>,
    listState: EditTileListState,
    selectionState: MutableSelectionState,
    showAvailableTiles: Boolean,
    canLayoutTile: Boolean,
    isAtCapacity: Boolean,
    onEditAction: (QqsEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        Modifier.fillMaxWidth().requiredHeightIn(QqsAvailableTilesGridMinHeight).animateContentSize()
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = showAvailableTiles,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement =
                    spacedBy(dimensionResource(id = R.dimen.qs_label_container_margin)),
                modifier = modifier.fillMaxSize(),
            ) {
                QqsAvailableTileGrid(
                    allTiles,
                    selectionState,
                    QQS_EDIT_GRID_COLUMNS,
                    canLayoutTile = canLayoutTile,
                    isAtCapacity = isAtCapacity,
                    { onEditAction(QqsEditAction.AddTile(it)) },
                    listState,
                )
            }
        }
    }
}

@Composable
private fun QqsAvailableTileGrid(
    tiles: List<EditTileViewModel>,
    selectionState: MutableSelectionState,
    columns: Int,
    canLayoutTile: Boolean,
    isAtCapacity: Boolean,
    onAddTile: (TileSpec) -> Unit,
    dragAndDropState: DragAndDropState,
) {
    val groupedTileSpecs =
        remember(tiles.fastMap { it.category }, tiles.fastMap { it.label }) {
            groupAndSort(tiles).mapValues { tiles -> tiles.value.map { it.tileSpec } }
        }
    val viewModelsMap = remember(tiles) { tiles.associateBy { it.tileSpec } }

    Column(
        verticalArrangement = spacedBy(2.dp),
        horizontalAlignment = Alignment.Start,
        modifier =
            Modifier.fillMaxWidth().wrapContentHeight().sysuiResTag(QQS_AVAILABLE_TILES_GRID_TEST_TAG),
    ) {
        groupedTileSpecs.entries.forEachIndexed { index, (category, tileSpecs) ->
            key(category) {
                val shape =
                    when (index) {
                        0 ->
                            RoundedCornerShape(
                                topStart = QqsGridBackgroundCornerRadius,
                                topEnd = QqsGridBackgroundCornerRadius,
                            )
                        groupedTileSpecs.size - 1 ->
                            RoundedCornerShape(
                                bottomStart = QqsGridBackgroundCornerRadius,
                                bottomEnd = QqsGridBackgroundCornerRadius,
                            )
                        else -> RectangleShape
                    }
                Column(
                    verticalArrangement = spacedBy(16.dp),
                    modifier =
                        Modifier.background(
                                brush = SolidColor(MaterialTheme.colorScheme.surface),
                                shape = shape,
                                alpha = QQS_AVAILABLE_TILES_GRID_ALPHA,
                            )
                            .padding(16.dp),
                ) {
                    QqsCategoryHeader(
                        category,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    )
                    tileSpecs.chunked(columns).forEach { row ->
                        Row(
                            horizontalArrangement = spacedBy(TileArrangementPadding),
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        ) {
                            for (tileSpec in row) {
                                val viewModel = viewModelsMap[tileSpec] ?: continue
                                key(tileSpec) {
                                    QqsAvailableTileGridCell(
                                        cell = viewModel,
                                        dragAndDropState = dragAndDropState,
                                        selectionState = selectionState,
                                        canLayoutTile = canLayoutTile,
                                        isAtCapacity = isAtCapacity,
                                        onAddTile = onAddTile,
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                    )
                                }
                            }

                            repeat(columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

private fun qqsGridHeight(rows: Int, tileHeight: Dp, tilePadding: Dp, gridPadding: Dp): Dp {
    return ((tileHeight + tilePadding) * rows) + gridPadding * 2
}

private fun GridCell.qqsKey(index: Int): Any {
    return if (this is TileGridCell) key else index
}

private fun LazyGridScope.QqsEditTiles(
    listState: EditTileListState,
    selectionState: MutableSelectionState,
    gridState: LazyGridState,
    coroutineScope: CoroutineScope,
    onRemoveTile: (TileSpec) -> Unit,
) {
    itemsIndexed(
        items = listState.tiles,
        key = { index, item -> item.qqsKey(index) },
        span = { _, item -> item.span },
        contentType = { _, _ -> QqsTileType },
    ) { index, cell ->
        when (cell) {
            is TileGridCell ->
                if (listState.isMoving(cell.tile.tileSpec)) {
                    QqsSpacerGridCell(
                        Modifier.background(
                            color =
                                MaterialTheme.colorScheme.secondary.copy(
                                    alpha = QQS_PLACEHOLDER_ALPHA
                                ),
                            shape = CircleShape,
                        )
                    )
                } else {
                    QqsTileGridCell(
                        cell = cell,
                        index = index,
                        dragAndDropState = listState,
                        selectionState = selectionState,
                        onRemoveTile = onRemoveTile,
                        coroutineScope = coroutineScope,
                    )
                }
            is SpacerGridCell ->
                QqsSpacerGridCell(
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(onTap = { selectionState.onTap(index) })
                    }
                )
        }
    }
}

@Composable
private fun LazyGridItemScope.QqsTileGridCell(
    cell: TileGridCell,
    index: Int,
    dragAndDropState: DragAndDropState,
    selectionState: MutableSelectionState,
    onRemoveTile: (TileSpec) -> Unit,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val stateDescription = stringResource(id = R.string.accessibility_qs_edit_position, index + 1)
    val removeActionLabel = stringResource(R.string.accessibility_qs_edit_remove_tile_action)

    val colors = QqsEditModeTileDefaults.editTileColors()
    val removeTile = {
        selectionState.unSelect()
        onRemoveTile(cell.tile.tileSpec)
    }

    val draggableModifier =
        Modifier.dragAndDropTileSource(
            SizedTileImpl(cell.tile, cell.width),
            dragAndDropState,
            DragType.Move,
        ) {
            selectionState.select(cell.tile.tileSpec)
        }

    Box(
        modifier
            .height(QqsEditTileDiameter)
            .fillMaxWidth()
            .animateItem(
                placementSpec =
                    spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold,
                    )
            )
            .semantics(mergeDescendants = true) {
                this.stateDescription = stateDescription
                contentDescription = cell.tile.label.text
                customActions =
                    listOf(
                        CustomAccessibilityAction(removeActionLabel) {
                            removeTile()
                            true
                        }
                    )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(QqsEditTileDiameter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background, CircleShape)
                    .borderOnFocus(
                        MaterialTheme.colorScheme.secondary,
                        CircleShape.topStart,
                    )
                    .then(draggableModifier)
            ) {
                SmallTileContent(
                    iconProvider = { cell.tile.icon },
                    color = colors.icon,
                    animateToEnd = true,
                    modifier = Modifier.align(Alignment.Center).clearAndSetSemantics {},
                )
            }

            QqsRemoveBadge(
                onClick = removeTile,
                contentDescription = removeActionLabel,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = QqsRemoveBadgeInset, y = -QqsRemoveBadgeInset),
            )
        }
    }
}

@Composable
private fun QqsRemoveBadge(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(QqsRemoveBadgeSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(QqsRemoveBadgeSize - 6.dp),
        )
    }
}

@Composable
private fun QqsCategoryHeader(category: TileCategory, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(category.iconId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = category.label.load() ?: "",
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun QqsAvailableTileGridCell(
    cell: EditTileViewModel,
    dragAndDropState: DragAndDropState,
    selectionState: MutableSelectionState,
    canLayoutTile: Boolean,
    isAtCapacity: Boolean,
    onAddTile: (TileSpec) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAddDisabled = cell.isCurrent || isAtCapacity
    val stateDescription: String? =
        if (isAddDisabled) stringResource(R.string.accessibility_qs_edit_tile_already_added) else null

    val alpha by animateFloatAsState(if (isAddDisabled) .38f else 1f)
    val colors = QqsEditModeTileDefaults.editTileColors()
    val onClick: () -> Unit = {
        onAddTile(cell.tileSpec)
        if (canLayoutTile) {
            selectionState.select(cell.tileSpec)
        }
    }
    val clickLabel =
        stringResource(id = R.string.accessibility_qs_edit_named_tile_add_action, cell.label.text)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(CommonTileDefaults.StartPadding, Alignment.Top),
        modifier =
            modifier
                .graphicsLayer { this.alpha = alpha }
                .semantics(mergeDescendants = true) {
                    if (stateDescription != null) {
                        this.stateDescription = stateDescription
                    } else {
                        this.role = Role.Button
                    }
                }
                .sysuiResTag(QQS_AVAILABLE_TILE_TEST_TAG),
    ) {
        Box(
            Modifier.fillMaxWidth().height(QqsEditTileDiameter),
            contentAlignment = Alignment.Center,
        ) {
            val draggableModifier =
                if (isAddDisabled || !canLayoutTile) {
                    Modifier
                } else {
                    Modifier.dragAndDropTileSource(
                        SizedTileImpl(cell, 1),
                        dragAndDropState,
                        DragType.Add,
                    ) {
                        selectionState.select(cell.tileSpec)
                    }
                }
            Box(Modifier.size(QqsEditTileDiameter)) {
                Box(
                    Modifier.then(draggableModifier)
                        .fillMaxSize()
                        .background(colors.background, CircleShape)
                        .borderOnFocus(
                            MaterialTheme.colorScheme.secondary,
                            CircleShape.topStart,
                        )
                        .clickable(
                            enabled = !isAddDisabled,
                            onClick = onClick,
                            onClickLabel = clickLabel,
                        )
                ) {
                    SmallTileContent(
                        iconProvider = { cell.icon },
                        color = colors.icon,
                        animateToEnd = true,
                        modifier = Modifier.align(Alignment.Center).clearAndSetSemantics {},
                    )
                }

                StaticTileBadge(
                    icon = Icons.Default.Add,
                    contentDescription = clickLabel,
                    enabled = !isAddDisabled,
                    onClick = onClick,
                )
            }
        }
        Box(Modifier.fillMaxSize()) {
            Text(
                cell.label.text,
                maxLines = 2,
                color = colors.label,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium.copy(hyphens = Hyphens.Auto),
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun QqsSpacerGridCell(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .height(QqsEditTileDiameter)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = modifier.size(QqsEditTileDiameter))
    }
}

private object QqsEditModeTileDefaults {
    @Composable
    fun editTileColors(): TileColors =
        TileColors(
            background = LocalAndroidColorScheme.current.surfaceEffect1,
            iconBackground = LocalAndroidColorScheme.current.surfaceEffect2,
            label = MaterialTheme.colorScheme.onSurface,
            secondaryLabel = MaterialTheme.colorScheme.onSurface,
            icon = MaterialTheme.colorScheme.onSurface,
        )
}

private const val QQS_PLACEHOLDER_ALPHA = .3f
private const val AUTO_SCROLL_DISTANCE = 100
private const val AUTO_SCROLL_SPEED = 2
private const val QQS_AVAILABLE_TILES_GRID_ALPHA = .32f
private const val AUTO_SELECT_DEBOUNCE_MILLIS = 100L
private val QqsCurrentTilesGridPadding = 10.dp
private val QqsAvailableTilesGridMinHeight = 200.dp
private val QqsGridBackgroundCornerRadius = 28.dp
private val QQS_EDGE_FADE_HEIGHT = 32.dp
private val QqsRemoveBadgeSize = 20.dp

private val QqsEditTileDiameter = 52.dp

private val QqsRemoveBadgeInset = QqsRemoveBadgeSize * 0.225f

private val QqsEditGridHorizontalPadding = 16.dp

private const val QQS_EDIT_MODE_ROOT_TEST_TAG = "QqsEditModeRoot"
private const val QQS_CURRENT_TILES_GRID_TEST_TAG = "QqsCurrentTilesGrid"
private const val QQS_AVAILABLE_TILES_GRID_TEST_TAG = "QqsAvailableTilesGrid"
private const val QQS_AVAILABLE_TILE_TEST_TAG = "QqsAvailableTileTestTag"
