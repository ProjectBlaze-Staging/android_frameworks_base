/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.systemui.scrim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.statusbar.notification.stack.StackStateAnimator;

/**
 * Drawable used on SysUI scrims.
 */
public class ScrimDrawable extends Drawable {
    private static final String TAG = "ScrimDrawable";

    private static final int GRADIENT_LAYER_COUNT = 3;
    private static final float[] GRADIENT_POSITIONS = {0f, 0.5f, 1.0f};
    private static final float GRADIENT_RADIUS_MULTIPLIER = 1.5f;
    private static final float COLOR_BLEND_INTENSITY = 0.15f;

    private boolean mShouldUseLargeScreenSize;
    private final Paint mPaint;
    private final Paint mGradientPaint;
    private final Path mPath = new Path();
    private final RectF mBoundsRectF = new RectF();
    private final int[] mGradientColors = new int[GRADIENT_LAYER_COUNT];

    private int mAlpha = 255;
    private int mMainColor;
    private ValueAnimator mColorAnimation;
    private int mMainColorTo;
    private float mCornerRadius;
    private ConcaveInfo mConcaveInfo;
    private int mBottomEdgePosition;
    private float mBottomEdgeRadius = -1;
    private boolean mCornerRadiusEnabled;

    private int mAccentColor = Color.TRANSPARENT;
    private int mBackgroundTintColor = Color.TRANSPARENT;
    private float mGlassEffectIntensity = 0f;
    private Shader mGlassmorphismShader;
    // Shader creation allocates a native object; rebuild lazily in draw() only
    // when an input (color, accent, tint, intensity, bounds) has changed.
    private boolean mGlassShaderDirty = true;

    public ScrimDrawable() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);

        mGradientPaint = new Paint();
        mGradientPaint.setStyle(Paint.Style.FILL);
        mGradientPaint.setAntiAlias(true);

        mShouldUseLargeScreenSize = false;
    }

    /**
     * Sets the background color with OneUI glassmorphism enhancement.
     */
    public void setColor(int mainColor, boolean animated) {
        if (mainColor == mMainColorTo) {
            return;
        }

        if (mColorAnimation != null && mColorAnimation.isRunning()) {
            mColorAnimation.cancel();
        }

        mMainColorTo = mainColor;

        if (animated) {
            final int mainFrom = mMainColor;

            ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
            anim.setDuration(StackStateAnimator.ANIMATION_DURATION_STANDARD);
            anim.addUpdateListener(animation -> {
                float ratio = (float) animation.getAnimatedValue();
                mMainColor = ColorUtils.blendARGB(mainFrom, mainColor, ratio);
                mGlassShaderDirty = true;
                invalidateSelf();
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation, boolean isReverse) {
                    if (mColorAnimation == animation) {
                        mColorAnimation = null;
                    }
                }
            });
            anim.setInterpolator(new DecelerateInterpolator());
            anim.start();
            mColorAnimation = anim;
        } else {
            mMainColor = mainColor;
            mGlassShaderDirty = true;
            invalidateSelf();
        }
    }

    /**
     * Set accent color for glassmorphism effect
     */
    public void setAccentColor(int accentColor) {
        if (mAccentColor != accentColor) {
            mAccentColor = accentColor;
            mGlassShaderDirty = true;
            invalidateSelf();
        }
    }

    /**
     * Set background tint for color sampling effect
     */
    public void setBackgroundTint(int tintColor, float intensity) {
        if (mBackgroundTintColor != tintColor || mGlassEffectIntensity != intensity) {
            mBackgroundTintColor = tintColor;
            mGlassEffectIntensity = intensity;
            mGlassShaderDirty = true;
            invalidateSelf();
        }
    }

    /**
     * Creates OneUI-style glassmorphism shader with color sampling
     */
    private void updateGlassmorphismShader() {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            return;
        }
        mGlassShaderDirty = false;

        float width = bounds.width();
        float height = bounds.height();
        float centerX = width / 2f;
        float centerY = height / 2f;

        int baseAlpha = Color.alpha(mMainColor);
        int baseRed = Color.red(mMainColor);
        int baseGreen = Color.green(mMainColor);
        int baseBlue = Color.blue(mMainColor);

        int[] colors = mGradientColors;
        float[] positions = GRADIENT_POSITIONS;

        if (mAccentColor != Color.TRANSPARENT && mGlassEffectIntensity > 0) {
            int accentRed = Color.red(mAccentColor);
            int accentGreen = Color.green(mAccentColor);
            int accentBlue = Color.blue(mAccentColor);

            for (int i = 0; i < GRADIENT_LAYER_COUNT; i++) {
                float blendRatio = mGlassEffectIntensity * COLOR_BLEND_INTENSITY * (1f - positions[i]);
                int r = (int) (baseRed * (1 - blendRatio) + accentRed * blendRatio);
                int g = (int) (baseGreen * (1 - blendRatio) + accentGreen * blendRatio);
                int b = (int) (baseBlue * (1 - blendRatio) + accentBlue * blendRatio);
                int a = Math.max(0, Math.min(255, (int) (baseAlpha * (1f - positions[i] * 0.3f)))); // Fade at edges, clamped

                colors[i] = Color.argb(a, r, g, b);
            }

            float radius = (float) Math.sqrt(width * width + height * height) / 2f * GRADIENT_RADIUS_MULTIPLIER;
            mGlassmorphismShader = new RadialGradient(
                centerX, centerY, radius,
                colors, positions,
                Shader.TileMode.CLAMP
            );
        } else {
            colors[0] = ColorUtils.setAlphaComponent(mMainColor, Math.min(255, (int) (baseAlpha * 1.1f)));
            colors[1] = mMainColor;
            colors[2] = ColorUtils.setAlphaComponent(mMainColor, Math.max(0, (int) (baseAlpha * 0.85f)));


            mGlassmorphismShader = new LinearGradient(
                0, 0, 0, height,
                colors, positions,
                Shader.TileMode.CLAMP
            );
        }

        mGradientPaint.setShader(mGlassmorphismShader);
    }

    @Override
    public void setAlpha(int alpha) {
        if (alpha != mAlpha) {
            mAlpha = alpha;
            invalidateSelf();
        }
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setXfermode(@Nullable Xfermode mode) {
        mPaint.setXfermode(mode);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public ColorFilter getColorFilter() {
        return mPaint.getColorFilter();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setShouldUseLargeScreenSize(boolean v) {
        mShouldUseLargeScreenSize = v;
    }

    public void setRoundedCorners(float radius) {
        if (radius == mCornerRadius) {
            return;
        }
        mCornerRadius = radius;
        if (mConcaveInfo != null) {
            mConcaveInfo.setCornerRadius(radius);
            updatePath();
        }
        invalidateSelf();
    }

    public void setRoundedCornersEnabled(boolean enabled) {
        if (mCornerRadiusEnabled == enabled) {
            return;
        }
        mCornerRadiusEnabled = enabled;
        invalidateSelf();
    }

    public void setBottomEdgeConcave(boolean enabled) {
        if (enabled && mConcaveInfo != null) {
            return;
        }
        if (!enabled) {
            mConcaveInfo = null;
        } else {
            mConcaveInfo = new ConcaveInfo();
            mConcaveInfo.setCornerRadius(mCornerRadius);
        }
        invalidateSelf();
    }

    public void setBottomEdgePosition(int y) {
        if (mBottomEdgePosition == y) {
            return;
        }
        mBottomEdgePosition = y;
        if (mConcaveInfo == null) {
            return;
        }
        updatePath();
        invalidateSelf();
    }

    public void setBottomEdgeRadius(float radius) {
        if (mBottomEdgeRadius != radius) {
            mBottomEdgeRadius = radius;
            invalidateSelf();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mGlassEffectIntensity > 0) {
            if (mGlassShaderDirty) {
                updateGlassmorphismShader();
            }
            if (mGlassmorphismShader != null) {
                mGradientPaint.setAlpha((int) (mAlpha * mGlassEffectIntensity));
                drawShape(canvas, mGradientPaint);
            }
        }

        mPaint.setColor(mMainColor);
        mPaint.setAlpha(mAlpha);
        drawShape(canvas, mPaint);
    }

    private void drawShape(Canvas canvas, Paint paint) {
        if (mConcaveInfo != null) {
            canvas.save();
            canvas.clipOutPath(mConcaveInfo.mPath);
            canvas.drawRect(getBounds().left, getBounds().top, getBounds().right,
                    mBottomEdgePosition + mConcaveInfo.mPathOverlap, paint);
            canvas.restore();
        } else if (mCornerRadiusEnabled && mCornerRadius > 0) {
            drawRoundedRect(canvas, paint);
        } else {
            canvas.drawRect(getBounds().left, getBounds().top, getBounds().right,
                    getBounds().bottom, paint);
        }
    }

    private void drawRoundedRect(Canvas canvas, Paint paint) {
        float topEdgeRadius = mCornerRadius;
        float bottomEdgeRadius = mBottomEdgeRadius == -1.0 ? mCornerRadius : mBottomEdgeRadius;

        mBoundsRectF.set(getBounds());

        if (!mShouldUseLargeScreenSize && mBottomEdgeRadius != -1) {
            mBoundsRectF.bottom -= bottomEdgeRadius;
        }

        if (mBoundsRectF.bottom - mBoundsRectF.top > bottomEdgeRadius) {
            mPath.reset();
            mPath.moveTo(mBoundsRectF.right, mBoundsRectF.top + topEdgeRadius);
            mPath.cubicTo(mBoundsRectF.right, mBoundsRectF.top + topEdgeRadius,
                    mBoundsRectF.right, mBoundsRectF.top,
                    mBoundsRectF.right - topEdgeRadius, mBoundsRectF.top);
            mPath.lineTo(mBoundsRectF.left + topEdgeRadius, mBoundsRectF.top);
            mPath.cubicTo(mBoundsRectF.left + topEdgeRadius, mBoundsRectF.top,
                    mBoundsRectF.left, mBoundsRectF.top,
                    mBoundsRectF.left, mBoundsRectF.top + topEdgeRadius);
            mPath.lineTo(mBoundsRectF.left, mBoundsRectF.bottom - bottomEdgeRadius);
            mPath.cubicTo(mBoundsRectF.left, mBoundsRectF.bottom - bottomEdgeRadius,
                    mBoundsRectF.left, mBoundsRectF.bottom,
                    mBoundsRectF.left + bottomEdgeRadius, mBoundsRectF.bottom);
            mPath.lineTo(mBoundsRectF.right - bottomEdgeRadius, mBoundsRectF.bottom);
            mPath.cubicTo(mBoundsRectF.right - bottomEdgeRadius, mBoundsRectF.bottom,
                    mBoundsRectF.right, mBoundsRectF.bottom,
                    mBoundsRectF.right, mBoundsRectF.bottom - bottomEdgeRadius);
            mPath.close();
            canvas.drawPath(mPath, paint);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        updatePath();
        mGlassShaderDirty = true;
    }

    private void updatePath() {
        if (mConcaveInfo == null) {
            return;
        }
        mConcaveInfo.mPath.reset();
        float top = mBottomEdgePosition;
        float bottom = mBottomEdgePosition + mConcaveInfo.mPathOverlap;
        mConcaveInfo.mPath.addRoundRect(getBounds().left, top, getBounds().right, bottom,
                mConcaveInfo.mCornerRadii, Path.Direction.CW);
    }

    @VisibleForTesting
    public int getMainColor() {
        return mMainColor;
    }

    private static class ConcaveInfo {
        private float mPathOverlap;
        private final float[] mCornerRadii;
        private final Path mPath = new Path();

        ConcaveInfo() {
            mCornerRadii = new float[] {0, 0, 0, 0, 0, 0, 0, 0};
        }

        public void setCornerRadius(float radius) {
            mPathOverlap = radius;
            mCornerRadii[0] = radius;
            mCornerRadii[1] = radius;
            mCornerRadii[2] = radius;
            mCornerRadii[3] = radius;
        }
    }
}
