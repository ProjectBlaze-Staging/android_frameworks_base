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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun EditPanelModeTabRow(
    editingQqs: Boolean,
    onEditQqs: () -> Unit,
    onEditQs: () -> Unit,
    qqsLabel: String,
    qsLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        EditPanelModeSegment(title = qqsLabel, selected = editingQqs, onClick = onEditQqs)
        EditPanelModeSegment(title = qsLabel, selected = !editingQqs, onClick = onEditQs)
    }
}

@Composable
private fun EditPanelModeSegment(title: String, selected: Boolean, onClick: () -> Unit) {
    val containerColor by animateColorAsState(
        targetValue =
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "EditPanelModeSegmentContainer",
    )
    val contentColor =
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier =
            Modifier.clip(RoundedCornerShape(50))
                .background(containerColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = contentColor)
    }
}
