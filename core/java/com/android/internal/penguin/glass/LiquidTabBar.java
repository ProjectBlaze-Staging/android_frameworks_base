/*
 * Copyright (C) 2026 The PenguinOS Project
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.internal.penguin.glass;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RenderNode;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.dynamicanimation.animation.FloatValueHolder;
import com.android.internal.dynamicanimation.animation.SpringAnimation;
import com.android.internal.dynamicanimation.animation.SpringForce;

final class LiquidTabBar implements ViewTreeObserver.OnPreDrawListener,
        View.OnLayoutChangeListener {

    private static final float SIDE_MARGIN_DP = 16f;
    private static final float INNER_PADDING_DP = 4f;
    private static final float VERTICAL_INSET_DP = 2.5f;
    private static final float HEIGHT_DP = 64f;
    private static final float BLUR_DP = 20f;
    private static final float LENS_DP = 24f;
    private static final float PRESSED_SCALE = 78f / 56f;

    private static final int ACCENT_LIGHT = 0xFF0088FF;
    private static final int ACCENT_DARK = 0xFF0091FF;
    private static final int LABEL_LIGHT = 0xFF1C1C1E;
    private static final int LABEL_DARK = 0xFFF2F2F7;

    final ViewGroup mBar;
    private final float mDensity;
    private final LiquidGlass mGlass = new LiquidGlass();
    private final Pill mPill = new Pill();
    private Drawable mOriginalBackground;
    private final ColorStateList mOriginalBackgroundTint;
    private final boolean mOriginalClipToOutline;
    private final float mOriginalElevation;
    private final android.view.ViewOutlineProvider mOriginalOutline;
    private final boolean mOriginalClipChildren;
    private final boolean mOriginalClipToPadding;

    private final LiquidMotion mMotion = new LiquidMotion(PRESSED_SCALE, this::onMotion);
    private final Lens mLens = new Lens();
    private final Drawable mOriginalForeground;

    private final FloatValueHolder mOffset = new FloatValueHolder();
    private final SpringAnimation mOffsetSpring = new SpringAnimation(mOffset);
    private final float mTouchSlop;
    private boolean mDragging;
    private boolean mMoved;
    private float mDownX;
    private float mLastX;

    private final RectF mCapsule = new RectF();
    private final Rect mTmp = new Rect();
    private ViewGroup mItems;
    private View mComposeChild;
    private int[] mComposeTabIds;
    private long mLastComposeQueryTime;
    private static final long COMPOSE_QUERY_THROTTLE_MS = 500;
    private int mSelected = -1;
    private int mItemCount;
    private boolean mDark;
    private int mAddedPadding;
    private int mLastGlassKey;
    private float mLabelLuminance = -1f;
    private float mBgLuminance = -1f;
    private ViewGroup mWrapperParent;
    private Drawable mWrapperOriginalBackground;
    private final java.util.ArrayList<View[]> mTabParts = new java.util.ArrayList<>();
    private final java.util.WeakHashMap<View, Drawable> mOriginalChildBackgrounds = new java.util.WeakHashMap<>();

    LiquidTabBar(ViewGroup bar) {
        mBar = bar;
        mDensity = bar.getResources().getDisplayMetrics().density;
        mOriginalBackground = bar.getBackground();
        mOriginalBackgroundTint = bar.getBackgroundTintList();
        mOriginalClipToOutline = bar.getClipToOutline();
        if (mOriginalBackground instanceof android.graphics.drawable.ColorDrawable cd
                && cd.getAlpha() > 128) {
            mBgLuminance = android.graphics.Color.luminance(cd.getColor());
        }
        TextView initialLabel = findLabel(bar);
        if (initialLabel != null) {
            mLabelLuminance = android.graphics.Color.luminance(initialLabel.getCurrentTextColor());
        }
        mOriginalElevation = bar.getElevation();
        mOriginalOutline = bar.getOutlineProvider();
        mOriginalForeground = bar.getForeground();
        mOriginalClipChildren = bar.getClipChildren();
        mOriginalClipToPadding = bar.getClipToPadding();
        mTouchSlop = ViewConfiguration.get(bar.getContext()).getScaledTouchSlop();
        mOffsetSpring.setSpring(new SpringForce().setDampingRatio(1f).setStiffness(300f));
        mOffsetSpring.setMinimumVisibleChange(0.5f);
        mOffsetSpring.addUpdateListener((anim, value, velocity) -> onMotion());

        // Unclip bar and up to 4 ancestor ViewGroups so top edge and magnified pill never clip
        bar.setClipChildren(false);
        bar.setClipToPadding(false);
        bar.setClipToOutline(false);
        ViewParent p = bar.getParent();
        for (int depth = 0; depth < 4 && p instanceof ViewGroup vg; depth++, p = vg.getParent()) {
            vg.setClipChildren(false);
            vg.setClipToPadding(false);
        }

        bar.setBackgroundTintList(null);
        bar.setBackground(mPill);
        bar.setForeground(mLens);
        bar.setElevation(0f);
        bar.setOutlineProvider(null);
        bar.addOnLayoutChangeListener(this);
        bar.getViewTreeObserver().addOnPreDrawListener(this);
        restyleMaterialItems();
        relayout();
    }

    void remove() {
        mMotion.cancel();
        mOffsetSpring.cancel();
        mBar.setScaleX(1f);
        mBar.setScaleY(1f);
        mBar.setTranslationX(0f);
        mBar.setClipChildren(mOriginalClipChildren);
        mBar.setClipToPadding(mOriginalClipToPadding);
        mBar.setClipToOutline(mOriginalClipToOutline);
        mBar.setForeground(mOriginalForeground);
        mBar.removeOnLayoutChangeListener(this);
        mBar.getViewTreeObserver().removeOnPreDrawListener(this);
        LiquidGlass.removeFrom(mBar);
        setAddedPadding(0);
        restoreItemsInset();
        if (mWrapperParent != null) {
            mWrapperParent.setBackground(mWrapperOriginalBackground);
            mWrapperParent = null;
            mWrapperOriginalBackground = null;
        }
        mBar.setBackgroundTintList(mOriginalBackgroundTint);
        mBar.setBackground(mOriginalBackground);
        mBar.setElevation(mOriginalElevation);
        mBar.setOutlineProvider(mOriginalOutline);
        for (java.util.Map.Entry<View, Drawable> entry : mOriginalChildBackgrounds.entrySet()) {
            View child = entry.getKey();
            if (child != null && entry.getValue() != null) {
                child.setBackground(entry.getValue());
            }
        }
        mOriginalChildBackgrounds.clear();
    }

    private float dp(float v) {
        return v * mDensity;
    }

    private void setAddedPadding(int added) {
        if (added == mAddedPadding) return;
        int delta = added - mAddedPadding;
        mAddedPadding = added;
        mBar.setPaddingRelative(mBar.getPaddingStart() + delta, mBar.getPaddingTop(),
                mBar.getPaddingEnd() + delta, mBar.getPaddingBottom());
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
            int oldTop, int oldRight, int oldBottom) {
        relayout();
    }

    private void relayout() {
        int w = mBar.getWidth();
        int h = mBar.getHeight();
        if (w == 0 || h == 0) return;
        if (mWrapperParent == null && mBar.getParent() instanceof ViewGroup parent
                && parent.getHeight() > 0 && parent.getHeight() <= h + Math.round(dp(24f))
                && parent.getBackground() != null) {
            mWrapperParent = parent;
            mWrapperOriginalBackground = parent.getBackground();
            parent.setBackground(null);
        }
        mItems = findItems(mBar);
        mComposeChild = (mItems == null) ? findComposeChild(mBar) : null;

        int screenWidth = mBar.getResources().getDisplayMetrics().widthPixels;
        boolean compactWidth = screenWidth > 0 && w < screenWidth * 0.85f;
        float sideDp = compactWidth ? 2f : SIDE_MARGIN_DP;
        float vInset = dp(VERTICAL_INSET_DP);
        float contentTop = Math.max(mBar.getPaddingTop(), vInset);
        float contentBottom = h - Math.max(mBar.getPaddingBottom(), vInset);
        if (mItems != null && mItems != mBar && mItems.getHeight() > 0 && mItems.getBottom() > 0) {
            contentTop = Math.max(dp(1.5f), mItems.getTop());
            contentBottom = Math.min(h - dp(1.5f), mItems.getBottom() + dp(5f));
        }
        float availableHeight = Math.max(dp(44f), contentBottom - contentTop);
        float height = Math.min(dp(HEIGHT_DP), availableHeight);
        float cy = (contentTop + contentBottom) / 2f;
        float side = dp(sideDp);
        mCapsule.set(side, cy - height / 2f, w - side, cy + height / 2f);

        mDark = isDark();
        int style = LiquidGlass.getEffectiveStyle(mBar.getContext());
        int key = ((int) mCapsule.left * 31 + (int) mCapsule.top * 17 + (int) mCapsule.right * 7
                + (int) mCapsule.bottom) * 8 + (mDark ? 4 : 0) + style;
        if (key != mLastGlassKey) {
            mLastGlassKey = key;
            boolean darkCrystal = (style == LiquidGlass.STYLE_IOS27_DARK_CRYSTAL) && mDark;
            int tint = darkCrystal
                    ? LiquidGlass.TINT_DARK_CRYSTAL
                    : (mDark ? LiquidGlass.TINT_DARK : LiquidGlass.TINT_LIGHT);
            float rimAlpha = darkCrystal ? 0.55f : (mDark ? 0.35f : 0.5f);
            float lensSize = dp(darkCrystal ? 28f : LENS_DP);
            mGlass.setShape(mCapsule.left, mCapsule.top, mCapsule.right, mCapsule.bottom,
                            height / 2f)
                    .setLens(lensSize, lensSize, 0f)
                    .setLook(dp(BLUR_DP), darkCrystal ? 1.65f : 1.5f, rimAlpha, tint)
                    .applyTo(mBar);
        }
        int inset = Math.round(dp(sideDp + INNER_PADDING_DP));
        if (mItems == mBar) {
            if (mBar.getPaddingStart() < inset) mAddedPadding = 0;
            setAddedPadding(inset);
        } else if (mItems != null) {
            insetItems(mItems, inset);
        } else if (mComposeChild != null && mBar.getChildCount() > 0
                && mBar.getChildAt(0) instanceof ViewGroup composeWrapper) {
            insetItems(composeWrapper, inset);
        }
        mComposeTabIds = null;
        mLastComposeQueryTime = SystemClock.uptimeMillis();
        collectTabParts();
        updateSelection(mSelected >= 0);
        recolorTabs();
    }

    private static View findComposeChild(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() == 0) continue;
            if (child.getAccessibilityNodeProvider() != null) return child;
            if (child instanceof ViewGroup inner) {
                View found = findComposeChild(inner);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void collectTabParts() {
        mTabParts.clear();
        if (mItems == null) return;
        for (int i = 0; i < mItems.getChildCount(); i++) {
            View child = mItems.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() == 0) continue;
            View[] parts = new View[6];
            parts[5] = child;
            collect(child, parts);
            mTabParts.add(parts);
        }
    }

    private void collect(View v, View[] parts) {
        String name = entryName(v);
        if (name != null) {
            if (name.endsWith("active_indicator_view")
                    || name.equals("active_indicator")
                    || name.endsWith("_active_indicator")) {
                parts[0] = v;
            } else if ((name.endsWith("item_icon_view") || name.equals("bottom_nav_item_image"))
                    && v instanceof ImageView) {
                parts[1] = v;
            } else if ((name.endsWith("large_label_view") || name.equals("bottom_nav_item_text"))
                    && v instanceof TextView) {
                parts[2] = v;
            } else if (name.endsWith("small_label_view") && v instanceof TextView) {
                parts[3] = v;
            } else if (name.endsWith("item_icon_container")) {
                parts[4] = v;
            }
        }
        if (parts[1] == null && v instanceof ImageView iv && iv.getVisibility() == View.VISIBLE
                && iv.getDrawable() != null && !isAvatarView(iv, name)) {
            parts[1] = iv;
        } else if (v instanceof TextView tv && tv.getVisibility() == View.VISIBLE
                && tv != parts[2] && tv != parts[3]) {
            if (parts[2] == null) parts[2] = tv;
            else if (parts[3] == null) parts[3] = tv;
        }
        if (v instanceof ViewGroup g) {
            for (int i = 0; i < g.getChildCount(); i++) collect(g.getChildAt(i), parts);
        }
    }

    private static boolean isAvatarView(ImageView iv, String name) {
        if (name != null && (name.contains("avatar") || name.contains("profile")
                || name.contains("account"))) {
            return true;
        }
        String cls = iv.getClass().getSimpleName();
        return cls.contains("CircularImageView") || cls.contains("Avatar")
                || cls.contains("SmartImageView") || cls.contains("AccountParticle");
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

    private void recolorTabs() {
        boolean darkSurface = isDarkSurface();
        int accent = darkSurface ? ACCENT_DARK : ACCENT_LIGHT;
        int label = darkSurface ? LABEL_DARK : LABEL_LIGHT;
        for (int i = 0; i < mTabParts.size(); i++) {
            View[] parts = mTabParts.get(i);
            int color = i == mSelected ? accent : label;
            if (parts[0] != null && parts[0] != parts[1] && parts[0] != parts[4]
                    && !(parts[0] instanceof ViewGroup) && parts[0].getBackground() != null) {
                if (!mOriginalChildBackgrounds.containsKey(parts[0])) {
                    mOriginalChildBackgrounds.put(parts[0], parts[0].getBackground());
                }
                parts[0].setBackground(null);
            }
            for (int j = 4; j < 6; j++) {
                if (parts[j] != null
                        && parts[j].getBackground() instanceof android.graphics.drawable.RippleDrawable) {
                    if (!mOriginalChildBackgrounds.containsKey(parts[j])) {
                        mOriginalChildBackgrounds.put(parts[j], parts[j].getBackground());
                    }
                    parts[j].setBackground(null);
                }
            }
            boolean hasLabel = parts[2] != null || parts[3] != null;
            if (parts[1] instanceof ImageView icon && icon.getDrawable() != null) {
                if (!hasLabel && icon.getWidth() > dp(36f)) {
                    continue;
                }
                icon.setImageAlpha(255);
                if (icon.getAlpha() < 0.85f) {
                    icon.setAlpha(1f);
                }
                ColorStateList tint = icon.getImageTintList();
                if (tint != null) {
                    if (tint.getDefaultColor() != color || tint.isStateful()) {
                        icon.setImageTintList(ColorStateList.valueOf(color));
                    }
                } else if (!(icon.getDrawable() instanceof android.graphics.drawable.BitmapDrawable)) {
                    icon.setImageTintList(ColorStateList.valueOf(color));
                }
            }
            for (int j = 2; j < 4; j++) {
                if (parts[j] instanceof TextView text && text.getCurrentTextColor() != color) {
                    text.setTextColor(color);
                }
            }
        }
    }

    private ViewGroup mInsetItems;
    private int mItemsMarginStart;
    private int mItemsMarginEnd;

    private void insetItems(ViewGroup targetGroup, int inset) {
        if (targetGroup == null || targetGroup == mBar || targetGroup == mInsetItems) return;
        if (!(targetGroup.getLayoutParams() instanceof ViewGroup.MarginLayoutParams lp)) return;
        restoreItemsInset();
        mInsetItems = targetGroup;
        mItemsMarginStart = lp.getMarginStart();
        mItemsMarginEnd = lp.getMarginEnd();
        lp.setMarginStart(mItemsMarginStart + inset);
        lp.setMarginEnd(mItemsMarginEnd + inset);
        targetGroup.setLayoutParams(lp);
    }

    private void restoreItemsInset() {
        if (mInsetItems != null
                && mInsetItems.getLayoutParams() instanceof ViewGroup.MarginLayoutParams lp) {
            lp.setMarginStart(mItemsMarginStart);
            lp.setMarginEnd(mItemsMarginEnd);
            mInsetItems.setLayoutParams(lp);
        }
        mInsetItems = null;
    }

    private ViewGroup findItems(ViewGroup group) {
        int visible = 0;
        int lastRight = Integer.MIN_VALUE / 2;
        boolean row = true;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() == 0) continue;
            if (child.getLeft() < lastRight - 2) row = false;
            lastRight = child.getRight();
            visible++;
        }
        if (row && visible >= 2 && visible <= 6) return group;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof ViewGroup child) {
                ViewGroup found = findItems(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static boolean isSelected(View v, int depth) {
        if (v.isSelected() || v.isActivated()) return true;
        if (v instanceof Checkable c && c.isChecked()) return true;
        for (int state : v.getDrawableState()) {
            if (state == android.R.attr.state_checked || state == android.R.attr.state_selected) {
                return true;
            }
        }
        if (depth > 0 && v instanceof ViewGroup g) {
            for (int i = 0; i < g.getChildCount(); i++) {
                if (isSelected(g.getChildAt(i), depth - 1)) return true;
            }
        }
        return false;
    }

    private void updateSelection(boolean animate) {
        if (mItems == null) {
            if (mComposeChild != null) {
                updateComposeSelection(animate);
            } else {
                mSelected = -1;
            }
            return;
        }
        int count = 0;
        int selected = -1;
        for (int i = 0; i < mItems.getChildCount(); i++) {
            View child = mItems.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() == 0) continue;
            if (selected < 0 && isSelected(child, 2)) selected = count;
            count++;
        }
        applySelection(count, selected, animate);
    }

    private void updateComposeSelection(boolean animate) {
        if (mComposeChild == null) return;
        AccessibilityNodeProvider provider = mComposeChild.getAccessibilityNodeProvider();
        if (provider == null) {
            if (mItemCount < 2) mItemCount = 5;
            applySelection(mItemCount, -1, animate);
            return;
        }
        // Direct check on cached tab virtual IDs (only 3-6 nodes) - avoids traversing the 120-node tree
        if (mComposeTabIds != null && mComposeTabIds.length >= 2) {
            int selected = -1;
            for (int i = 0; i < mComposeTabIds.length; i++) {
                AccessibilityNodeInfo tab = provider.createAccessibilityNodeInfo(mComposeTabIds[i]);
                if (tab != null && (tab.isSelected() || tab.isChecked())) {
                    selected = i;
                    break;
                }
            }
            if (selected >= 0 || mSelected >= 0) {
                applySelection(mComposeTabIds.length, selected, animate);
                return;
            }
        }
        int count = 0;
        int selected = -1;
        try {
            AccessibilityNodeInfo root = provider.createAccessibilityNodeInfo(
                    AccessibilityNodeProvider.HOST_VIEW_ID);
            if (root != null) {
                int[] result = new int[] {0, -1};
                int[] budget = new int[] {120};
                java.util.List<Integer> tabIds = new java.util.ArrayList<>();
                findComposeTabRow(mComposeChild, provider, root, budget, result, tabIds);
                count = result[0];
                selected = result[1];
                if (tabIds.size() >= 2) {
                    mComposeTabIds = new int[tabIds.size()];
                    for (int i = 0; i < tabIds.size(); i++) {
                        mComposeTabIds[i] = tabIds.get(i);
                    }
                }
            }
        } catch (RuntimeException ignored) {
        }
        if (count < 2) {
            count = mItemCount >= 2 ? mItemCount : 5;
        }
        applySelection(count, selected, animate);
    }

    private static boolean findComposeTabRow(View view, AccessibilityNodeProvider provider,
            AccessibilityNodeInfo node, int[] budget, int[] result, java.util.List<Integer> tabIds) {
        int w = Math.max(view.getWidth(), 1);
        int childCount = node.getChildCount();
        if (childCount >= 2 && childCount <= 8) {
            int tabs = 0;
            int selected = -1;
            int lastRight = Integer.MIN_VALUE / 2;
            boolean validRow = true;
            int[] screenLoc = new int[2];
            view.getLocationOnScreen(screenLoc);
            Rect r = new Rect();
            tabIds.clear();
            for (int i = 0; i < childCount && budget[0] > 0; i++) {
                int id = AccessibilityNodeInfo.getVirtualDescendantId(node.getChildId(i));
                AccessibilityNodeInfo child = provider.createAccessibilityNodeInfo(id);
                budget[0]--;
                if (child == null || !child.isVisibleToUser()) continue;
                child.getBoundsInScreen(r);
                r.offset(-screenLoc[0], -screenLoc[1]);
                if (r.width() < w * 0.11f || r.width() > w * 0.56f) continue;
                if (r.left < lastRight - 4) {
                    validRow = false;
                    break;
                }
                lastRight = r.right;
                tabIds.add(id);
                if (selected < 0 && isComposeNodeSelected(provider, child, 2, budget)) {
                    selected = tabs;
                }
                tabs++;
            }
            if (validRow && tabs >= 2 && tabs <= 6) {
                result[0] = tabs;
                result[1] = selected;
                return true;
            }
            tabIds.clear();
        }
        for (int i = 0; i < childCount && budget[0] > 0; i++) {
            int id = AccessibilityNodeInfo.getVirtualDescendantId(node.getChildId(i));
            AccessibilityNodeInfo child = provider.createAccessibilityNodeInfo(id);
            budget[0]--;
            if (child == null || !child.isVisibleToUser()) continue;
            if (findComposeTabRow(view, provider, child, budget, result, tabIds)) return true;
        }
        return false;
    }

    private static boolean isComposeNodeSelected(AccessibilityNodeProvider provider,
            AccessibilityNodeInfo node, int depth, int[] budget) {
        if (node.isSelected() || node.isChecked()) return true;
        if (depth <= 0) return false;
        for (int i = 0; i < node.getChildCount() && budget[0] > 0; i++) {
            int id = AccessibilityNodeInfo.getVirtualDescendantId(node.getChildId(i));
            AccessibilityNodeInfo child = provider.createAccessibilityNodeInfo(id);
            budget[0]--;
            if (child != null && isComposeNodeSelected(provider, child, depth - 1, budget)) {
                return true;
            }
        }
        return false;
    }

    private void applySelection(int count, int selected, boolean animate) {
        mItemCount = count;
        mMotion.setMax(Math.max(0, count - 1));
        if (selected < 0 && count >= 2 && mSelected < 0) {
            selected = 0;
        }
        if (selected < 0 || selected == mSelected) return;
        if (mSelected < 0 || !animate || Float.isNaN(mMotion.value())) {
            mMotion.snapTo(selected);
        } else if (!mDragging) {
            mMotion.animateToValue(selected);
        }
        mSelected = selected;
        mPill.invalidateSelf();
    }

    @Override
    public boolean onPreDraw() {
        if (mBar.getBackground() != mPill) {
            mOriginalBackground = mBar.getBackground();
            mBar.setBackground(mPill);
            mLastGlassKey = 0;
            relayout();
        }
        if (mBar.getElevation() != 0f) mBar.setElevation(0f);
        if (mItems != null && !mDragging) {
            updateSelection(true);
        } else if (mComposeChild != null && !mDragging) {
            long now = SystemClock.uptimeMillis();
            if (now - mLastComposeQueryTime >= COMPOSE_QUERY_THROTTLE_MS) {
                mLastComposeQueryTime = now;
                updateSelection(true);
            }
        }
        recolorTabs();
        return true;
    }

    void onObserveTouch(MotionEvent ev) {
        float x = ev.getX();
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mComposeChild != null && (mSelected < 0 || mItemCount < 2)) {
                    updateComposeSelection(false);
                }
                if (!mCapsule.contains(x, ev.getY()) || mSelected < 0 || mItemCount < 2) return;
                mDragging = true;
                mMoved = false;
                mDownX = x;
                mLastX = x;
                mOffsetSpring.cancel();
                mOffset.setValue(0f);
                mMotion.pressDown();
                mMotion.updateValue(indexAt(x));
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mDragging) return;
                float dx = x - mLastX;
                mLastX = x;
                if (Math.abs(x - mDownX) > mTouchSlop) mMoved = true;
                if (dx != 0f && mMoved) {
                    float rtl = mBar.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? -1f : 1f;
                    mMotion.updateValue(mMotion.target() + rtl * dx / tabWidth());
                    mOffset.setValue(mOffset.getValue() + dx);
                    onMotion();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!mDragging) return;
                mDragging = false;
                int target = Math.round(mMotion.target());
                target = Math.max(0, Math.min(mItemCount - 1, target));
                boolean up = ev.getActionMasked() == MotionEvent.ACTION_UP;
                if (up && !mMoved) {
                    mSelected = target;
                } else if (up && mMoved && target != mSelected) {
                    performTabClick(target);
                    mSelected = target;
                } else if (!up || mMoved) {
                    target = mSelected;
                }
                mMotion.updateValue(target);
                mMotion.release();
                mOffsetSpring.animateToFinalPosition(0f);
                mLastComposeQueryTime = SystemClock.uptimeMillis();
                if (mComposeChild != null) {
                    mBar.postDelayed(() -> updateSelection(true), 220);
                } else {
                    mBar.postOnAnimation(() -> updateSelection(true));
                }
                break;
        }
    }

    private void performTabClick(int index) {
        View tab = tabAt(index);
        if (tab != null) {
            tab.performClick();
            return;
        }
        final View compose = mComposeChild;
        if (compose == null) return;
        final int count = Math.max(mItemCount, 1);
        final int rtlIndex = mBar.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL
                ? (count - 1 - index) : index;
        final int[] tabIds = mComposeTabIds;
        compose.post(() -> {
            if (!compose.isAttachedToWindow()) return;
            AccessibilityNodeProvider provider = compose.getAccessibilityNodeProvider();
            if (provider != null && tabIds != null && index >= 0 && index < tabIds.length) {
                if (performComposeNodeClick(provider, tabIds[index], 2)) {
                    return;
                }
            }
            float w = compose.getWidth();
            float h = compose.getHeight();
            if (w <= 0f || h <= 0f) return;
            float tapX = (rtlIndex + 0.5f) * (w / count);
            float tapY = h * 0.5f;
            long now = SystemClock.uptimeMillis();
            MotionEvent down = MotionEvent.obtain(
                    now, now, MotionEvent.ACTION_DOWN, tapX, tapY, 0);
            MotionEvent upEvent = MotionEvent.obtain(
                    now, now + 12, MotionEvent.ACTION_UP, tapX, tapY, 0);
            compose.dispatchTouchEvent(down);
            compose.dispatchTouchEvent(upEvent);
            down.recycle();
            upEvent.recycle();
        });
    }

    private static boolean performComposeNodeClick(AccessibilityNodeProvider provider,
            int virtualId, int depth) {
        if (provider.performAction(virtualId, AccessibilityNodeInfo.ACTION_CLICK, null)) {
            return true;
        }
        if (depth <= 0) return false;
        AccessibilityNodeInfo node = provider.createAccessibilityNodeInfo(virtualId);
        if (node == null) return false;
        for (int i = 0; i < node.getChildCount(); i++) {
            int childId = AccessibilityNodeInfo.getVirtualDescendantId(node.getChildId(i));
            if (performComposeNodeClick(provider, childId, depth - 1)) {
                return true;
            }
        }
        return false;
    }

    private float tabWidth() {
        return pillWidth();
    }

    private float pillWidth() {
        int count = Math.max(mItemCount, 1);
        return (mCapsule.width() - 2f * dp(INNER_PADDING_DP)) / count;
    }

    private float indexAt(float x) {
        float inner = dp(INNER_PADDING_DP);
        float index = (float) Math.floor((x - mCapsule.left - inner) / tabWidth());
        if (mBar.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) index = mItemCount - 1 - index;
        return Math.max(0, Math.min(mItemCount - 1, index));
    }

    private View tabAt(int index) {
        if (mItems == null) return null;
        int seen = 0;
        for (int i = 0; i < mItems.getChildCount(); i++) {
            View child = mItems.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() == 0) continue;
            if (seen++ == index) return child;
        }
        return null;
    }

    private float pillCenterX() {
        float v = mMotion.value();
        if (mBar.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) v = mItemCount - 1 - v;
        return mCapsule.left + dp(INNER_PADDING_DP) + (v + 0.5f) * tabWidth();
    }

    private void onMotion() {
        float press = mMotion.press();
        float w = Math.max(mBar.getWidth(), 1);
        float barScale = 1f + dp(12f) / w * press;
        mBar.setScaleX(barScale);
        mBar.setScaleY(barScale);
        float fraction = Math.max(-1f, Math.min(1f, mOffset.getValue() / w));
        float eased = 1f - (1f - Math.abs(fraction)) * (1f - Math.abs(fraction));
        mBar.setTranslationX(dp(4f) * Math.signum(fraction) * eased);
        mPill.invalidateSelf();
        mLens.invalidateSelf();
    }

    private boolean pillRect(RectF out) {
        if (mSelected < 0 || Float.isNaN(mMotion.value()) || mCapsule.isEmpty()) return false;
        float v = mMotion.velocity() / 10f;
        float w = pillWidth() * mMotion.scaleX()
                / (1f - Math.max(-0.2f, Math.min(0.2f, v * 0.75f)));
        float h = (mCapsule.height() - 2f * dp(INNER_PADDING_DP)) * mMotion.scaleY()
                * (1f - Math.max(-0.2f, Math.min(0.2f, v * 0.25f)));
        float cx = pillCenterX();
        float cy = mCapsule.centerY();
        out.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
        return true;
    }

    private void restyleMaterialItems() {
        boolean darkSurface = isDarkSurface();
        int accent = darkSurface ? ACCENT_DARK : ACCENT_LIGHT;
        int label = darkSurface ? LABEL_DARK : LABEL_LIGHT;
        ColorStateList colors = new ColorStateList(
                new int[][] {{android.R.attr.state_checked}, {android.R.attr.state_selected}, {}},
                new int[] {accent, accent, label});
        invoke("setItemActiveIndicatorEnabled", boolean.class, false);
        invoke("setItemIconTintList", ColorStateList.class, colors);
        invoke("setItemTextColor", ColorStateList.class, colors);
        invoke("setItemRippleColor", ColorStateList.class, null);
    }

    private boolean isDarkSurface() {
        return isDark();
    }

    private boolean isDark() {
        if (mBgLuminance >= 0f) return mBgLuminance < 0.5f;
        if (mLabelLuminance >= 0f) return mLabelLuminance > 0.35f;
        return (mBar.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private static TextView findLabel(View v) {
        if (v instanceof TextView text && text.getVisibility() == View.VISIBLE
                && text.length() > 0) {
            return text;
        }
        if (v instanceof ViewGroup g) {
            for (int i = 0; i < g.getChildCount(); i++) {
                TextView found = findLabel(g.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }

    private void invoke(String name, Class<?> type, Object arg) {
        try {
            mBar.getClass().getMethod(name, type).invoke(mBar, arg);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private final class Pill extends Drawable {
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint mShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint mRim = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF mRect = new RectF();
        private android.graphics.Bitmap mShadowBitmap;
        private float mShadowW;
        private float mShadowH;
        private boolean mShadowDark;
        private float mRimTop = Float.NaN;
        private boolean mRimDark;
        private int mRimStyle = -1;

        private final Paint mInnerPillRim = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint mInnerGlow = new Paint(Paint.ANTI_ALIAS_FLAG);

        Pill() {
            mRim.setStyle(Paint.Style.STROKE);
            mInnerPillRim.setStyle(Paint.Style.STROKE);
        }

        private void drawShadow(Canvas canvas) {
            float pad = dp(8f) * 2f;
            if (mShadowBitmap == null || mShadowW != mCapsule.width()
                    || mShadowH != mCapsule.height() || mShadowDark != mDark) {
                mShadowW = mCapsule.width();
                mShadowH = mCapsule.height();
                mShadowDark = mDark;
                int bw = Math.max(1, Math.round(mShadowW + 2 * pad));
                int bh = Math.max(1, Math.round(mShadowH + 2 * pad));
                mShadowBitmap = android.graphics.Bitmap.createBitmap(bw, bh,
                        android.graphics.Bitmap.Config.ALPHA_8);
                Canvas c = new Canvas(mShadowBitmap);
                mShadow.setMaskFilter(new BlurMaskFilter(dp(8f), BlurMaskFilter.Blur.OUTER));
                mShadow.setColor(0xFF000000);
                float r = mShadowH / 2f;
                c.drawRoundRect(pad, pad, pad + mShadowW, pad + mShadowH, r, r, mShadow);
                mShadow.setMaskFilter(null);
            }
            int style = LiquidGlass.getEffectiveStyle(mBar.getContext());
            boolean darkCrystal = (style == LiquidGlass.STYLE_IOS27_DARK_CRYSTAL);
            mShadow.setColor(darkCrystal ? 0x80000000 : (mDark ? 0x55000000 : 0x26000000));
            canvas.drawBitmap(mShadowBitmap, mCapsule.left - pad, mCapsule.top - pad, mShadow);
        }

        private void drawCapsuleSurfaceAndRim(Canvas canvas) {
            int style = LiquidGlass.getEffectiveStyle(mBar.getContext());
            float r = mCapsule.height() / 2f;
            // Subtle optical glass sheen so backdrop refraction/blur remains vivid and never turns black
            if (style == LiquidGlass.STYLE_IOS27_DARK_CRYSTAL && mDark) {
                mPaint.setColor(0x28182030);
            } else if (style == LiquidGlass.STYLE_IOS_LIQUID_GLASS) {
                mPaint.setColor(mDark ? 0x22202430 : 0x30F0F4FA);
            } else {
                mPaint.setColor(mDark ? 0x38222630 : 0x48F2F4F8);
            }
            canvas.drawRoundRect(mCapsule, r, r, mPaint);

            // Specular 3D rim border around the floating capsule
            boolean isLens = LiquidGlass.isLiquidLensStyle(style);
            float stroke = dp(style == LiquidGlass.STYLE_IOS27_DARK_CRYSTAL ? 1.5f
                    : (isLens ? 1.35f : 1.0f));
            mRim.setStrokeWidth(stroke);
            if (mRimTop != mCapsule.top || mRimDark != mDark || mRimStyle != style) {
                mRimTop = mCapsule.top;
                mRimDark = mDark;
                mRimStyle = style;
                if (style == LiquidGlass.STYLE_IOS27_DARK_CRYSTAL && mDark) {
                    mRim.setShader(new android.graphics.LinearGradient(
                            mCapsule.left, mCapsule.top, mCapsule.right, mCapsule.bottom,
                            new int[] {
                                    0xDEFFFFFF,
                                    0x32FFFFFF,
                                    0x9970C4FF
                            },
                            new float[] {0f, 0.52f, 1f},
                            android.graphics.Shader.TileMode.CLAMP));
                } else if (style == LiquidGlass.STYLE_IOS_LIQUID_GLASS) {
                    mRim.setShader(new android.graphics.LinearGradient(
                            mCapsule.left, mCapsule.top, mCapsule.right, mCapsule.bottom,
                            new int[] {
                                     mDark ? 0x99FFFFFF : 0xCCFFFFFF,
                                     mDark ? 0x28FFFFFF : 0x44FFFFFF,
                                     mDark ? 0x6666B2FF : 0x660088FF
                            },
                            new float[] {0f, 0.55f, 1f},
                            android.graphics.Shader.TileMode.CLAMP));
                } else {
                    mRim.setShader(new android.graphics.LinearGradient(
                            0f, mCapsule.top, 0f, mCapsule.bottom,
                            mDark ? 0x48FFFFFF : 0x70FFFFFF,
                            mDark ? 0x18FFFFFF : 0x22000000,
                            android.graphics.Shader.TileMode.CLAMP));
                }
            }
            float half = stroke / 2f;
            canvas.drawRoundRect(
                    mCapsule.left + half, mCapsule.top + half,
                    mCapsule.right - half, mCapsule.bottom - half,
                    r - half, r - half, mRim);
        }

        @Override
        public void draw(Canvas canvas) {
            if (mCapsule.isEmpty()) return;
            drawShadow(canvas);
            drawCapsuleSurfaceAndRim(canvas);

            if (!pillRect(mRect)) return;
            int style = LiquidGlass.getEffectiveStyle(mBar.getContext());
            float press = mMotion.press();
            float h = mRect.height();
            float r = h / 2f;

            if (style == LiquidGlass.STYLE_IOS27_DARK_CRYSTAL && mDark) {
                // Luminous Smoked Crystal Active Lens Pill (translucent sapphire sheen, never opaque black)
                mPaint.setColor(0x38385078);
                canvas.drawRoundRect(mRect, r, r, mPaint);

                // Subtle top optical refraction sheen inside active pill
                mInnerGlow.setShader(new android.graphics.LinearGradient(
                        mRect.left, mRect.top, mRect.left, mRect.bottom,
                        new int[] {0x38FFFFFF, 0x0CFFFFFF, 0x244EA8FF},
                        new float[] {0f, 0.55f, 1f},
                        android.graphics.Shader.TileMode.CLAMP));
                canvas.drawRoundRect(mRect, r, r, mInnerGlow);

                // Crisp 3D inner lens Fresnel specular rim around active tab capsule
                float innerStroke = dp(1.15f);
                mInnerPillRim.setStrokeWidth(innerStroke);
                mInnerPillRim.setShader(new android.graphics.LinearGradient(
                        mRect.left, mRect.top, mRect.right, mRect.bottom,
                        new int[] {0xC8FFFFFF, 0x2EFFFFFF, 0x8866B8FF},
                        new float[] {0f, 0.5f, 1f},
                        android.graphics.Shader.TileMode.CLAMP));
                float half = innerStroke / 2f;
                canvas.drawRoundRect(
                        mRect.left + half, mRect.top + half,
                        mRect.right - half, mRect.bottom - half,
                        r - half, r - half, mInnerPillRim);
                return;
            }

            // AmeOS 16 Float Bar (Figma 4:1460) active pill gradient (#60A5FF -> #4781FF) + specular rim
            int alpha = Math.round(255 * (mDark ? 0.24f : 0.22f) * (1f - press * 0.25f));
            if (alpha > 0) {
                mInnerGlow.setShader(new android.graphics.LinearGradient(
                        mRect.left, mRect.top, mRect.left, mRect.bottom,
                        (alpha << 24) | 0x60A5FF,
                        (alpha << 24) | 0x4781FF,
                        android.graphics.Shader.TileMode.CLAMP));
                canvas.drawRoundRect(mRect, r, r, mInnerGlow);
            }
            float innerStroke = dp(0.9f);
            mInnerPillRim.setStrokeWidth(innerStroke);
            mInnerPillRim.setShader(new android.graphics.LinearGradient(
                    mRect.left, mRect.top, mRect.right, mRect.bottom,
                    new int[] {0xC2FFFFFF, 0x38FFFFFF, 0x8860A5FF},
                    new float[] {0f, 0.5f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            float half = innerStroke / 2f;
            canvas.drawRoundRect(
                    mRect.left + half, mRect.top + half,
                    mRect.right - half, mRect.bottom - half,
                    r - half, r - half, mInnerPillRim);
        }

        @Override
        public void setAlpha(int alpha) {}

        @Override
        public void setColorFilter(ColorFilter colorFilter) {}

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private final class Lens extends Drawable {
        private final RenderNode mNode = new RenderNode("LiquidTabBarLens");
        private final LiquidGlass mGlass = new LiquidGlass();
        private final Paint mFill = new Paint();
        private final RectF mRect = new RectF();

        @Override
        public void draw(Canvas canvas) {
            if (!LiquidGlass.isLiquidLensStyle(
                    LiquidGlass.getEffectiveStyle(mBar.getContext()))) {
                return;
            }
            float press = mMotion.press();
            if (press < 0.01f || !canvas.isHardwareAccelerated() || !pillRect(mRect)) return;
            int w = Math.max(1, Math.round(mRect.width()));
            int h = Math.max(1, Math.round(mRect.height()));
            int left = Math.round(mRect.left);
            int top = Math.round(mRect.top);
            mNode.setPosition(left, top, left + w, top + h);
            mGlass.setShape(0f, 0f, w, h, h / 2f)
                    .setLens(dp(10f) * press, dp(14f) * press, dp(1.5f) * press)
                    .setLook(0f, 1f, press, 0)
                    .setInnerShadow(dp(8f), 0.15f * press);
            mNode.setBackdropRenderEffect(mGlass.build());
            Canvas c = mNode.beginRecording(w, h);
            mFill.setColor(0x01000000);
            c.drawRect(0f, 0f, 1f, 1f, mFill);
            mNode.endRecording();
            canvas.drawRenderNode(mNode);
        }

        @Override
        public void setAlpha(int alpha) {}

        @Override
        public void setColorFilter(ColorFilter colorFilter) {}

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
