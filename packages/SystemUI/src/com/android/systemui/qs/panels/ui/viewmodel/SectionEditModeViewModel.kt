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

package com.android.systemui.qs.panels.ui.viewmodel

import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.lifecycle.ExclusiveActivatable
import com.android.systemui.qs.panels.domain.interactor.QSSectionsInteractor
import com.android.systemui.qs.panels.shared.model.FloatingTile
import com.android.systemui.qs.panels.shared.model.QSLayoutItem
import com.android.systemui.qs.panels.shared.model.SectionConfig
import com.android.systemui.qs.panels.shared.model.SectionType
import com.android.systemui.qs.pipeline.shared.TileSpec
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SysUISingleton
class SectionEditModeViewModel
@Inject
constructor(
    @com.android.systemui.dagger.qualifiers.Application private val applicationScope: CoroutineScope,
    private val sectionsInteractor: QSSectionsInteractor,
) : ExclusiveActivatable() {

    private val _isEditingSections = MutableStateFlow(false)
    val isEditingSections = _isEditingSections.asStateFlow()

    private val _showTilePicker = MutableStateFlow(false)
    val showTilePicker = _showTilePicker.asStateFlow()

    private val _isDraggingTile = MutableStateFlow(false)
    val isDraggingTile = _isDraggingTile.asStateFlow()

    private val _draggedTileSpec = MutableStateFlow<TileSpec?>(null)
    val draggedTileSpec = _draggedTileSpec.asStateFlow()

    val sectionConfigs: Flow<List<SectionConfig>> = sectionsInteractor.sectionConfigs

    val mainQSFloatingTiles: Flow<List<FloatingTile>> = sectionsInteractor.mainQSFloatingTiles

    val flatLayout: Flow<List<QSLayoutItem>> = sectionsInteractor.flatLayout

    fun toggleEditMode() {
        _isEditingSections.value = !_isEditingSections.value
    }

    fun startEditingSections() {
        _isEditingSections.value = true
    }

    fun stopEditingSections() {
        _isEditingSections.value = false
        _showTilePicker.value = false
        _isDraggingTile.value = false
        _draggedTileSpec.value = null
        com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.clearFallbackTiles()
    }

    fun setFlatLayout(items: List<QSLayoutItem>) {
        sectionsInteractor.setFlatLayout(items)
    }

    fun addTileToFlatLayout(spec: TileSpec, spanCols: Int = 1, spanRows: Int = 1, insertIndex: Int = -1) {
        applicationScope.launch {
            val current = sectionsInteractor.flatLayout.first().toMutableList()
            val newItem = QSLayoutItem.TileItem(spec, spanCols, spanRows)
            if (current.any { it is QSLayoutItem.TileItem && it.spec == spec }) return@launch
            if (insertIndex < 0 || insertIndex >= current.size) {
                current.add(newItem)
            } else {
                current.add(insertIndex, newItem)
            }
            sectionsInteractor.setFlatLayout(current)
        }
    }

    fun removeTileFromFlatLayout(spec: TileSpec) {
        applicationScope.launch {
            val current = sectionsInteractor.flatLayout.first()
            val updated = current.filter { !(it is QSLayoutItem.TileItem && it.spec == spec) }
            sectionsInteractor.setFlatLayout(updated)
        }
    }

    fun toggleFlatSectionVisibility(type: SectionType) {
        applicationScope.launch {
            val current = sectionsInteractor.flatLayout.first()
            val updated = current.map { item ->
                if (item is QSLayoutItem.SectionHeader && item.type == type)
                    item.copy(visible = !item.visible)
                else item
            }
            sectionsInteractor.setFlatLayout(updated)
        }
    }

    fun resizeTileInFlatLayout(spec: TileSpec, spanCols: Int, spanRows: Int) {
        applicationScope.launch {
            val current = sectionsInteractor.flatLayout.first()
            val updated = current.map { item ->
                if (item is QSLayoutItem.TileItem && item.spec == spec)
                    item.copy(spanCols = spanCols, spanRows = spanRows)
                else item
            }
            sectionsInteractor.setFlatLayout(updated)
        }
    }

    fun setFlatSectionHeightScale(type: SectionType, scale: Float) {
        applicationScope.launch {
            val current = sectionsInteractor.flatLayout.first()
            val updated = current.map { item ->
                if (item is QSLayoutItem.SectionHeader && item.type == type)
                    item.copy(heightScale = scale)
                else item
            }
            sectionsInteractor.setFlatLayout(updated)
        }
    }

    fun setSectionOrder(configs: List<SectionConfig>) {
        sectionsInteractor.setSectionOrder(configs)
    }

    fun toggleSectionVisibility(type: SectionType) {
        sectionsInteractor.toggleSectionVisibility(type)
    }

    fun resetToDefaults() {
        sectionsInteractor.resetToDefaults()
    }

    fun showTilePicker() { _showTilePicker.value = true }
    fun hideTilePicker() { _showTilePicker.value = false }

    fun addFloatingTile(tile: FloatingTile, afterSection: SectionType) {
        stopDraggingTile()
        sectionsInteractor.addFloatingTile(tile, afterSection)
        applicationScope.launch {
            val current = sectionsInteractor.flatLayout.first().toMutableList()
            if (current.any { it is QSLayoutItem.TileItem && it.spec == tile.spec }) return@launch
            val insertBefore = current.indexOfFirst {
                it is QSLayoutItem.SectionHeader && it.type == afterSection
            }
            val newItem = QSLayoutItem.TileItem(tile.spec, tile.spanCols, tile.spanRows)
            if (insertBefore >= 0) current.add(insertBefore, newItem) else current.add(newItem)
            sectionsInteractor.setFlatLayout(current)
        }
    }

    fun removeFloatingTile(spec: TileSpec) {
        sectionsInteractor.removeFloatingTile(spec)
        removeTileFromFlatLayout(spec)
    }

    fun addMainQSTile(tile: FloatingTile) {
        stopDraggingTile()
        sectionsInteractor.addMainQSTile(tile)
    }

    fun removeMainQSTile(spec: TileSpec) {
        sectionsInteractor.removeMainQSTile(spec)
    }

    fun moveMainQSTile(fromIndex: Int, toIndex: Int) {
        sectionsInteractor.moveMainQSTile(fromIndex, toIndex)
    }

    fun resizeMainQSTile(spec: TileSpec, spanCols: Int, spanRows: Int) {
        sectionsInteractor.resizeMainQSTile(spec, spanCols, spanRows)
    }

    fun moveMainQSTileToSection(spec: TileSpec, targetSection: SectionType) {
        stopDraggingTile()
        sectionsInteractor.moveMainQSTileToSection(spec, targetSection)
    }

    fun moveSectionTileToMain(spec: TileSpec) {
        stopDraggingTile()
        sectionsInteractor.moveSectionTileToMain(spec)
    }

    fun moveFloatingTileInSection(
        spec: TileSpec,
        fromIndex: Int,
        toIndex: Int,
        sectionType: SectionType,
    ) {
        applicationScope.launch {
            val currentConfigs = sectionsInteractor.sectionConfigs.first()
            val sectionConfig = currentConfigs.find { it.type == sectionType } ?: return@launch
            val updatedTiles = sectionConfig.floatingTiles.toMutableList()
            if (fromIndex in updatedTiles.indices && toIndex in updatedTiles.indices) {
                val movedTile = updatedTiles.removeAt(fromIndex)
                updatedTiles.add(toIndex, movedTile)
                val updatedConfig = sectionConfig.copy(floatingTiles = updatedTiles)
                sectionsInteractor.setSectionOrder(
                    currentConfigs.map { cfg ->
                        if (cfg.type == sectionType) updatedConfig else cfg
                    }
                )
            }
        }
    }

    fun startDraggingTile(spec: TileSpec) {
        _isDraggingTile.value = true
        _draggedTileSpec.value = spec
        _showTilePicker.value = false
    }

    fun stopDraggingTile() {
        _isDraggingTile.value = false
        _draggedTileSpec.value = null
    }

    override suspend fun onActivated(): Nothing = awaitCancellation()
}