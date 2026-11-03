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

package com.android.systemui.qs.panels.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.qs.panels.shared.model.FloatingTile
import com.android.systemui.qs.panels.shared.model.QSLayoutItem
import com.android.systemui.qs.panels.shared.model.SectionConfig
import com.android.systemui.qs.panels.shared.model.SectionType
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.settings.UserFileManager
import com.android.systemui.user.data.repository.UserRepository
import com.android.systemui.util.kotlin.SharedPreferencesExt.observe
import com.android.systemui.util.kotlin.emitOnStart
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalCoroutinesApi::class)
@SysUISingleton
class QSSectionsRepository
@Inject
constructor(
    private val userFileManager: UserFileManager,
    private val userRepository: UserRepository,
    @Background private val backgroundDispatcher: CoroutineDispatcher,
    @Named("QSSectionsScope") private val scope: CoroutineScope,
    @Named("MainQSTilesKey") private val mainQSTilesKey: String,
    @Named("SectionTilesPrefix") private val sectionTilesPrefix: String,
) {

    companion object {
        private const val SECTION_CONFIGS_KEY = "section_configs"
        private const val PREF_KEY_FLAT_LAYOUT = "qs_flat_layout_v1"
        private const val FILE_NAME = "qs_sections_prefs"
    }

    val sectionConfigs: Flow<List<SectionConfig>> =
        userRepository.selectedUserInfo
            .flatMapLatest { userInfo ->
                val prefs = getSharedPrefs(userInfo.id)
                prefs.observe().emitOnStart().map { prefs.getSectionConfigs() }
            }
            .flowOn(backgroundDispatcher)

    private val _mainQSFloatingTiles = MutableStateFlow<List<FloatingTile>>(
        listOf(
            FloatingTile(TileSpec.create("wifi"), SectionType.TILES, 1, 1),
            FloatingTile(TileSpec.create("bt"), SectionType.TILES, 1, 1),
        )
    )
    val mainQSFloatingTiles: Flow<List<FloatingTile>> = _mainQSFloatingTiles

    // Hydrated from prefs on the background dispatcher in init; loading here
    // would do disk I/O and JSON parsing on the constructing (main) thread.
    private val _flatLayout = MutableStateFlow(QSLayoutItem.getDefault())

    val flatLayout: Flow<List<QSLayoutItem>> = _flatLayout

    fun setFlatLayout(items: List<QSLayoutItem>) {
        _flatLayout.value = items
        scope.launch(backgroundDispatcher) {
            val userId = userRepository.getSelectedUserInfo().id
            getSharedPrefs(userId).edit {
                putString(PREF_KEY_FLAT_LAYOUT, QSLayoutItem.listToJson(items))
            }
        }
    }

    init {
        scope.launch(backgroundDispatcher) {
            userRepository.selectedUserInfo.collect { userInfo ->
                val prefs = getSharedPrefs(userInfo.id)
                val tilesJson = prefs.getString(mainQSTilesKey, null)
                if (tilesJson != null) {
                    try {
                        val tiles = mutableListOf<FloatingTile>()
                        val array = JSONArray(tilesJson)
                        for (i in 0 until array.length()) {
                            FloatingTile.fromJson(array.getJSONObject(i))?.let { tiles.add(it) }
                        }
                        _mainQSFloatingTiles.value = tiles
                    } catch (_: Exception) { }
                }
                _flatLayout.value = loadFlatLayoutForUser(userInfo.id)
            }
        }
    }

    fun addMainQSTile(tile: FloatingTile) {
        val current = _mainQSFloatingTiles.value.toMutableList()
        current.removeAll { it.spec == tile.spec }
        current.add(tile)
        _mainQSFloatingTiles.value = current
        saveMainQSTiles(current)
    }

    fun removeMainQSTile(spec: TileSpec) {
        val current = _mainQSFloatingTiles.value.filter { it.spec != spec }
        _mainQSFloatingTiles.value = current
        saveMainQSTiles(current)
    }

    fun moveMainQSTile(fromIndex: Int, toIndex: Int) {
        val current = _mainQSFloatingTiles.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _mainQSFloatingTiles.value = current
            saveMainQSTiles(current)
        }
    }

    fun resizeMainQSTile(spec: TileSpec, spanCols: Int, spanRows: Int) {
        val current = _mainQSFloatingTiles.value.toMutableList()
        val index = current.indexOfFirst { it.spec == spec }
        if (index != -1) {
            current[index] = current[index].copy(spanCols = spanCols, spanRows = spanRows)
            _mainQSFloatingTiles.value = current
            saveMainQSTiles(current)
        }
    }

    fun moveMainQSTileToSection(spec: TileSpec, targetSection: SectionType) {
        val tile = _mainQSFloatingTiles.value.find { it.spec == spec } ?: return
        removeMainQSTile(spec)
        addFloatingTile(tile.copy(afterSection = targetSection), targetSection)
    }

    fun moveSectionTileToMain(spec: TileSpec) {
        val configs = getCurrentConfigs()
        val tile = configs.flatMap { it.floatingTiles }.find { it.spec == spec } ?: return
        removeFloatingTile(spec)
        addMainQSTile(tile.copy(afterSection = SectionType.TILES))
    }

    fun writeSectionConfigs(configs: List<SectionConfig>) {
        with(getSharedPrefs(userRepository.getSelectedUserInfo().id)) {
            val jsonArray = JSONArray()
            configs.forEachIndexed { index, config ->
                jsonArray.put(config.copy(position = index).toJson())
            }
            edit().putString(SECTION_CONFIGS_KEY, jsonArray.toString()).apply()
        }
    }

    fun toggleSectionVisibility(type: SectionType) {
        val configs = getCurrentConfigs().toMutableList()
        val index = configs.indexOfFirst { it.type == type }
        if (index != -1 && configs[index].type.canBeRemoved) {
            configs[index] = configs[index].copy(visible = !configs[index].visible)
            writeSectionConfigs(configs)
        }
    }

    fun addFloatingTile(tile: FloatingTile, afterSection: SectionType) {
        val configs = getCurrentConfigs().toMutableList()
        val sectionIndex = configs.indexOfFirst { it.type == afterSection }
        if (sectionIndex != -1) {
            val updatedTiles = configs[sectionIndex].floatingTiles.toMutableList()
            updatedTiles.removeAll { it.spec == tile.spec }
            updatedTiles.add(tile)
            configs[sectionIndex] = configs[sectionIndex].copy(floatingTiles = updatedTiles)
            writeSectionConfigs(configs)
        }
    }

    fun removeFloatingTile(spec: TileSpec) {
        val configs = getCurrentConfigs().toMutableList()
        configs.forEachIndexed { index, config ->
            val updated = config.floatingTiles.filter { it.spec != spec }
            if (updated.size != config.floatingTiles.size) {
                configs[index] = config.copy(floatingTiles = updated)
            }
        }
        writeSectionConfigs(configs)
    }

    fun moveFloatingTile(spec: TileSpec, newAfterSection: SectionType) {
        val configs = getCurrentConfigs().toMutableList()
        var tileToMove: FloatingTile? = null
        configs.forEachIndexed { index, config ->
            val tile = config.floatingTiles.find { it.spec == spec }
            if (tile != null) {
                tileToMove = tile
                configs[index] = config.copy(
                    floatingTiles = config.floatingTiles.filter { it.spec != spec }
                )
            }
        }
        tileToMove?.let { tile ->
            val sectionIndex = configs.indexOfFirst { it.type == newAfterSection }
            if (sectionIndex != -1) {
                val updated = configs[sectionIndex].floatingTiles.toMutableList()
                updated.add(tile.copy(afterSection = newAfterSection))
                configs[sectionIndex] = configs[sectionIndex].copy(floatingTiles = updated)
            }
        }
        writeSectionConfigs(configs)
    }

    fun resetToDefaults() {
        writeSectionConfigs(SectionConfig.getDefaultConfigs())
        _mainQSFloatingTiles.value = emptyList()
        saveMainQSTiles(emptyList())
        val flatDefault = QSLayoutItem.getDefault()
        _flatLayout.value = flatDefault
        scope.launch(backgroundDispatcher) {
            val userId = userRepository.getSelectedUserInfo().id
            getSharedPrefs(userId).edit {
                putString(PREF_KEY_FLAT_LAYOUT, QSLayoutItem.listToJson(flatDefault))
            }
        }
    }

    private fun getCurrentConfigs(): List<SectionConfig> =
        getSharedPrefs(userRepository.getSelectedUserInfo().id).getSectionConfigs()

    private fun SharedPreferences.getSectionConfigs(): List<SectionConfig> {
        val jsonString = getString(SECTION_CONFIGS_KEY, null)
            ?: return SectionConfig.getDefaultConfigs()
        return try {
            val jsonArray = JSONArray(jsonString)
            val configs = mutableListOf<SectionConfig>()
            for (i in 0 until jsonArray.length()) {
                SectionConfig.fromJson(jsonArray.getJSONObject(i))?.let { configs.add(it) }
            }
            if (configs.isEmpty()) SectionConfig.getDefaultConfigs()
            else configs.sortedBy { it.position }
        } catch (_: Exception) {
            SectionConfig.getDefaultConfigs()
        }
    }

    private fun loadFlatLayoutForUser(userId: Int): List<QSLayoutItem> {
        val prefs = getSharedPrefs(userId)
        val legacy = prefs.getSectionConfigs()
        val legacyTiles = legacy.flatMap { it.floatingTiles }

        val json = prefs.getString(PREF_KEY_FLAT_LAYOUT, null)
        if (json != null) {
            val loaded = QSLayoutItem.listFromJson(json)
            if (loaded.isNotEmpty()) {
                val hasTiles = loaded.any { it is QSLayoutItem.TileItem }
                if (hasTiles || legacyTiles.isEmpty()) return loaded
            }
        }
        return if (legacyTiles.isNotEmpty() || legacy != SectionConfig.getDefaultConfigs()) {
            QSLayoutItem.fromSectionConfigs(legacy)
        } else {
            QSLayoutItem.getDefault()
        }
    }

    private fun saveMainQSTiles(tiles: List<FloatingTile>) {
        scope.launch(backgroundDispatcher) {
            val prefs = getSharedPrefs(userRepository.getSelectedUserInfo().id)
            prefs.edit {
                val array = JSONArray()
                tiles.forEach { array.put(it.toJson()) }
                putString(mainQSTilesKey, array.toString())
            }
        }
    }

    private fun getSharedPrefs(userId: Int): SharedPreferences =
        userFileManager.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE, userId)
}