/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.penguin

import android.graphics.Rect
import android.view.View

interface StatusBarIsland {
    fun attach(statusBar: View, onBoundsChanged: (Rect) -> Unit)

    fun attachKeyguard(statusBar: View)
}
