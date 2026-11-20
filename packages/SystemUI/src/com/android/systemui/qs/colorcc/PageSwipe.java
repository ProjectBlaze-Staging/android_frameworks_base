/*
 * Copyright (C) 2026 Guest (ColorCC)
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.qs.colorcc;

import android.app.KeyguardManager;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class PageSwipe {
    private static final long FOLLOW_NS = 120000000;
    private static volatile float controls = 1.0f;
    private static volatile long reportedAt;
    static volatile boolean touching;
    private static final List<View> rows = new ArrayList();
    private static final Matrix matrix = new Matrix();
    static volatile boolean moveRows = false;

    private PageSwipe() {
    }

    static boolean following() {
        long j = reportedAt;
        if (j != 0) {
            return System.nanoTime() - j < FOLLOW_NS || touching || ColorCCBridge.isSliding();
        }
        return false;
    }

    static float shown() {
        return controls;
    }

    static void onNativeSlide(int panelTransitionState, float fraction, float yFraction, String directionName, int targetState, View shadeRootView) {
        int r5 = 0;
        boolean z = "LEFT".equals(directionName) || "RIGHT".equals(directionName);
        if (panelTransitionState != 2 || !z || yFraction != 0.0f) {
            end();
            return;
        }
        float fClamp = Logic.clamp(fraction);
        if (targetState != 0) {
            fClamp = 1.0f - fClamp;
        }
        controls = fClamp;
        boolean z2 = reportedAt == 0;
        reportedAt = System.nanoTime();
        if (z2 && moveRows) {
            collect(shadeRootView);
        }
        if (shadeRootView != null && !rows.isEmpty() && shadeRootView.getTranslationX() != 0.0f) {
            shadeRootView.setTranslationX(0.0f);
        }
        float f = 1.0f - controls;
        float f2 = ColorCCBridge.isReversed() ? 1.0f : -1.0f;
        int width = shadeRootView == null ? 0 : shadeRootView.getRootView().getWidth();
        while (true) {
            List<View> list = rows;
            if (r5 >= list.size()) {
                return;
            }
            float fClamp2 = Logic.clamp(f);
            Matrix matrix2 = matrix;
            matrix2.setTranslate(width * f2 * (1.0f - fClamp2), 0.0f);
            list.get(r5).setAnimationMatrix(matrix2);
            r5++;
        }
    }

    static void onNativeSlideEnd() {
        end();
    }

    private static void collect(View view) {
        rows.clear();
        if (view == null) {
            return;
        }
        KeyguardManager keyguardManager = (KeyguardManager) view.getContext().getSystemService(KeyguardManager.class);
        if (keyguardManager == null || !keyguardManager.isKeyguardLocked()) {
            int identifier = view.getResources().getIdentifier("notification_stack_scroller", "id", "com.android.systemui");
            View viewFindViewById = identifier == 0 ? null : view.getRootView().findViewById(identifier);
            if (viewFindViewById instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewFindViewById;
                int height = viewGroup.getRootView().getHeight();
                int[] r1 = new int[2];
                for (int r2 = 0; r2 < viewGroup.getChildCount(); r2++) {
                    View childAt = viewGroup.getChildAt(r2);
                    if (childAt.getVisibility() == 0 && childAt.getWidth() != 0 && childAt.getHeight() != 0) {
                        childAt.getLocationOnScreen(r1);
                        if (r1[1] <= height) {
                            rows.add(childAt);
                        }
                    }
                }
            }
        }
    }

    private static void end() {
        if (reportedAt == 0) {
            return;
        }
        reportedAt = 0L;
        Iterator<View> it = rows.iterator();
        while (it.hasNext()) {
            it.next().setAnimationMatrix(null);
        }
        rows.clear();
    }
}
