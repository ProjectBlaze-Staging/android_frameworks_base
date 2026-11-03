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

package com.android.systemui.qs.panels.domain.interactor

import com.android.internal.logging.UiEventLogger
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.log.LogBuffer
import com.android.systemui.log.core.LogLevel
import com.android.systemui.qs.QSEditEvent
import com.android.systemui.qs.panels.data.repository.DefaultLargeTilesRepository
import com.android.systemui.qs.panels.shared.model.PanelsLog
import com.android.systemui.qs.panels.shared.model.TileGridConfig
import com.android.systemui.qs.pipeline.domain.interactor.CurrentTilesInteractor
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.qs.pipeline.shared.metricSpec
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/** Interactor for retrieving the list of [TileSpec] to be displayed as icons and resizing icons. */
@SysUISingleton
class IconTilesInteractor
@Inject
constructor(
    private val repo: DefaultLargeTilesRepository,
    private val currentTilesInteractor: CurrentTilesInteractor,
    private val preferencesInteractor: QSPreferencesInteractor,
    private val uiEventLogger: UiEventLogger,
    @PanelsLog private val logBuffer: LogBuffer,
    @Background private val scope: CoroutineScope,
) {

    val largeTilesSpecs: StateFlow<Set<TileSpec>> =
        preferencesInteractor.largeTilesSpecs
            .onEach { logChange(it) }
            .stateIn(scope, SharingStarted.Eagerly, repo.defaultLargeTiles)

    val tileGridConfigs: StateFlow<List<TileGridConfig>> =
        preferencesInteractor.tileGridConfigs
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val tileGridConfigsLandscape: StateFlow<List<TileGridConfig>> =
        preferencesInteractor.tileGridConfigsLandscape
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun isIconTile(spec: TileSpec): Boolean = !largeTilesSpecs.value.contains(spec)

    /**
     * Get the grid size for a tile (columns x rows).
     * Returns (1, 1) if no custom size is set.
     */
    fun getTileSize(spec: TileSpec, landscape: Boolean = false): Pair<Int, Int> {
        val configs = if (landscape) tileGridConfigsLandscape.value else tileGridConfigs.value
        return configs.find { it.spec == spec }?.let { it.spanCols to it.spanRows }
            ?: TileGridConfig.getDefaultSize(spec)
    }

    /** Set the large tiles to be [specs] */
    fun addLargeTile(spec: TileSpec) {
        preferencesInteractor.setLargeTilesSpecs(largeTilesSpecs.value + spec)
    }

    /** Set the large tiles to be [specs] */
    fun setLargeTiles(specs: Set<TileSpec>) {
        preferencesInteractor.setLargeTilesSpecs(specs)
    }

    /** Remove [specs] from the current set of large tiles */
    fun removeLargeTiles(specs: Set<TileSpec>) {
        preferencesInteractor.removeLargeTilesSpecs(specs)
    }

    /** Set the size of a specific tile. */
    fun setTileSize(spec: TileSpec, spanCols: Int, spanRows: Int, landscape: Boolean = false) {
        if (!isCurrent(spec)) {
            return
        }
        
        preferencesInteractor.writeTileSize(spec, spanCols, spanRows, landscape)

        uiEventLogger.log(
            QSEditEvent.QS_EDIT_RESIZE_LARGE,
            0,
            "${spec.metricSpec}_${spanCols}x${spanRows}"
        )
    }

    fun getTileGridConfigs(landscape: Boolean = false): List<TileGridConfig> {
        return if (landscape) tileGridConfigsLandscape.value else tileGridConfigs.value
    }

    fun resetToDefault() {
        preferencesInteractor.setLargeTilesSpecs(repo.defaultLargeTiles)
    }

    fun resize(spec: TileSpec, toIcon: Boolean) {
        if (!isCurrent(spec)) {
            return
        }

        val isIcon = !largeTilesSpecs.value.contains(spec)
        if (toIcon && !isIcon) {
            preferencesInteractor.setLargeTilesSpecs(largeTilesSpecs.value - spec)
            uiEventLogger.log(
                /* event= */ QSEditEvent.QS_EDIT_RESIZE_SMALL,
                /* uid= */ 0,
                /* packageName= */ spec.metricSpec,
            )
        } else if (!toIcon && isIcon) {
            preferencesInteractor.setLargeTilesSpecs(largeTilesSpecs.value + spec)
            uiEventLogger.log(
                /* event= */ QSEditEvent.QS_EDIT_RESIZE_LARGE,
                /* uid= */ 0,
                /* packageName= */ spec.metricSpec,
            )
        }
    }

    private fun isCurrent(spec: TileSpec): Boolean {
        return currentTilesInteractor.currentTilesSpecs.contains(spec)
    }

    private fun logChange(specs: Set<TileSpec>) {
        logBuffer.log(
            LOG_BUFFER_LARGE_TILES_SPECS_CHANGE_TAG,
            LogLevel.DEBUG,
            { str1 = specs.toString() },
            { "Large tiles change: $str1" },
        )
    }

    private companion object {
        const val LOG_BUFFER_LARGE_TILES_SPECS_CHANGE_TAG = "LargeTilesSpecsChange"
    }
}
