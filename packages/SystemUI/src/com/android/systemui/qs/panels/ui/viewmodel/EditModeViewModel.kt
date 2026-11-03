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

package com.android.systemui.qs.panels.ui.viewmodel

import com.android.systemui.lifecycle.ExclusiveActivatable

import android.content.Context
import androidx.compose.ui.util.fastMap
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.android.internal.logging.UiEventLogger
import com.android.systemui.common.ui.domain.interactor.ConfigurationInteractor
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.qs.QSEditEvent
import com.android.systemui.qs.panels.domain.interactor.EditTilesListInteractor
import com.android.systemui.qs.panels.domain.interactor.GridLayoutTypeInteractor
import com.android.systemui.qs.panels.domain.interactor.TilesAvailabilityInteractor
import com.android.systemui.qs.panels.shared.model.GridLayoutType
import com.android.systemui.qs.panels.ui.compose.GridLayout
import com.android.systemui.qs.pipeline.domain.interactor.CurrentTilesInteractor
import com.android.systemui.qs.pipeline.domain.interactor.CurrentTilesInteractor.Companion.POSITION_AT_END
import com.android.systemui.qs.pipeline.domain.interactor.MinimumTilesInteractor
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.qs.pipeline.shared.metricSpec
import com.android.systemui.shade.ShadeDisplayAware
import com.android.systemui.util.kotlin.emitOnStart
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@SysUISingleton
class EditModeViewModel
@Inject
constructor(
    private val editTilesListInteractor: EditTilesListInteractor,
    private val currentTilesInteractor: CurrentTilesInteractor,
    private val tilesAvailabilityInteractor: TilesAvailabilityInteractor,
    private val minTilesInteractor: MinimumTilesInteractor,
    private val uiEventLogger: UiEventLogger,
    @ShadeDisplayAware private val configurationInteractor: ConfigurationInteractor,
    @ShadeDisplayAware internal val context: Context,
    @Named("Default") private val defaultGridLayout: GridLayout,
    @Application private val applicationScope: CoroutineScope,
    @Background private val bgDispatcher: CoroutineDispatcher,
    gridLayoutTypeInteractor: GridLayoutTypeInteractor,
    gridLayoutMap: Map<GridLayoutType, @JvmSuppressWildcards GridLayout>,
) : ExclusiveActivatable() {
    private val _isEditing = MutableStateFlow(false)
    
    private val _explicitTiles = MutableStateFlow<Set<TileSpec>?>(null)

    /**
     * Whether we should be editing right now. Use [startEditing] and [stopEditing] to change this.
     */
    val isEditing = _isEditing.asStateFlow()

    val gridLayout: StateFlow<GridLayout> =
        gridLayoutTypeInteractor.layout
            .map { gridLayoutMap[it] ?: defaultGridLayout }
            .stateIn(applicationScope, SharingStarted.WhileSubscribed(), defaultGridLayout)

    /**
     * Flow of view models for each tile that should be visible in edit mode (or empty flow when not
     * editing).
     */
    val tiles: Flow<List<EditTileViewModel>> =
        isEditing.flatMapLatest { isEditing ->
            if (isEditing) {
                val editTilesData = editTilesListInteractor.getTilesToEdit()
                val unavailable =
                    tilesAvailabilityInteractor.getUnavailableTiles(
                        editTilesData.stockTiles
                            .map { it.tileSpec }
                            .minus(currentTilesInteractor.currentTilesSpecs.toSet())
                    )
                combine(currentTilesInteractor.currentTiles, _explicitTiles) { currentTilesList, explicitSpecs ->
                        val currentSpecs = explicitSpecs ?: currentTilesList.map { it.spec }.toSet()
                        val dualTargetSpecs =
                            currentTilesList
                                .filter { it.tile.state.handlesSecondaryClick }
                                .map { it.spec }
                                .toSet()
                        val canRemoveTiles = currentSpecs.size > minTilesInteractor.minNumberOfTiles
                        val allTiles = editTilesData.stockTiles + editTilesData.customTiles
                        val allTilesMap = allTiles.associateBy { it.tileSpec }
                        val currentTiles = currentSpecs.mapNotNull { allTilesMap[it] }
                        val nonCurrentTiles = allTiles.filter { it.tileSpec !in currentSpecs }

                        (currentTiles + nonCurrentTiles)
                            .filterNot { it.tileSpec in unavailable }
                            .map {
                                val current = it.tileSpec in currentSpecs
                                val isDualTarget = current && it.tileSpec in dualTargetSpecs
                                val availableEditActions = buildSet {
                                    if (current) {
                                        add(AvailableEditActions.MOVE)
                                        if (canRemoveTiles) {
                                            add(AvailableEditActions.REMOVE)
                                        }
                                    } else {
                                        add(AvailableEditActions.ADD)
                                    }
                                }
                                UnloadedEditTileViewModel(
                                    it.tileSpec,
                                    it.icon,
                                    it.label,
                                    it.appName,
                                    it.appIcon,
                                    current,
                                    isDualTarget,
                                    availableEditActions,
                                    it.category,
                                )
                            }
                    }
                    .combine(configurationInteractor.onAnyConfigurationChange.emitOnStart()) {
                        tiles,
                        _ ->
                        tiles.fastMap { it.load(context) }
                    }
            } else {
                emptyFlow()
            }
        }

    /**
     * Always-on flow of all tiles with live [EditTileViewModel.isCurrent] state, intended
     * for the tile picker UI.
     */
    val tilesForPicker: Flow<List<EditTileViewModel>> =
        kotlinx.coroutines.flow.flow { emit(editTilesListInteractor.getTilesToEdit()) }
            .flatMapLatest { editTilesData ->
                val allTiles = editTilesData.stockTiles + editTilesData.customTiles
                val unavailable =
                    tilesAvailabilityInteractor.getUnavailableTiles(
                        editTilesData.stockTiles
                            .map { it.tileSpec }
                            .minus(currentTilesInteractor.currentTilesSpecs.toSet())
                    )

                combine(currentTilesInteractor.currentTiles, _explicitTiles) { currentTilesList, explicitSpecs ->
                        val currentSpecs = explicitSpecs ?: currentTilesList.map { it.spec }.toSet()
                        allTiles
                            .filterNot { it.tileSpec in unavailable }
                            .map {
                                val current = it.tileSpec in currentSpecs
                                UnloadedEditTileViewModel(
                                    it.tileSpec,
                                    it.icon,
                                    it.label,
                                    it.appName,
                                    it.appIcon,
                                    isCurrent = current,
                                    isDualTarget = false,
                                    availableEditActions = if (current) {
                                        setOf(AvailableEditActions.MOVE, AvailableEditActions.REMOVE)
                                    } else {
                                        setOf(AvailableEditActions.ADD)
                                    },
                                    it.category,
                                )
                            }
                    }
                    .combine(configurationInteractor.onAnyConfigurationChange.emitOnStart()) {
                        tiles, _ -> tiles.fastMap { it.load(context) }
                    }
            }

    /** @see isEditing */
    fun startEditing() {
        if (!isEditing.value) {
            uiEventLogger.log(QSEditEvent.QS_EDIT_OPEN)
        }
        _isEditing.value = true
    }

    /** @see isEditing */
    fun stopEditing() {
        if (isEditing.value) {
            uiEventLogger.log(QSEditEvent.QS_EDIT_CLOSED)
        }
        _isEditing.value = false
    }

    override suspend fun onActivated(): Nothing {
        awaitCancellation()
    }

    /**
     * Immediately adds [tileSpec] to the current tiles at [position].
     */
    fun addTile(tileSpec: TileSpec, position: Int = POSITION_AT_END) {
        val specs = (_explicitTiles.value?.toMutableList() ?: currentTilesInteractor.currentTilesSpecs.toMutableList())
        val currentPosition = specs.indexOf(tileSpec)
        val moved = currentPosition != -1

        if (currentPosition != -1) {
            if (currentPosition == position) {
                return
            }
            specs.removeAt(currentPosition)
        }

        if (position >= 0 && position < specs.size) {
            specs.add(position, tileSpec)
        } else {
            specs.add(tileSpec)
        }
        uiEventLogger.logWithPosition(
            if (moved) QSEditEvent.QS_EDIT_MOVE else QSEditEvent.QS_EDIT_ADD,
            /* uid= */ 0,
            /* packageName= */ tileSpec.metricSpec,
            if (moved && position == POSITION_AT_END) specs.size - 1 else position,
        )

        _explicitTiles.value = specs.toSet()
        currentTilesInteractor.setTiles(specs)
    }

    /** Immediately removes [tileSpec] from the current tiles. */
    fun removeTile(tileSpec: TileSpec) {
        uiEventLogger.log(
            QSEditEvent.QS_EDIT_REMOVE,
            /* uid= */ 0,
            /* packageName= */ tileSpec.metricSpec,
        )
        _explicitTiles.value = _explicitTiles.value?.minus(tileSpec)
        currentTilesInteractor.removeTiles(listOf(tileSpec))
    }

    fun setTiles(tileSpecs: List<TileSpec>) {
        _explicitTiles.value = tileSpecs.toSet()
        
        val currentTiles = currentTilesInteractor.currentTilesSpecs
        currentTilesInteractor.setTiles(tileSpecs)
        applicationScope.launch(bgDispatcher) {
            calculateDiffsAndEmitUiEvents(currentTiles, tileSpecs)
        }
    }

    private fun calculateDiffsAndEmitUiEvents(
        currentTiles: List<TileSpec>,
        newTiles: List<TileSpec>,
    ) {
        val listDiff = DiffUtil.calculateDiff(DiffCallback(currentTiles, newTiles))
        listDiff.dispatchUpdatesTo(
            object : ListUpdateCallback {
                override fun onInserted(position: Int, count: Int) {
                    newTiles.getOrNull(position)?.let {
                        uiEventLogger.logWithPosition(
                            QSEditEvent.QS_EDIT_ADD,
                            /* uid= */ 0,
                            /* packageName= */ it.metricSpec,
                            position,
                        )
                    }
                }

                override fun onRemoved(position: Int, count: Int) {
                    currentTiles.getOrNull(position)?.let {
                        uiEventLogger.log(QSEditEvent.QS_EDIT_REMOVE, 0, it.metricSpec)
                    }
                }

                override fun onMoved(fromPosition: Int, toPosition: Int) {
                    currentTiles.getOrNull(fromPosition)?.let {
                        uiEventLogger.logWithPosition(
                            QSEditEvent.QS_EDIT_MOVE,
                            /* uid= */ 0,
                            /* packageName= */ it.metricSpec,
                            toPosition,
                        )
                    }
                }

                override fun onChanged(position: Int, count: Int, payload: Any?) {}
            }
        )
    }
}

private class DiffCallback(
    private val currentList: List<TileSpec>,
    private val newList: List<TileSpec>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return currentList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return currentList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(oldItemPosition, newItemPosition)
    }
}
