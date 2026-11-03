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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.systemui.qs.panels.domain.interactor.QqsTilesInteractor
import com.android.systemui.qs.panels.ui.compose.EditTileListState
import com.android.systemui.qs.panels.ui.viewmodel.EditTileViewModel
import com.android.systemui.qs.pipeline.shared.TileSpec

@Composable
fun QqsEditGrid(
    allTiles: List<EditTileViewModel>,
    @Suppress("UNUSED_PARAMETER") qqsColumns: Int,
    qqsTilesInteractor: QqsTilesInteractor,
    ensureCurrent: (TileSpec) -> Unit,
    releaseIfUnused: (TileSpec) -> Unit,
    onStopEditing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = androidx.compose.foundation.rememberScrollState()

    val qqsSpecs by
        qqsTilesInteractor.qqsTiles.collectAsStateWithLifecycle(initialValue = emptyList())

    val qqsScopedTiles =
        remember(allTiles, qqsSpecs) {
            allTiles.map { tile ->
                tile.copy(isCurrent = tile.tileSpec in qqsSpecs, spanCols = 1, spanRows = 1)
            }
        }

    val qqsTilesInOrder =
        remember(qqsScopedTiles, qqsSpecs) {
            qqsSpecs.mapNotNull { spec -> qqsScopedTiles.find { it.tileSpec == spec } }
        }

    val listState = remember {
        EditTileListState(
            initialTiles = qqsTilesInOrder,
            initialLargeTiles = emptySet(),
            columns = QQS_EDIT_GRID_COLUMNS,
            largeTilesSpan = 1,
            forceUnitSpan = true,
        )
    }

    LaunchedEffect(qqsTilesInOrder) { listState.updateTiles(qqsTilesInOrder, emptySet()) }

    QqsDefaultEditTileGrid(
        listState = listState,
        allTiles = qqsScopedTiles,
        modifier = modifier,
        scrollState = scrollState,
        onStopEditing = onStopEditing,
        onEditAction = editAction@{ action ->
            when (action) {
                is QqsEditAction.AddTile -> {
                    if (listState.tileSpecs().size >= QQS_EDIT_GRID_MAX_TILES) return@editAction
                    ensureCurrent(action.tileSpec)
                    qqsTilesInteractor.addTile(action.tileSpec)
                }
                is QqsEditAction.InsertTile -> {
                    val current = listState.tileSpecs().toMutableList()
                    val isNewTile = action.tileSpec !in current
                    if (isNewTile && current.size >= QQS_EDIT_GRID_MAX_TILES) {
                        return@editAction
                    }
                    ensureCurrent(action.tileSpec)
                    if (action.tileSpec in current) current.remove(action.tileSpec)
                    val insertAt = action.position.coerceIn(0, current.size)
                    current.add(insertAt, action.tileSpec)
                    qqsTilesInteractor.setQqsTiles(current)
                }
                is QqsEditAction.RemoveTile -> {
                    qqsTilesInteractor.removeTile(action.tileSpec)
                    releaseIfUnused(action.tileSpec)
                }
                is QqsEditAction.SetTiles -> {
                    qqsTilesInteractor.setQqsTiles(action.tileSpecs)
                }
            }
        },
    )
}
