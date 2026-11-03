/*
 * Copyright (C) 2024 The Android Open Source Project
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.compose.animation.scene.ContentScope
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.grid.ui.compose.CustomVerticalSpannedGrid
import com.android.systemui.haptics.msdl.qs.TileHapticsViewModel
import com.android.systemui.lifecycle.rememberViewModel
import com.android.systemui.qs.panels.shared.model.SizedTileImpl
import com.android.systemui.qs.panels.shared.model.TileGridConfig
import com.android.systemui.qs.panels.ui.compose.EditTileListState
import com.android.systemui.qs.panels.ui.compose.PaginatableGridLayout
import com.android.systemui.qs.panels.ui.compose.TileListener
import com.android.systemui.qs.panels.ui.compose.bounceableInfo
import com.android.systemui.qs.panels.ui.viewmodel.BounceableTileViewModel
import com.android.systemui.qs.panels.ui.viewmodel.DetailsViewModel
import com.android.systemui.qs.panels.ui.viewmodel.EditTileViewModel
import com.android.systemui.qs.panels.ui.viewmodel.IconTilesViewModel
import com.android.systemui.qs.panels.ui.viewmodel.InfiniteGridViewModel
import com.android.systemui.qs.panels.ui.viewmodel.TextFeedbackContentViewModel
import com.android.systemui.qs.panels.ui.viewmodel.TileViewModel
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.qs.shared.ui.QuickSettings.Elements.toElementKey
import com.android.systemui.qs.ui.composable.QuickSettingsShade
import com.android.systemui.res.R
import com.android.systemui.shade.shared.flag.DualShadeFlag
import javax.inject.Inject
import kotlinx.coroutines.launch

val LocalQSCompactMode = compositionLocalOf { false }
val LocalCollapsedRows = compositionLocalOf { 2 }

@SysUISingleton
class InfiniteGridLayout
@Inject
constructor(
    private val detailsViewModel: DetailsViewModel,
    private val iconTilesViewModel: IconTilesViewModel,
    override val viewModelFactory: InfiniteGridViewModel.Factory,
    private val textFeedbackContentViewModelFactory: TextFeedbackContentViewModel.Factory,
    private val tileHapticsViewModelFactory: TileHapticsViewModel.Factory,
) : PaginatableGridLayout {

    @Composable
    override fun ContentScope.TileGrid(
        tiles: List<TileViewModel>,
        modifier: Modifier,
        listening: () -> Boolean,
        enableRevealEffect: Boolean,
    ) {
        val viewModel =
            rememberViewModel(traceName = "InfiniteGridLayout.TileGrid") {
                viewModelFactory.create()
            }

        val context = LocalContext.current
        val textFeedbackViewModel =
            rememberViewModel(traceName = "InfiniteGridLayout.TileGrid", key = context) {
                textFeedbackContentViewModelFactory.create(context)
            }

        val columns = viewModel.columnsWithMediaViewModel.columns
        val largeTilesSpan = viewModel.columnsWithMediaViewModel.largeSpan
        val largeTiles by viewModel.iconTilesViewModel.largeTiles.collectAsStateWithLifecycle()
        val tileGridConfigs by viewModel.iconTilesViewModel.tileGridConfigs.collectAsStateWithLifecycle()
        
        val isCompactMode = LocalQSCompactMode.current
        val collapsedRows = LocalCollapsedRows.current

        val sizedTiles =
            remember(tiles, largeTiles, largeTilesSpan, tileGridConfigs, isCompactMode) {
                tiles.map { tile ->
                    if (isCompactMode) {
                        SizedTileImpl(tile, 1)
                    } else {
                        val config = tileGridConfigs.find { it.spec == tile.spec }
                        val width = config?.spanCols ?: if (largeTiles.contains(tile.spec)) largeTilesSpan else 1
                        SizedTileImpl(tile, width)
                    }
                }
            }

        val squishiness by viewModel.squishinessViewModel.squishiness.collectAsStateWithLifecycle()
        val scope = rememberCoroutineScope()
        
        val effectiveColumns = if (isCompactMode) 4 else columns
        val spans by remember(sizedTiles) { derivedStateOf { sizedTiles.fastMap { it.width } } }

        val bounceables =
            remember(sizedTiles) { List(sizedTiles.size) { BounceableTileViewModel() } }

        val tilesToDisplay = if (isCompactMode) {
            val tilesPerRow = effectiveColumns
            val maxTiles = tilesPerRow * collapsedRows
            sizedTiles.take(maxTiles)
        } else {
            sizedTiles
        }

        CustomVerticalSpannedGrid(
            columns = effectiveColumns,
            rowSpacing = dimensionResource(R.dimen.qs_tile_margin_vertical),
            spans = spans.take(tilesToDisplay.size),
            keys = { tilesToDisplay[it].tile.spec },
            modifier = modifier,
        ) { spanIndex, column, isFirstInColumn, isLastInColumn ->
            val sizedTile = tilesToDisplay[spanIndex]
            
            val tileConfig = tileGridConfigs.find { config -> config.spec == sizedTile.tile.spec }
            val spanRows = if (isCompactMode) 1 else (tileConfig?.spanRows ?: 1)
            
            val isIconOnly = true

            Element(sizedTile.tile.spec.toElementKey(), Modifier) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Tile(
                        tile = sizedTile.tile,
                        iconOnly = isIconOnly,
                        spanRows = spanRows,
                        squishiness = { squishiness },
                        tileHapticsViewModelFactory = tileHapticsViewModelFactory,
                        coroutineScope = scope,
                        bounceableInfo =
                            bounceables.bounceableInfo(
                                sizedTile,
                                index = spanIndex,
                                column = column,
                                columns = effectiveColumns,
                                isFirstInRow = isFirstInColumn,
                                isLastInRow = isLastInColumn,
                            ),
                        detailsViewModel = detailsViewModel,
                        isVisible = listening,
                        requestToggleTextFeedback = { textFeedbackViewModel.requestShowFeedback(sizedTile.tile.spec) },
                        modifier = Modifier,
                        enableRevealEffect = enableRevealEffect,
                    )
                }
            }
        }

        TileListener(tilesToDisplay.map { it.tile }, listening)
    }

    @Composable
    override fun EditTileGrid(
        tiles: List<EditTileViewModel>,
        modifier: Modifier,
        onAddTile: (TileSpec, Int) -> Unit,
        onRemoveTile: (TileSpec) -> Unit,
        onSetTiles: (List<TileSpec>) -> Unit,
        onStopEditing: () -> Unit,
    ) {
        val viewModel =
            rememberViewModel(traceName = "InfiniteGridLayout.EditTileGrid") {
                viewModelFactory.create()
            }
        val columnsViewModel =
            rememberViewModel(traceName = "InfiniteGridLayout.EditTileGrid") {
                viewModel.columnsWithMediaViewModelFactory.createWithoutMediaTracking()
            }
        val snapshotViewModel =
            rememberViewModel("InfiniteGridLayout.EditTileGrid") {
                viewModel.snapshotViewModelFactory.create()
            }
        val topBarActionsViewModel =
            rememberViewModel("InfiniteGridLayout.EditTileGrid") {
                viewModel.editTopBarActionsViewModelFactory.create()
            }
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val dialogDelegate =
            rememberViewModel("InfiniteGridLayout.EditTileGrid") {
                viewModel.resetDialogDelegateFactory.create {
                    // Clear the stack of snapshots on reset
                    snapshotViewModel.clearStack()

                    // Automatically scroll to the top on reset
                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                }
            }
        val showDualShadeSetting =
            DualShadeFlag.isEnabled &&
                LocalResources.current.getBoolean(
                    com.android.settingslib.R.bool.config_useDualShadeSetting
                )
        val actions =
            remember(topBarActionsViewModel, showDualShadeSetting) {
                topBarActionsViewModel.actions(showDualShadeSetting).toMutableStateList()
            }
        val columns = columnsViewModel.columns
        val largeTilesSpan = columnsViewModel.largeSpan
        val largeTiles by viewModel.iconTilesViewModel.largeTiles.collectAsStateWithLifecycle()
        val tileGridConfigs by viewModel.iconTilesViewModel.tileGridConfigs.collectAsStateWithLifecycle()

        val tilesWithSizes =
            remember(tiles, tileGridConfigs) {
                val configsBySpec = tileGridConfigs.associateBy { it.spec }
                tiles.filter { it.isCurrent }.map { tile ->
                    val config = configsBySpec[tile.tileSpec]
                    if (config != null) {
                        tile.copy(spanCols = config.spanCols, spanRows = config.spanRows)
                    } else {
                        val defaultSize = TileGridConfig.getDefaultSize(tile.tileSpec)
                        tile.copy(spanCols = defaultSize.first, spanRows = defaultSize.second)
                    }
                }
            }
        val listState =
            remember(columns, largeTilesSpan, tileGridConfigs) {
                EditTileListState(
                    tilesWithSizes,
                    largeTiles,
                    columns = columns,
                    largeTilesSpan = largeTilesSpan,
                )
            }
        LaunchedEffect(tilesWithSizes, largeTiles) {
            listState.updateTiles(tilesWithSizes, largeTiles)
        }

        DefaultEditTileGrid(
            listState = listState,
            allTiles = tiles,
            modifier = modifier,
            scrollState = scrollState,
            snapshotViewModel = snapshotViewModel,
            topBarActions = actions,
            onStopEditing = onStopEditing,
            iconTilesViewModel = iconTilesViewModel,
            onEditAction = { action ->
                // Opening the dialog doesn't require a snapshot
                if (action != EditAction.ResetGrid) {
                    snapshotViewModel.takeSnapshot(tilesWithSizes.map { it.tileSpec }, largeTiles)
                }

                when (action) {
                    is EditAction.InsertTile -> {
                        onAddTile(action.tileSpec, action.position)
                    }
                    is EditAction.RemoveTile -> {
                        onRemoveTile(action.tileSpec)
                    }
                    is EditAction.ResizeTileGrid -> {
                        iconTilesViewModel.setTileSize(
                            action.tileSpec,
                            action.spanCols,
                            action.spanRows,
                            false
                        )
                    }
                    is EditAction.SetTiles -> {
                        onSetTiles(action.tileSpecs)
                    }
                    EditAction.ResetGrid -> {
                        dialogDelegate.showDialog()
                    }
                }
            }
        )
    }
}