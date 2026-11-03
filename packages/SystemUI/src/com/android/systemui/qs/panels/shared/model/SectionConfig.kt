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

import org.json.JSONArray
import org.json.JSONObject

data class SectionConfig(
    val type: SectionType,
    val position: Int,
    val visible: Boolean = true,
    val heightScale: Float = 1.0f,
    val floatingTiles: List<FloatingTile> = emptyList()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("type", type.name)
            put("position", position)
            put("visible", visible)
            put("heightScale", heightScale.toDouble())
            
            val tilesArray = JSONArray()
            floatingTiles.forEach { tile ->
                tilesArray.put(tile.toJson())
            }
            put("floatingTiles", tilesArray)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): SectionConfig? {
            return try {
                val typeName = json.getString("type")
                val type = SectionType.valueOf(typeName)
                
                val floatingTiles = mutableListOf<FloatingTile>()
                if (json.has("floatingTiles")) {
                    val tilesArray = json.getJSONArray("floatingTiles")
                    for (i in 0 until tilesArray.length()) {
                        FloatingTile.fromJson(tilesArray.getJSONObject(i))?.let {
                            floatingTiles.add(it)
                        }
                    }
                }
                
                SectionConfig(
                    type = type,
                    position = json.getInt("position"),
                    visible = json.optBoolean("visible", true),
                    heightScale = json.optDouble("heightScale", 1.0).toFloat(),
                    floatingTiles = floatingTiles
                )
            } catch (e: Exception) {
                null
            }
        }

        fun getDefaultConfigs(): List<SectionConfig> {
            return SectionType.DEFAULT_ORDER.mapIndexed { index, type ->
                SectionConfig(type, index, visible = true, heightScale = 1.0f)
            }
        }
    }
}
