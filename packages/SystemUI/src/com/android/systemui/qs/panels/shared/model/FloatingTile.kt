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
import org.json.JSONObject

data class FloatingTile(
    val spec: TileSpec,
    val afterSection: SectionType,
    val spanCols: Int = 1,
    val spanRows: Int = 1
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("spec", spec.spec)
            put("afterSection", afterSection.name)
            put("spanCols", spanCols)
            put("spanRows", spanRows)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): FloatingTile? {
            return try {
                FloatingTile(
                    spec = TileSpec.create(json.getString("spec")),
                    afterSection = SectionType.valueOf(json.getString("afterSection")),
                    spanCols = json.optInt("spanCols", 1),
                    spanRows = json.optInt("spanRows", 1)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
