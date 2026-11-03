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

package com.android.systemui.qs.panels.shared.model

import com.android.systemui.qs.pipeline.shared.TileSpec
import org.json.JSONArray
import org.json.JSONObject

sealed class QSLayoutItem {
    abstract val id: String

    data class SectionHeader(
        val type: SectionType,
        val visible: Boolean = true,
        val heightScale: Float = 1.0f,
    ) : QSLayoutItem() {
        override val id: String get() = "section_${type.name}"
    }

    data class TileItem(
        val spec: TileSpec,
        val spanCols: Int = 1,
        val spanRows: Int = 1,
    ) : QSLayoutItem() {
        override val id: String get() = "tile_${spec.spec}"
    }

    fun toJson(): JSONObject = when (this) {
        is SectionHeader -> JSONObject().apply {
            put("kind", "section")
            put("type", type.name)
            put("visible", visible)
            put("heightScale", heightScale.toDouble())
        }
        is TileItem -> JSONObject().apply {
            put("kind", "tile")
            put("spec", spec.spec)
            put("spanCols", spanCols)
            put("spanRows", spanRows)
        }
    }

    companion object {

        fun fromJson(json: JSONObject): QSLayoutItem? = try {
            when (json.getString("kind")) {
                "section" -> SectionHeader(
                    type = SectionType.valueOf(json.getString("type")),
                    visible = json.optBoolean("visible", true),
                    heightScale = json.optDouble("heightScale", 1.0).toFloat(),
                )
                "tile" -> TileItem(
                    spec = TileSpec.create(json.getString("spec")),
                    spanCols = json.optInt("spanCols", 1),
                    spanRows = json.optInt("spanRows", 1),
                )
                else -> null
            }
        } catch (e: Exception) { null }

        fun listToJson(items: List<QSLayoutItem>): String {
            val arr = JSONArray()
            items.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun listFromJson(json: String): List<QSLayoutItem> = try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { fromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) { emptyList() }

        fun getDefault(): List<QSLayoutItem> =
            listOf(
                TileItem(TileSpec.create("internet")),
                TileItem(TileSpec.create("bt")),
                TileItem(TileSpec.create("dnd")),
                TileItem(TileSpec.create("flashlight")),
                TileItem(TileSpec.create("rotation")),
                TileItem(TileSpec.create("battery")),
                TileItem(TileSpec.create("cast")),
                TileItem(TileSpec.create("screenrecord")),
            ) + SectionType.DEFAULT_ORDER.map { SectionHeader(it) }

        fun fromSectionConfigs(configs: List<SectionConfig>): List<QSLayoutItem> {
            val result = mutableListOf<QSLayoutItem>()
            configs.sortedBy { it.position }.forEach { config ->
                config.floatingTiles.forEach { tile ->
                    result.add(TileItem(tile.spec, tile.spanCols, tile.spanRows))
                }
                result.add(SectionHeader(config.type, config.visible, config.heightScale))
            }
            return result
        }
    }
}