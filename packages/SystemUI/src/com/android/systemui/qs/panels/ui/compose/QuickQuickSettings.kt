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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.compose.animation.scene.ContentScope
import com.android.systemui.compose.modifiers.sysuiResTag
import com.android.systemui.grid.ui.compose.CustomVerticalSpannedGrid
import com.android.systemui.qs.composefragment.ui.GridAnchor
import com.android.systemui.qs.panels.ui.compose.infinitegrid.QQS_EDIT_GRID_COLUMNS
import com.android.systemui.qs.panels.ui.compose.infinitegrid.QQS_EDIT_GRID_MAX_TILES
import com.android.systemui.qs.panels.ui.compose.infinitegrid.Tile
import com.android.systemui.qs.panels.ui.viewmodel.BounceableTileViewModel
import com.android.systemui.qs.panels.ui.viewmodel.QuickQuickSettingsViewModel
import com.android.systemui.res.R

@Composable
fun ContentScope.QuickQuickSettings(
    viewModel: QuickQuickSettingsViewModel,
    modifier: Modifier = Modifier.fillMaxWidth(),
    listening: () -> Boolean,
) {
    val sizedTiles = viewModel.tileViewModels
    val tiles = sizedTiles.fastMap { it.tile }
    val squishiness by viewModel.squishinessViewModel.squishiness.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val maxTiles = if (isLandscape) QQS_EDIT_GRID_COLUMNS else QQS_EDIT_GRID_MAX_TILES

    val qqsTiles = remember(sizedTiles, maxTiles) { sizedTiles.take(maxTiles) }
    val spans by remember(qqsTiles) { derivedStateOf { List(qqsTiles.size) { 1 } } }

    val qqsColumns = QQS_EDIT_GRID_COLUMNS

    Box(modifier = modifier) {
        GridAnchor()
        CustomVerticalSpannedGrid(
            columns = qqsColumns,
            rowSpacing = 15.dp,
            spans = spans,
            modifier = Modifier.sysuiResTag("qqs_tile_layout"),
            keys = { qqsTiles[it].tile.spec },
        ) { spanIndex, column, isFirstInColumn, isLastInColumn ->
            val it = qqsTiles[spanIndex]
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Tile(
                    tile = it.tile,
                    iconOnly = true,
                    squishiness = { squishiness },
                    coroutineScope = scope,
                    tileHapticsViewModelFactory = viewModel.tileHapticsViewModelFactory,
                    // There should be no QuickQuickSettings when the details view is enabled.
                    detailsViewModel = null,
                    isVisible = listening,
                    bounceableInfo = null,
                    interactionSource = null,
                )
            }
        }
    }

    TileListener(tiles, listening)
}
