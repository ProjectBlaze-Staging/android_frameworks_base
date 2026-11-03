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

package com.android.systemui.qs.panels.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.systemui.plugins.qs.QSTile

@Composable
fun QSTileIconRenderer(
    icon: QSTile.Icon?,
    contentColor: Color,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    if (icon == null) {
        // Fallback placeholder
        Box(
            modifier = modifier
                .size(size)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Empty placeholder
        }
        return
    }

    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    // Drawable inflation and bitmap conversion are expensive; only redo them
    // when the icon, tint or size actually changes, not on every recomposition.
    val bitmap = remember(icon, contentColor, sizePx) {
        try {
            icon.getDrawable(context)
                ?.mutate()
                ?.apply { setTint(contentColor.toArgb()) }
                ?.toBitmap(width = sizePx, height = sizePx)
                ?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier.size(size)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Empty placeholder
        }
    }
}
