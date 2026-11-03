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

import androidx.annotation.StringRes
import com.android.systemui.res.R

enum class SectionType(
    @StringRes val labelRes: Int,
    val canBeRemoved: Boolean = true
) {
    BRIGHTNESS(R.string.qs_section_brightness, canBeRemoved = true),
    TILES(R.string.qs_section_tiles, canBeRemoved = false),
    MEDIA(R.string.qs_section_media, canBeRemoved = true);

    companion object {
        val DEFAULT_ORDER = listOf(BRIGHTNESS, TILES, MEDIA)
    }
}
