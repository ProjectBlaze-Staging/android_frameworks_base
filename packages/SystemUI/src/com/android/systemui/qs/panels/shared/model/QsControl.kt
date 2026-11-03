/*
 * Copyright (C) 2026 RisingOS (revived) Android Project
 * Copyright (C) 2025-2026 AxionOS
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

data class QSControlSpan(val columns: Int, val rows: Int) {
    init {
        require(columns in MIN_COLUMNS..MAX_COLUMNS)
        require(rows in MIN_ROWS..MAX_ROWS)
    }

    fun coerceIn(min: QSControlSpan, max: QSControlSpan): QSControlSpan =
        QSControlSpan(
            columns = columns.coerceIn(min.columns, max.columns),
            rows = rows.coerceIn(min.rows, max.rows),
        )

    override fun toString(): String = "${columns}x$rows"

    companion object {
        const val MIN_COLUMNS = 1
        const val MAX_COLUMNS = 4
        const val MIN_ROWS = 1
        const val MAX_ROWS = 4
    }
}

data class QSControlSpans(
    val default: QSControlSpan,
    val min: QSControlSpan,
    val max: QSControlSpan,
)

enum class QSControl(
    val id: String,
    val label: String,
    val defaultSpan: QSControlSpan,
    val minSpan: QSControlSpan,
    val maxSpan: QSControlSpan,
) {
    BRIGHTNESS(
        id = "control:brightness",
        label = "Brightness",
        defaultSpan = QSControlSpan(1, 3),
        minSpan = QSControlSpan(1, 1),
        maxSpan = QSControlSpan(QSControlSpan.MAX_COLUMNS, QSControlSpan.MAX_ROWS),
    ),
    VOLUME(
        id = "control:volume",
        label = "Volume",
        defaultSpan = QSControlSpan(1, 3),
        minSpan = QSControlSpan(1, 1),
        maxSpan = QSControlSpan(QSControlSpan.MAX_COLUMNS, QSControlSpan.MAX_ROWS),
    ),
    MEDIA(
        id = "control:media",
        label = "Media",
        defaultSpan = QSControlSpan(2, 2),
        minSpan = QSControlSpan(2, 1),
        maxSpan = QSControlSpan(4, 2),
    ),
    RINGER(
        id = "control:ringer",
        label = "Ringer",
        defaultSpan = QSControlSpan(2, 1),
        minSpan = QSControlSpan(2, 1),
        maxSpan = QSControlSpan(3, 1),
    );

    val isSlider: Boolean
        get() = this == BRIGHTNESS || this == VOLUME

    fun spans(columns: Int): QSControlSpans {
        val resolvedMax =
            maxSpan.copy(
                columns = maxSpan.columns.coerceAtMost(columns).coerceAtLeast(minSpan.columns)
            )
        return QSControlSpans(defaultSpan.coerceIn(minSpan, resolvedMax), minSpan, resolvedMax)
    }

    fun coerceSpan(span: QSControlSpan, columns: Int): QSControlSpan {
        if (isSlider) {
            return if (span.columns > span.rows) {
                QSControlSpan(span.columns.coerceIn(SLIDER_MIN_LENGTH, SLIDER_MAX_LENGTH), 1)
            } else {
                QSControlSpan(1, span.rows.coerceIn(SLIDER_MIN_LENGTH, SLIDER_MAX_LENGTH))
            }
        }
        val spans = spans(columns)
        return span.coerceIn(spans.min, spans.max)
    }

    companion object {
        const val SLIDER_MIN_LENGTH = 3
        const val SLIDER_MAX_LENGTH = 4

        fun fromId(id: String): QSControl? = entries.firstOrNull { it.id == id }
    }
}
