/*
 * Copyright (C) 2026 The PenguinOS Project
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.internal.penguin.glass;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Universal bottom tab bar scanner and coordinator for View and Jetpack Compose hierarchies.
 *
 * @hide
 */
public final class LiquidTabBars {
    private static final String TAG = "LiquidTabBars";

    public static final String SETTING_ENABLED = "penguin_glass_tab_bars";
    public static final String SETTING_BLAZE_ENABLED = "blaze_glass_tab_bars";
    public static final String SETTING_EXCLUDED = "penguin_glass_tab_bars_excluded";
    public static final String SETTING_FLOAT = "penguin_glass_tab_bars_float";

    private static final String[] BAR_CLASSES = {
            "BottomNavigationView", "NavigationBarView", "PivotBar", "BottomNavigation",
            "BottomTabLayout", "BottomNavBar", "BottomNav", "BottomBar", "TabBar",
            "FloatingTabBar", "FloatingToolbarLayout",
    };
    private static final String[] BAR_IDS = {
            "bottom_nav", "bottom_nav_bar", "bottom_navigation", "navigation_bar",
            "googleapp_navigation_bar", "hub_tabs_nav", "tab_bar", "photos_tabbar_layout",
            "bottom_tab_bar",
    };
    private static final String[] NON_BAR_CLASSES = {
            "RecyclerView", "ListView", "ScrollView", "GridView", "ViewPager",
            "Toolbar", "ActionBar", "AppBarLayout", "CollapsingToolbar",
    };
    private static final String[] SCROLLER_CLASSES = {
            "RecyclerView", "ListView", "ScrollView", "GridView",
            "AndroidComposeView", "ComposeView", "NestedScrollView", "ViewPager2",
    };
    private static final long SCAN_INTERVAL_MS = 450;
    private static final long EAGER_MS = 3500;

    private final Context mContext;
    private final boolean mFloat;
    private final float mDensity;
    private final List<LiquidTabBar> mBars = new ArrayList<>();
    private ComposeTabBar mComposeBar;
    private long mNextComposeScan;
    private int mComposeMisses;
    private final Set<View> mPadded = Collections.newSetFromMap(new WeakHashMap<>());
    private long mLastScan;
    private boolean mScanPending;
    private final long mCreated;

    private LiquidTabBars(Context context, boolean floatOverContent) {
        mContext = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        mFloat = floatOverContent;
        mDensity = context.getResources().getDisplayMetrics().density;
        mCreated = SystemClock.uptimeMillis();
    }

    public static LiquidTabBars create(Context context) {
        if (context == null) return null;
        if (Process.myUid() < Process.FIRST_APPLICATION_UID) return null;
        String pkg = context.getPackageName();
        try {
            int blazeEnabled = Settings.Secure.getInt(
                    context.getContentResolver(), SETTING_BLAZE_ENABLED, 1);
            int penguinEnabled = Settings.Secure.getInt(
                    context.getContentResolver(), SETTING_ENABLED, blazeEnabled);
            if (blazeEnabled == 0 || penguinEnabled == 0) {
                return null;
            }
            if (pkg != null && (pkg.startsWith("com.android.systemui")
                    || pkg.startsWith("com.android.launcher")
                    || pkg.contains("aperture")
                    || pkg.contains("camera")
                    || pkg.contains("inputmethod")
                    || listed(context, SETTING_EXCLUDED, pkg))) {
                return null;
            }
            boolean floatOver = Settings.Secure.getInt(
                    context.getContentResolver(), SETTING_FLOAT, 1) != 0
                    || listed(context, SETTING_FLOAT, pkg);
            return new LiquidTabBars(context, floatOver);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean listed(Context context, String key, String pkg) {
        String list = Settings.Secure.getString(context.getContentResolver(), key);
        return list != null && Arrays.asList(list.split(",")).contains(pkg);
    }

    public void onLayout(View root) {
        if (LiquidGlass.getEffectiveStyle(mContext) == LiquidGlass.STYLE_MATERIAL3_LITE) {
            for (int i = mBars.size() - 1; i >= 0; i--) {
                mBars.get(i).remove();
                mBars.remove(i);
            }
            if (mComposeBar != null) {
                mComposeBar.remove();
                mComposeBar = null;
            }
            return;
        }
        boolean attached = false;
        for (int i = mBars.size() - 1; i >= 0; i--) {
            LiquidTabBar bar = mBars.get(i);
            if (bar.mBar.isAttachedToWindow() && bar.mBar.getVisibility() == View.VISIBLE) {
                attached = true;
            } else {
                bar.remove();
                mBars.remove(i);
            }
        }
        if (mComposeBar != null) {
            if (mComposeBar.mView.isAttachedToWindow()
                    && mComposeBar.mView.getVisibility() == View.VISIBLE
                    && mComposeBar.update()) {
                attached = true;
            } else {
                mComposeBar.remove();
                mComposeBar = null;
            }
        }
        if (attached || !(root instanceof ViewGroup)) return;
        long now = SystemClock.uptimeMillis();
        boolean eager = now - mCreated < EAGER_MS;
        long wait = eager ? 0 : SCAN_INTERVAL_MS - (now - mLastScan);
        if (wait > 0) {
            if (!mScanPending) {
                mScanPending = true;
                root.postDelayed(() -> {
                    mScanPending = false;
                    if (root.isAttachedToWindow()) onLayout(root);
                }, wait);
            }
            return;
        }
        mLastScan = now;
        ViewGroup found = find((ViewGroup) root, root.getWidth(), root.getHeight());
        if (found == null) {
            if (eager || now >= mNextComposeScan) {
                findCompose((ViewGroup) root, root.getHeight());
                if (mComposeBar == null) {
                    if (!eager) {
                        mComposeMisses = Math.min(mComposeMisses + 1, 1);
                    }
                    mNextComposeScan = now + (SCAN_INTERVAL_MS << mComposeMisses);
                } else {
                    mComposeMisses = 0;
                    padScrollers(root, mComposeBar.getBarHeight());
                    liftBottomFabs(root, mComposeBar.getBarHeight());
                }
            }
            if (mComposeBar == null && !mScanPending) {
                mScanPending = true;
                root.postDelayed(() -> {
                    mScanPending = false;
                    if (root.isAttachedToWindow()) onLayout(root);
                }, Math.max(mNextComposeScan - now, SCAN_INTERVAL_MS));
            }
            return;
        }
        try {
            mBars.add(new LiquidTabBar(found));
            if (mFloat) floatOverContent(found);
        } catch (RuntimeException e) {
            Log.w(TAG, "Could not restyle " + found.getClass().getName(), e);
        }
    }

    public boolean onTouch(MotionEvent ev) {
        if (mComposeBar != null && mComposeBar.onTouch(ev)) return true;
        for (int i = 0; i < mBars.size(); i++) {
            LiquidTabBar bar = mBars.get(i);
            View view = bar.mBar;
            if (!view.isAttachedToWindow() || !view.isShown()) continue;
            view.getLocationInWindow(mLocation);
            MotionEvent local = MotionEvent.obtain(ev);
            local.offsetLocation(-mLocation[0], -mLocation[1]);
            float sx = view.getScaleX();
            float sy = view.getScaleY();
            if (sx != 1f || sy != 1f) {
                local.setLocation(local.getX() / sx, local.getY() / sy);
            }
            bar.onObserveTouch(local);
            local.recycle();
        }
        return false;
    }

    private final int[] mLocation = new int[2];

    private void findCompose(ViewGroup group, int rootHeight) {
        int maxGap = Math.round(96 * mDensity);
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() == 0) continue;
            child.getLocationInWindow(mLocation);
            if (mLocation[1] + child.getHeight() < rootHeight - maxGap) continue;
            if (child.getAccessibilityNodeProvider() != null) {
                try {
                    mComposeBar = ComposeTabBar.find(child, mFloat, mPadded);
                } catch (RuntimeException e) {
                    Log.w(TAG, "Could not look for a Compose tab bar", e);
                }
                if (mComposeBar != null) {
                    return;
                }
            }
            if (child instanceof ViewGroup g) {
                findCompose(g, rootHeight);
                if (mComposeBar != null) return;
            }
        }
    }

    private ViewGroup find(ViewGroup group, int rootWidth, int rootHeight) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || !(child instanceof ViewGroup g)) continue;
            if (isBar(g, rootWidth, rootHeight)) return g;
            ViewGroup found = find(g, rootWidth, rootHeight);
            if (found != null) return found;
        }
        return null;
    }

    private boolean isBar(ViewGroup v, int rootWidth, int rootHeight) {
        if (matches(v.getClass(), NON_BAR_CLASSES)) return false;
        int h = v.getHeight();
        boolean knownBar = matches(v.getClass(), BAR_CLASSES) || hasBarResourceId(v);
        float minWidthRatio = knownBar ? 0.65f : 0.88f;
        if (v.getWidth() < rootWidth * minWidthRatio
                || h < 42 * mDensity
                || h > 160 * mDensity) {
            return false;
        }
        int[] loc = new int[2];
        v.getLocationInWindow(loc);
        int bottomInset = 0;
        android.view.WindowInsets insets = v.getRootWindowInsets();
        if (insets != null) {
            bottomInset = insets.getInsets(
                    android.view.WindowInsets.Type.systemBars()
                            | android.view.WindowInsets.Type.displayCutout()).bottom;
        }
        int maxGap = Math.max(bottomInset + Math.round(32 * mDensity), Math.round(92 * mDensity));
        if (loc[1] + h < rootHeight - maxGap) return false;
        if (knownBar) {
            return hasHorizontalTabRow(v, v.getWidth(), 2) || hasComposeTabChild(v);
        }
        return isStructuralTabBar(v, rootWidth);
    }

    private boolean hasComposeTabChild(ViewGroup v) {
        for (int i = 0; i < v.getChildCount(); i++) {
            View child = v.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() < v.getWidth() * 0.8f) {
                continue;
            }
            if (child.getAccessibilityNodeProvider() != null) return true;
            if (child instanceof ViewGroup inner && hasComposeTabChild(inner)) return true;
        }
        return false;
    }

    private boolean isStructuralTabBar(ViewGroup v, int rootWidth) {
        if (v.getHeight() > 96 * mDensity) return false;
        int visible = 0;
        int clickable = 0;
        int selected = 0;
        int withIconAndLabel = 0;
        int withIcon = 0;
        int totalWidth = 0;
        int lastRight = Integer.MIN_VALUE / 2;
        for (int i = 0; i < v.getChildCount(); i++) {
            View child = v.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() == 0) continue;
            if (child.getWidth() < rootWidth * 0.14f || child.getWidth() > rootWidth * 0.38f) {
                return false;
            }
            if (child.getLeft() < lastRight - 4) return false;
            lastRight = child.getRight();
            totalWidth += child.getWidth();
            visible++;
            if (child.isClickable()) clickable++;
            if (hasSelectedState(child, 2)) selected++;
            boolean icon = hasVisibleClass(child, ImageView.class, 3);
            boolean label = hasVisibleClass(child, TextView.class, 3);
            if (icon) withIcon++;
            if (icon && label) withIconAndLabel++;
        }
        if (visible < 3 || visible > 6 || clickable < visible - 1 || selected < 1) {
            return false;
        }
        if (totalWidth < v.getWidth() * 0.85f) return false;
        return withIcon >= visible - 1 && withIconAndLabel >= visible - 2;
    }

    private static boolean hasSelectedState(View v, int depth) {
        if (v.isSelected() || v.isActivated()) return true;
        for (int s : v.getDrawableState()) {
            if (s == android.R.attr.state_selected || s == android.R.attr.state_checked) return true;
        }
        if (depth > 0 && v instanceof ViewGroup g) {
            for (int i = 0; i < g.getChildCount(); i++) {
                if (hasSelectedState(g.getChildAt(i), depth - 1)) return true;
            }
        }
        return false;
    }

    private static boolean hasVisibleClass(View v, Class<?> target, int depth) {
        if (v.getVisibility() != View.VISIBLE) return false;
        if (target.isInstance(v)) return true;
        if (depth > 0 && v instanceof ViewGroup g) {
            for (int i = 0; i < g.getChildCount(); i++) {
                if (hasVisibleClass(g.getChildAt(i), target, depth - 1)) return true;
            }
        }
        return false;
    }

    private boolean hasHorizontalTabRow(ViewGroup v, int barWidth, int minTabs) {
        if (hasHorizontalTabRowDirect(v, barWidth, minTabs)) return true;
        ViewGroup inner = singleWideChild(v, barWidth);
        if (inner != null) {
            if (hasHorizontalTabRowDirect(inner, inner.getWidth(), minTabs)) return true;
            ViewGroup inner2 = singleWideChild(inner, inner.getWidth());
            if (inner2 != null) {
                return hasHorizontalTabRowDirect(inner2, inner2.getWidth(), minTabs);
            }
        }
        return false;
    }

    private ViewGroup singleWideChild(ViewGroup v, int barWidth) {
        ViewGroup candidate = null;
        for (int i = 0; i < v.getChildCount(); i++) {
            View child = v.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE
                    || child.getWidth() == 0
                    || child.getHeight() <= Math.round(4 * mDensity)) {
                continue;
            }
            if (child instanceof ViewGroup g && g.getWidth() >= barWidth * 0.8f) {
                if (candidate != null) return null;
                candidate = g;
            } else {
                return null;
            }
        }
        return candidate;
    }

    private boolean hasHorizontalTabRowDirect(ViewGroup v, int barWidth, int minTabs) {
        int visible = 0;
        int clickableOrSelected = 0;
        int lastRight = Integer.MIN_VALUE / 2;
        for (int i = 0; i < v.getChildCount(); i++) {
            View child = v.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE
                    || child.getWidth() == 0
                    || child.getHeight() <= Math.round(4 * mDensity)) {
                continue;
            }
            if (child.getWidth() < barWidth * 0.11f || child.getWidth() > barWidth * 0.56f) {
                return false;
            }
            if (child.getLeft() < lastRight - 4) return false;
            lastRight = child.getRight();
            visible++;
            if (child.isClickable() || hasSelectedState(child, 2)) {
                clickableOrSelected++;
            }
        }
        return visible >= minTabs && visible <= 6 && clickableOrSelected >= Math.min(2, minTabs);
    }

    private static boolean hasBarResourceId(View v) {
        String name = entryName(v);
        if (name == null) return false;
        for (String id : BAR_IDS) {
            if (name.equals(id)) return true;
        }
        return false;
    }

    private static String entryName(View v) {
        int id = v.getId();
        if (id == View.NO_ID || (id >>> 24) == 0) return null;
        try {
            return v.getResources().getResourceEntryName(id);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean matches(Class<?> cls, String[] names) {
        for (Class<?> c = cls; c != null && c != View.class; c = c.getSuperclass()) {
            String name = c.getSimpleName();
            for (String n : names) {
                if (name.contains(n)) return true;
            }
        }
        return false;
    }

    private void floatOverContent(ViewGroup bar) {
        int barHeight = bar.getHeight();
        alignAndLiftSiblingFabs(bar, barHeight);
        hideSiblingShadowsAndGradients(bar, barHeight);
        View target = bar;
        for (int depth = 0; depth < 3 && target.getParent() instanceof ViewGroup parent;
                depth++, target = parent) {
            if (depth > 0 && target.getHeight() > barHeight + Math.round(28 * mDensity)) {
                break;
            }
            hideSiblingShadowsAndGradients(target, barHeight);
            View content = null;
            if (parent instanceof LinearLayout ll && ll.getOrientation() == LinearLayout.VERTICAL) {
                int index = parent.indexOfChild(target);
                if (index > 0
                        && target.getLayoutParams() instanceof ViewGroup.MarginLayoutParams lp) {
                    View candidate = null;
                    for (int k = index - 1; k >= 0; k--) {
                        View prev = parent.getChildAt(k);
                        if (prev.getVisibility() != View.VISIBLE) continue;
                        if (prev.getHeight() > barHeight * 2) {
                            content = prev;
                            break;
                        } else if (candidate == null && prev.getHeight() >= Math.round(48 * mDensity)) {
                            // Miniplayer (~56-72dp) or intermediate sibling
                            candidate = prev;
                        }
                    }
                    if (content == null) {
                        content = candidate;
                    }
                    if (content != null) {
                        lp.topMargin -= barHeight;
                        target.setLayoutParams(lp);
                        if (candidate != null && candidate != content) {
                            padScrollers(candidate, barHeight);
                            liftBottomFabs(candidate, barHeight);
                        }
                    }
                }
            } else {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View child = parent.getChildAt(i);
                    if (child == target || Math.abs(child.getBottom() - target.getTop()) > 4) {
                        continue;
                    }
                    if (extendToBottom(child, target, barHeight)) {
                        content = child;
                        break;
                    }
                }
            }
            if (content != null) {
                padScrollers(content, barHeight);
                liftBottomFabs(content, barHeight);
                alignAndLiftSiblingFabs(bar, barHeight);
                return;
            }
        }
    }

    private void hideSiblingShadowsAndGradients(View barOrWrapper, int barHeight) {
        if (!(barOrWrapper.getParent() instanceof ViewGroup parent)) return;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View sibling = parent.getChildAt(i);
            if (sibling == barOrWrapper || sibling.getVisibility() != View.VISIBLE) continue;
            String name = entryName(sibling);
            int sh = sibling.getHeight();
            if (sh > 0 && sh <= Math.round(4 * mDensity)
                    && Math.abs(sibling.getBottom() - barOrWrapper.getTop()) <= Math.round(8 * mDensity)) {
                sibling.setAlpha(0f);
                continue;
            }
            if (name != null && (name.contains("tab_bar_shadow")
                    || name.contains("bottom_gradient")
                    || name.contains("footer_container_above_shadow"))) {
                sibling.setAlpha(0f);
            }
        }
    }

    private void liftBottomFabs(View view, int barHeight) {
        if (!(view instanceof ViewGroup group)) return;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            String name = entryName(child);
            String cls = child.getClass().getSimpleName();
            boolean isFab = (name != null && (name.equals("fab_container") || name.equals("fab")
                    || name.contains("search_fab")
                    || name.endsWith("_fab") || name.startsWith("fab_")))
                    || cls.contains("FloatingActionButton")
                    || cls.contains("FloatingToolbarLayout");
            if (isFab && child.getBottom() > group.getHeight() - barHeight
                    && !mPadded.contains(child)) {
                mPadded.add(child);
                if (child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams lp) {
                    lp.bottomMargin += barHeight;
                    child.setLayoutParams(lp);
                } else {
                    child.setTranslationY(child.getTranslationY() - barHeight);
                }
                continue;
            }
            if (child instanceof ViewGroup && !matches(child.getClass(), SCROLLER_CLASSES)) {
                liftBottomFabs(child, barHeight);
            }
        }
    }

    private void alignAndLiftSiblingFabs(ViewGroup bar, int barHeight) {
        if (!(bar.getParent() instanceof ViewGroup parent)) return;
        alignFabsInGroup(parent, bar, barHeight);
        if (parent.getParent() instanceof ViewGroup grandParent) {
            alignFabsInGroup(grandParent, bar, barHeight);
        }
    }

    private void alignFabsInGroup(ViewGroup group, ViewGroup bar, int barHeight) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View sibling = group.getChildAt(i);
            if (sibling == bar || sibling.getVisibility() != View.VISIBLE) continue;
            String name = entryName(sibling);
            String cls = sibling.getClass().getSimpleName();
            boolean isFabOrToolbar = (name != null && (name.contains("search_fab")
                    || name.contains("fab_container") || name.endsWith("_fab") || name.startsWith("fab_")))
                    || cls.contains("FloatingToolbarLayout")
                    || cls.contains("FloatingActionButton");
            if (isFabOrToolbar) {
                if (bar.getTranslationY() != 0f && sibling.getTranslationY() == 0f) {
                    sibling.setTranslationY(bar.getTranslationY());
                }
                if (bar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams barLp
                        && sibling.getLayoutParams() instanceof ViewGroup.MarginLayoutParams sibLp) {
                    if (barLp.topMargin < 0 && sibLp.topMargin >= 0) {
                        sibLp.topMargin += barLp.topMargin;
                        sibling.setLayoutParams(sibLp);
                    }
                }
            }
        }
    }

    private static boolean extendToBottom(View child, View bar, int barHeight) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        if (lp instanceof RelativeLayout.LayoutParams rl
                && rl.getRule(RelativeLayout.ABOVE) == bar.getId()) {
            rl.removeRule(RelativeLayout.ABOVE);
            rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            child.setLayoutParams(rl);
            return true;
        }
        if (setConstraint(lp, bar.getId())) {
            child.setLayoutParams(lp);
            return true;
        }
        if (lp instanceof ViewGroup.MarginLayoutParams m && m.bottomMargin >= barHeight - 2) {
            m.bottomMargin -= barHeight;
            child.setLayoutParams(m);
            return true;
        }
        return false;
    }

    private static boolean setConstraint(ViewGroup.LayoutParams lp, int barId) {
        try {
            Class<?> cls = lp.getClass();
            Field toTop = cls.getField("bottomToTop");
            if (barId == View.NO_ID || toTop.getInt(lp) != barId) return false;
            toTop.setInt(lp, -1);
            cls.getField("bottomToBottom").setInt(lp, 0);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private void padScrollers(View view, int barHeight) {
        if (view instanceof ViewGroup group) {
            if (matches(view.getClass(), SCROLLER_CLASSES) && !mPadded.contains(view)) {
                mPadded.add(view);
                group.setClipToPadding(false);
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                        view.getPaddingRight(), view.getPaddingBottom() + barHeight);
                return;
            }
            for (int i = 0; i < group.getChildCount(); i++) {
                padScrollers(group.getChildAt(i), barHeight);
            }
        }
    }
}
