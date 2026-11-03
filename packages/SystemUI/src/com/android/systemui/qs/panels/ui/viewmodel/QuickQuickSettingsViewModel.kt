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

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.android.systemui.haptics.msdl.qs.TileHapticsViewModel
import com.android.systemui.lifecycle.HydratedActivatable
import com.android.systemui.media.controls.ui.controller.MediaHierarchyManager.Companion.LOCATION_QQS
import com.android.systemui.media.remedia.ui.compose.MediaUiBehavior
import com.android.systemui.media.remedia.ui.viewmodel.MediaCarouselVisibility
import com.android.systemui.qs.panels.domain.interactor.QqsTilesInteractor
import com.android.systemui.qs.panels.shared.model.SizedTileImpl
import com.android.systemui.qs.panels.shared.model.splitInRowsSequence
import com.android.systemui.qs.panels.ui.compose.infinitegrid.QQS_EDIT_GRID_COLUMNS
import com.android.systemui.qs.panels.ui.compose.infinitegrid.QQS_EDIT_GRID_MAX_ROWS
import com.android.systemui.qs.pipeline.domain.interactor.CurrentTilesInteractor
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class QuickQuickSettingsViewModel
@AssistedInject
constructor(
    tilesInteractor: CurrentTilesInteractor,
    qsColumnsViewModelFactory: QSColumnsViewModel.Factory,
    mediaInRowInLandscapeViewModelFactory: MediaInRowInLandscapeViewModel.Factory,
    val squishinessViewModel: TileSquishinessViewModel,
    val tileHapticsViewModelFactory: TileHapticsViewModel.Factory,
    private val qqsTilesInteractor: QqsTilesInteractor,
) : HydratedActivatable() {

    private val qsColumnsViewModel = qsColumnsViewModelFactory.create(LOCATION_QQS, mediaUiBehavior)
    private val mediaInRowViewModel =
        mediaInRowInLandscapeViewModelFactory.create(LOCATION_QQS, mediaUiBehavior)

    val columns: Int
        get() = QQS_EDIT_GRID_COLUMNS

    private val currentTiles by tilesInteractor.currentTiles.hydratedStateOf()

    private val qqsSpecs by qqsTilesInteractor.qqsTiles.hydratedStateOf(initialValue = emptyList())

    val tileViewModels by derivedStateOf {
        val order = qqsSpecs
        currentTiles
            .filter { order.contains(it.spec) }
            .sortedBy { order.indexOf(it.spec) }
            .map { SizedTileImpl(TileViewModel(it.tile, it.spec, it.expandable), 1) }
            .let {
                splitInRowsSequence(it, QQS_EDIT_GRID_COLUMNS)
                    .take(QQS_EDIT_GRID_MAX_ROWS)
                    .toList()
                    .flatten()
            }
    }

    override suspend fun onActivated() {
        coroutineScope {
            launch { qsColumnsViewModel.activate() }
            launch { mediaInRowViewModel.activate() }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(): QuickQuickSettingsViewModel
    }

    companion object {
        /** Behavior of the media carousel in quick quick settings */
        @VisibleForTesting
        val mediaUiBehavior: MediaUiBehavior
            get() =
                MediaUiBehavior(
                    isCarouselDismissible = true,
                    carouselVisibility = MediaCarouselVisibility.WhenAnyCardIsActive,
                )
    }
}
