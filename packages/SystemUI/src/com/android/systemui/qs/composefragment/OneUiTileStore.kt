/*
 * Copyright (C) 2026 RisingOS (revived) Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.android.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.composefragment

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.android.systemui.qs.pipeline.shared.TileSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class OneUiTileStore(private val prefs: SharedPreferences) {

    private val _insideTiles = MutableStateFlow<List<TileSpec>>(emptyList())
    val insideTiles: StateFlow<List<TileSpec>> = _insideTiles.asStateFlow()

    private var mutated = false

    // Seeds the flow from prefs off the main thread; skipped if the user
    // already changed the tiles before the read finished.
    suspend fun load() {
        val loaded = withContext(Dispatchers.IO) { loadFromPrefs() }
        if (!mutated) {
            _insideTiles.value = loaded
        }
    }

    private fun loadFromPrefs(): List<TileSpec> {
        val saved = prefs.getString(KEY_INSIDE_TILES, null) ?: return DEFAULT_TILES
        return saved.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { TileSpec.create(it) }
    }

    private fun persist(specs: List<TileSpec>) {
        prefs.edit()
            .putString(KEY_INSIDE_TILES, specs.joinToString(",") { it.spec })
            .apply()
    }

    fun addTile(spec: TileSpec) {
        mutated = true
        val current = _insideTiles.value
        if (current.any { it == spec }) return
        val updated = current + spec
        _insideTiles.value = updated
        persist(updated)
    }

    fun removeTile(spec: TileSpec) {
        mutated = true
        val current = _insideTiles.value
        if (current.none { it == spec }) return
        val updated = current.filter { it != spec }
        _insideTiles.value = updated
        persist(updated)
    }

    fun setTiles(specs: List<TileSpec>) {
        mutated = true
        _insideTiles.value = specs
        persist(specs)
    }

    fun contains(spec: TileSpec): Boolean = _insideTiles.value.any { it == spec }

    private companion object {
        const val PREFS_NAME = "qs_oneui_layout"
        const val KEY_INSIDE_TILES = "inside_tiles"
        val DEFAULT_TILES = listOf(
            TileSpec.create("airplane"),
            TileSpec.create("nfc"),
            TileSpec.create("hotspot"),
            TileSpec.create("location"),
            TileSpec.create("saver"),
            TileSpec.create("dark"),
        )
    }
}

@Composable
fun rememberOneUiTileStore(): OneUiTileStore {
    val context = LocalContext.current
    val store = remember {
        val prefs = context.getSharedPreferences("qs_oneui_layout", Context.MODE_PRIVATE)
        OneUiTileStore(prefs)
    }
    LaunchedEffect(store) { store.load() }
    return store
}