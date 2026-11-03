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

package com.android.systemui.qs.panels.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import com.android.systemui.qs.panels.shared.model.GridPlacement
import com.android.systemui.qs.panels.shared.model.SizedTile
import com.android.systemui.qs.panels.shared.model.SizedTileImpl
import com.android.systemui.qs.panels.shared.model.TileGridConfig
import com.android.systemui.qs.panels.ui.compose.selection.PlacementEvent
import com.android.systemui.qs.panels.ui.model.GridCell
import com.android.systemui.qs.panels.ui.model.SpacerGridCell
import com.android.systemui.qs.panels.ui.model.TileGridCell
import com.android.systemui.qs.panels.ui.model.toGridCells
import com.android.systemui.qs.panels.ui.viewmodel.EditTileViewModel
import com.android.systemui.qs.pipeline.shared.TileSpec

/**
 * Holds the state for the tiles to display and builds a grid using their sizes and the available
 * columns.
 */
class EditTileListState(
    initialTiles: List<EditTileViewModel>,
    initialLargeTiles: Set<TileSpec>,
    val columns: Int,
    val largeTilesSpan: Int,
    private val forceUnitSpan: Boolean = false,
) : DragAndDropState {

    private val _tileGridConfigs =
        mutableMapOf<TileSpec, Pair<Int, Int>>().apply {
            initialTiles.forEach { tile -> put(tile.tileSpec, tile.spanCols to tile.spanRows) }
        }

    override var draggedCell by mutableStateOf<SizedTile<EditTileViewModel>?>(null)
        private set

    override var draggedPosition by mutableStateOf(Offset.Unspecified)
        private set

    override var dragType by mutableStateOf<DragType?>(null)
        private set

    // A dragged cell can be removed if it was added in the drag movement OR if it's marked as
    // removable
    override val isDraggedCellRemovable: Boolean
        get() = dragType == DragType.Add || draggedCell?.tile?.isRemovable ?: false

    override val dragInProgress: Boolean
        get() = draggedCell != null

    private val _tiles: SnapshotStateList<GridCell> =
        initialTiles.toGridCells(initialLargeTiles).toMutableStateList()
    val tiles: List<GridCell> = _tiles

    var largeTilesSpecs: Set<TileSpec> = initialLargeTiles
        private set

    /**
     * Updates the tiles list with new tiles, preserving grid configurations.
     */
    fun updateTiles(newTiles: List<EditTileViewModel>, newLargeTiles: Set<TileSpec>) {
        newTiles.forEach { tile ->
            _tileGridConfigs[tile.tileSpec] = tile.spanCols to tile.spanRows
        }

        largeTilesSpecs = newLargeTiles
        regenerateGridWithTiles(newTiles)
    }

    /**
     * Set the size of a specific tile.
     */
    fun setTileSize(spec: TileSpec, spanCols: Int, spanRows: Int) {
        _tileGridConfigs[spec] = spanCols.coerceIn(1, 4) to spanRows.coerceIn(1, 3)

        val tileIndex = _tiles.indexOfFirst { it is TileGridCell && it.tile.tileSpec == spec }
        if (tileIndex != -1) {
            val cell = _tiles[tileIndex] as TileGridCell
            _tiles[tileIndex] = cell.copy(width = getTileSize(spec).first)
            regenerateGrid()
        }
    }

    /**
     * Get the size of a tile (spanCols x spanRows).
     */
    fun getTileSize(spec: TileSpec): Pair<Int, Int> {
        if (forceUnitSpan) return 1 to 1
        return _tileGridConfigs[spec] ?: (1 to 1)
    }

    fun tileSpecs(): List<TileSpec> {
        return _tiles.filterIsInstance<TileGridCell>().map { it.tile.tileSpec }
    }

    /**
     * Finds the closest tile to the given tileSpec.
     *
     * If the given tileSpec is found in the list of tileSpecs, this function returns the next tile
     * in the list. If the given tileSpec is the last tile in the list, this function returns the
     * previous tile in the list. If the given tileSpec is not found in the list of tileSpecs, this
     * function returns null.
     *
     * @param tileSpec The tileSpec to find the closest tile to.
     * @return The closest tile to the given tileSpec, or null if the given tileSpec is not found.
     */
    fun findNeighboringTile(tileSpec: TileSpec): TileSpec? {
        val specs = tileSpecs()
        return when (val index = specs.indexOf(tileSpec)) {
            -1 -> null
            specs.size - 1 -> { // Last element
                if (specs.size > 1) {
                    specs[index - 1]
                } else {
                    null
                }
            }
            else -> specs[index + 1] // Return next element
        }
    }

    private fun indexOf(tileSpec: TileSpec): Int {
        return _tiles.indexOfFirst { it is TileGridCell && it.tile.tileSpec == tileSpec }
    }

    override fun onMoved(position: Offset) {
        draggedPosition = position
    }

    override fun isMoving(tileSpec: TileSpec): Boolean {
        return draggedCell?.let { it.tile.tileSpec == tileSpec } ?: false
    }

    override fun onStarted(cell: SizedTile<EditTileViewModel>, dragType: DragType) {
        draggedCell = cell
        this.dragType = dragType
    }

    override fun onTargeting(target: Int, insertAfter: Boolean) {
        val draggedTile = draggedCell ?: return
        val fromIndex = indexOf(draggedTile.tile.tileSpec)
        if (fromIndex == INVALID_INDEX) {
            return
        }

        val targetIndex =
            targetIndexForPlacement(
                PlacementEvent.PlaceToIndex(
                    movingSpec = draggedTile.tile.tileSpec,
                    targetIndex = target,
                )
            )

        val movingItem = _tiles.removeAt(fromIndex) as TileGridCell
        _tiles.add(targetIndex, movingItem)
        regenerateGrid()
    }

    override fun onDrop() {
        draggedCell = null
        draggedPosition = Offset.Unspecified
        dragType = null
        // Remove the spacers
        regenerateGrid()
    }

    /** Resize the tile corresponding to the [TileSpec] to [toIcon] */
    fun resizeTile(tileSpec: TileSpec, toIcon: Boolean) {
        val fromIndex = indexOf(tileSpec)
        if (fromIndex != INVALID_INDEX) {
            val cell = _tiles[fromIndex] as TileGridCell

            if (cell.isIcon == toIcon) return

            _tiles[fromIndex] = cell.copy(width = if (toIcon) 1 else largeTilesSpan)
            regenerateGrid(fromIndex)
        }
    }

    override fun movedOutOfBounds() {
        val draggedTile = draggedCell ?: return

        _tiles.removeIf { cell ->
            cell is TileGridCell && cell.tile.tileSpec == draggedTile.tile.tileSpec
        }
        draggedPosition = Offset.Unspecified

        // Regenerate spacers without the dragged tile
        regenerateGrid()
    }

    /**
     * Return the appropriate index to move the tile to for the placement [event]
     *
     * The grid includes spacers. As a result, indexes from the grid need to be translated to the
     * corresponding index from [currentTileSpecs].
     */
    fun targetIndexForPlacement(event: PlacementEvent): Int {
        val currentTileSpecs = tileSpecs()
        return when (event) {
            is PlacementEvent.PlaceToTileSpec -> {
                currentTileSpecs.indexOf(event.targetSpec)
            }
            is PlacementEvent.PlaceToIndex -> {
                if (event.targetIndex >= _tiles.size) {
                    currentTileSpecs.size
                } else if (event.targetIndex <= 0) {
                    0
                } else {
                    // The index may point to a spacer, so first find the first tile located
                    // after index, then use its position as a target
                    val targetTile =
                        _tiles.subList(event.targetIndex, _tiles.size).firstOrNull {
                            it is TileGridCell
                        } as? TileGridCell

                    if (targetTile == null) {
                        currentTileSpecs.size
                    } else {
                        val targetIndex = currentTileSpecs.indexOf(targetTile.tile.tileSpec)
                        val fromIndex = currentTileSpecs.indexOf(event.movingSpec)
                        if (fromIndex < targetIndex) targetIndex - 1 else targetIndex
                    }
                }
            }
        }
    }

    /**
     * Calculate grid positions for tiles with custom sizes.
     */
    private fun calculateGridPositions(tiles: List<TileGridCell>): List<GridPlacement> {
        val placements = mutableListOf<GridPlacement>()
        val occupied = mutableSetOf<Pair<Int, Int>>()

        tiles.forEach { tile ->
            val (spanCols, spanRows) = getTileSize(tile.tile.tileSpec)
            var placed = false
            var searchRow = 0

            while (!placed) {
                for (col in 0 until columns) {
                    if (col + spanCols > columns) continue

                    var fits = true
                    for (r in 0 until spanRows) {
                        for (c in 0 until spanCols) {
                            if (occupied.contains((searchRow + r) to (col + c))) {
                                fits = false
                                break
                            }
                        }
                        if (!fits) break
                    }

                    if (fits) {
                        placements.add(GridPlacement(col, searchRow, spanCols, spanRows))
                        for (r in 0 until spanRows) {
                            for (c in 0 until spanCols) {
                                occupied.add((searchRow + r) to (col + c))
                            }
                        }
                        placed = true
                        break
                    }
                }
                if (!placed) searchRow++
            }
        }

        return placements
    }

    private fun List<EditTileViewModel>.toGridCells(largeTiles: Set<TileSpec>): List<GridCell> {
        val sizedTiles =
            map {
                SizedTileImpl(
                    it,
                    if (forceUnitSpan) 1 else if (largeTiles.contains(it.tileSpec)) largeTilesSpan else 1,
                )
            }
        return sizedTiles.toGridCellsWithSizes(0)
    }

    /**
     * Regenerate grid with new tiles list.
     */
    private fun regenerateGridWithTiles(newTiles: List<EditTileViewModel>) {
        val currentTileCells = _tiles.filterIsInstance<TileGridCell>()
        val currentSpecs = currentTileCells.map { it.tile.tileSpec }.toSet()
        val newSpecs = newTiles.map { it.tileSpec }.toSet()

        val tilesToKeep = currentTileCells.filter { it.tile.tileSpec in newSpecs }

        val specsToAdd = newSpecs - currentSpecs
        val tilesToAdd = newTiles.filter { it.tileSpec in specsToAdd }

        val updatedTiles =
            tilesToKeep
                .map { cell ->
                    val newTile = newTiles.find { it.tileSpec == cell.tile.tileSpec }
                    if (newTile != null) {
                        cell.copy(tile = newTile)
                    } else {
                        cell
                    }
                }
                .map { it.tile } + tilesToAdd

        _tiles.clear()

        val sizedTiles =
            updatedTiles.map { tile ->
                val (spanCols, _) = getTileSize(tile.tileSpec)
                SizedTileImpl(tile, spanCols)
            }

        _tiles.addAll(sizedTiles.toGridCellsWithSizes(0))
    }

    /** Regenerate the list of [GridCell] with their new potential rows */
    private fun regenerateGrid() {
        val orderedItems = _tiles.filterIsInstance<TileGridCell>()
        _tiles.clear()

        val placements = calculateGridPositions(orderedItems)

        orderedItems.forEachIndexed { index, item ->
            val placement = placements.getOrElse(index) { GridPlacement(0, 0, 1, 1) }
            val (spanCols, _) = getTileSize(item.tile.tileSpec)

            _tiles.add(
                item.copy(row = placement.gridY, column = placement.gridX, width = spanCols)
            )
        }
    }

    /**
     * Regenerate the grid of [GridCell] with their new potential rows from [fromIndex], leaving
     * cells before that untouched.
     */
    private fun regenerateGrid(fromIndex: Int) {
        val fromRow = _tiles[fromIndex].row
        val (pre, post) = _tiles.partition { it.row < fromRow }
        val rebuilt =
            post
                .filterIsInstance<TileGridCell>()
                .map { SizedTileImpl(it.tile, getTileSize(it.tile.tileSpec).first) }
                .toGridCellsWithSizes(fromRow)
        _tiles.clear()
        _tiles.addAll(pre)
        _tiles.addAll(rebuilt)
    }

    /**
     * Convert sized tiles to grid cells with proper positioning.
     */
    private fun List<SizedTile<EditTileViewModel>>.toGridCellsWithSizes(
        startingRow: Int
    ): List<GridCell> {
        val cells = mutableListOf<GridCell>()
        val occupied = mutableSetOf<Pair<Int, Int>>()
        val startRow = startingRow
        var currentRow = startingRow

        this.forEach { sizedTile ->
            val tile = sizedTile.tile
            val (spanCols, spanRows) = getTileSize(tile.tileSpec)

            var placed = false
            var searchRow = currentRow

            while (!placed) {
                for (col in 0 until columns) {
                    if (col + spanCols > columns) continue

                    var fits = true
                    for (r in 0 until spanRows) {
                        for (c in 0 until spanCols) {
                            if (occupied.contains((searchRow + r) to (col + c))) {
                                fits = false
                                break
                            }
                        }
                        if (!fits) break
                    }

                    if (fits) {
                        cells.add(
                            TileGridCell(
                                sizedTile = sizedTile,
                                row = searchRow,
                                column = col,
                            )
                        )

                        for (r in 0 until spanRows) {
                            for (c in 0 until spanCols) {
                                occupied.add((searchRow + r) to (col + c))
                            }
                        }

                        placed = true
                        break
                    }
                }

                if (!placed) {
                    val usedInRow = occupied.count { it.first == searchRow }
                    if (usedInRow > 0 && usedInRow < columns) {
                        repeat(columns - usedInRow) { cells.add(SpacerGridCell(searchRow)) }
                    }
                    searchRow++
                }
            }
        }

        return cells
    }

    companion object {
        const val INVALID_INDEX = -1
    }
}
