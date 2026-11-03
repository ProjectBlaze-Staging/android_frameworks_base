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

package com.android.systemui.qs.panels.domain.interactor

import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.qs.panels.data.repository.QSSectionsRepository
import com.android.systemui.qs.panels.shared.model.FloatingTile
import com.android.systemui.qs.panels.shared.model.QSLayoutItem
import com.android.systemui.qs.panels.shared.model.SectionConfig
import com.android.systemui.qs.panels.shared.model.SectionType
import com.android.systemui.qs.pipeline.shared.TileSpec
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@SysUISingleton
class QSSectionsInteractor
@Inject
constructor(private val repository: QSSectionsRepository) {

    val sectionConfigs: Flow<List<SectionConfig>> = repository.sectionConfigs

    val mainQSFloatingTiles: Flow<List<FloatingTile>> = repository.mainQSFloatingTiles

    fun setSectionOrder(configs: List<SectionConfig>) {
        repository.writeSectionConfigs(configs)
    }

    fun toggleSectionVisibility(type: SectionType) {
        repository.toggleSectionVisibility(type)
    }

    fun resetToDefaults() {
        repository.resetToDefaults()
        repository.setFlatLayout(QSLayoutItem.getDefault())
    }

    fun addFloatingTile(tile: FloatingTile, afterSection: SectionType) {
        repository.addFloatingTile(tile, afterSection)
    }

    fun removeFloatingTile(spec: TileSpec) {
        repository.removeFloatingTile(spec)
    }

    fun moveFloatingTile(spec: TileSpec, newAfterSection: SectionType) {
        repository.moveFloatingTile(spec, newAfterSection)
    }

    fun addMainQSTile(tile: FloatingTile) {
        repository.addMainQSTile(tile)
    }

    fun removeMainQSTile(spec: TileSpec) {
        repository.removeMainQSTile(spec)
    }

    fun moveMainQSTile(fromIndex: Int, toIndex: Int) {
        repository.moveMainQSTile(fromIndex, toIndex)
    }

    fun resizeMainQSTile(spec: TileSpec, spanCols: Int, spanRows: Int) {
        repository.resizeMainQSTile(spec, spanCols, spanRows)
    }

    fun moveMainQSTileToSection(spec: TileSpec, targetSection: SectionType) {
        repository.moveMainQSTileToSection(spec, targetSection)
    }

    fun moveSectionTileToMain(spec: TileSpec) {
        repository.moveSectionTileToMain(spec)
    }

    val flatLayout: Flow<List<QSLayoutItem>> = repository.flatLayout

    fun setFlatLayout(items: List<QSLayoutItem>) {
        repository.setFlatLayout(items)
    }

    fun addTileToFlatLayout(spec: TileSpec, spanCols: Int = 1, spanRows: Int = 1, insertIndex: Int = -1) {
        repository.flatLayout.let { }
    }

    fun removeTileFromFlatLayout(spec: TileSpec) {
    }

    fun toggleFlatSectionVisibility(type: SectionType) {
    }
}