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

package com.android.systemui.qs.panels.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GridBackground(
    rows: Int,
    columns: Int,
    cellSize: Dp,
    spacing: Dp,
    modifier: Modifier = Modifier,
    dotColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
    dotRadius: Dp = 2.dp
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val radiusPx = dotRadius.toPx()
        val cellSizePx = cellSize.toPx()
        val spacingPx = spacing.toPx()
        
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val x = (cellSizePx + spacingPx) * col + (cellSizePx / 2)
                val y = (cellSizePx + spacingPx) * row + (cellSizePx / 2)
                drawCircle(
                    color = dotColor,
                    radius = radiusPx,
                    center = Offset(x, y)
                )
            }
        }
    }
}
