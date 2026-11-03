/*
 * Copyright (C) 2026 RisingOS (revived) Android Project
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

package com.android.systemui.qs.panels.shared.model

import com.android.systemui.qs.pipeline.shared.TileSpec
import org.json.JSONObject

data class TileGridConfig(
    val spec: TileSpec,
    val spanCols: Int = 1,
    val spanRows: Int = 1,
    val position: Int = 0
) {
    init {
        require(spanCols in 1..4) { "spanCols must be between 1 and 4" }
        require(spanRows in 1..3) { "spanRows must be between 1 and 3" }
        require(position >= 0) { "position must be non-negative" }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("spec", spec.spec)
            put("spanCols", spanCols)
            put("spanRows", spanRows)
            put("position", position)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): TileGridConfig? {
            return try {
                TileGridConfig(
                    spec = TileSpec.create(json.getString("spec")),
                    spanCols = json.optInt("spanCols", 1),
                    spanRows = json.optInt("spanRows", 1),
                    position = json.getInt("position")
                )
            } catch (e: Exception) {
                null
            }
        }

        fun getDefaultSize(spec: TileSpec): Pair<Int, Int> {
            return when {
                spec.spec.contains("flashlight", ignoreCase = true) -> 1 to 1
                spec.spec.contains("wifi", ignoreCase = true) -> 2 to 1
                spec.spec.contains("bluetooth", ignoreCase = true) -> 2 to 1
                else -> 1 to 1
            }
        }
    }
}
