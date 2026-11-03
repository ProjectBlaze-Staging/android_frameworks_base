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

import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.qs.panels.domain.interactor.IconTilesInteractor
import com.android.systemui.qs.panels.shared.model.TileGridConfig
import com.android.systemui.qs.pipeline.shared.TileSpec
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

interface IconTilesViewModel {
    val largeTiles: StateFlow<Set<TileSpec>>
    val tileGridConfigs: StateFlow<List<TileGridConfig>>
    val tileGridConfigsLandscape: StateFlow<List<TileGridConfig>>

    fun isIconTile(spec: TileSpec): Boolean

    fun resize(spec: TileSpec, toIcon: Boolean)
    
    fun setTileSize(spec: TileSpec, spanCols: Int, spanRows: Int, landscape: Boolean = false)
    
    fun getTileSize(spec: TileSpec, landscape: Boolean = false): Pair<Int, Int>
}

@SysUISingleton
class IconTilesViewModelImpl @Inject constructor(private val interactor: IconTilesInteractor) :
    IconTilesViewModel {
    override val largeTiles = interactor.largeTilesSpecs
    override val tileGridConfigs = interactor.tileGridConfigs
    override val tileGridConfigsLandscape = interactor.tileGridConfigsLandscape

    override fun isIconTile(spec: TileSpec): Boolean = interactor.isIconTile(spec)

    override fun resize(spec: TileSpec, toIcon: Boolean) = interactor.resize(spec, toIcon)
    
    override fun setTileSize(spec: TileSpec, spanCols: Int, spanRows: Int, landscape: Boolean) {
        interactor.setTileSize(spec, spanCols, spanRows, landscape)
    }
    
    override fun getTileSize(spec: TileSpec, landscape: Boolean): Pair<Int, Int> {
        return interactor.getTileSize(spec, landscape)
    }
}
