/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.systemui.qs.composefragment.ui

import androidx.compose.animation.core.tween
import com.android.compose.animation.scene.ContentKey
import com.android.compose.animation.scene.ElementKey
import com.android.compose.animation.scene.ElementMatcher
import com.android.compose.animation.scene.TransitionBuilder
import com.android.systemui.qs.composefragment.MiniPlayerElementKey
import com.android.systemui.qs.composefragment.SceneKeys
import com.android.systemui.qs.shared.ui.QuickSettings.Elements

private val QqsMiniPlayerMatcher = object : ElementMatcher {
    override fun matches(key: ElementKey, content: ContentKey): Boolean {
        return key == MiniPlayerElementKey.MiniPlayer && content == SceneKeys.QuickQuickSettings
    }
}

private val QsMiniPlayerMatcher = object : ElementMatcher {
    override fun matches(key: ElementKey, content: ContentKey): Boolean {
        return key == MiniPlayerElementKey.MiniPlayer && content == SceneKeys.QuickSettings
    }
}

fun TransitionBuilder.quickQuickSettingsToQuickSettings(
    animateTilesExpansion: () -> Boolean = { true }
) {

    fractionRange(start = 0.43f) { fade(Elements.QuickSettingsContent) }

    fractionRange(start = 0.9f) { fade(Elements.FooterActions) }

    anchoredTranslate(Elements.QuickSettingsContent, Elements.GridAnchor)

    sharedElement(Elements.TileElementMatcher, enabled = animateTilesExpansion())
    sharedElement(Elements.BrightnessSlider)
    sharedElement(MiniPlayerElementKey.MiniPlayer)

    // This will animate between 0f (QQS) and 0.5, fading in the QQS tiles when coming back
    // from non first page QS. The QS content ends fading out at 0.43f, so there's a brief
    // overlap, but because they are really faint, it looks better than complete black without
    // overlap.
    fractionRange(end = 0.5f) { fade(SceneKeys.QqsTileElementMatcher) }
    anchoredTranslate(SceneKeys.QqsTileElementMatcher, Elements.GridAnchor)
}

fun TransitionBuilder.quickQuickSettingsToQuickSettingsOneUI() {
    spec = tween(durationMillis = 350)

    sharedElement(MiniPlayerElementKey.MiniPlayer, enabled = false)

    fractionRange(end = 0.3f) {
        fade(SceneKeys.QuickQuickSettingsContent)
        fade(SceneKeys.QqsBrightnessSlider)
        fade(QqsMiniPlayerMatcher)
    }

    fractionRange(start = 0.6f) {
        fade(Elements.QuickSettingsContent)
        fade(SceneKeys.QsBrightnessSlider)
        fade(QsMiniPlayerMatcher)
    }

    fractionRange(start = 0.75f) {
        fade(Elements.FooterActions)
    }
}
