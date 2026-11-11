/*
 * Copyright (C) 2026 The PenguinOS Project
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.internal.penguin.glass;

import android.annotation.ColorInt;
import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.RuntimeShader;
import android.graphics.Shader;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;

/**
 * Real-time AGSL Liquid Glass & ColorOS Frosted Glass engine with
 * BlazeHouse 3-tier surface mode and Battery Saver auto-throttle.
 *
 * @hide
 */
public final class LiquidGlass {

    public static final String SETTING_GLASS_STYLE = "blaze_ui_glass_style";
    public static final String SETTING_AUTO_LITE_BATTERY_SAVER = "blaze_auto_lite_on_battery_saver";

    public static final int STYLE_IOS_LIQUID_GLASS = 0;
    public static final int STYLE_COLOROS_FROSTED = 1;
    public static final int STYLE_MATERIAL3_LITE = 2;
    public static final int STYLE_IOS27_DARK_CRYSTAL = 3;

    public static boolean isLiquidLensStyle(int style) {
        return style == STYLE_IOS_LIQUID_GLASS || style == STYLE_IOS27_DARK_CRYSTAL;
    }

    private static long sLastCheckMs;
    private static int sEffectiveStyle = STYLE_IOS27_DARK_CRYSTAL;
    private static volatile boolean sShaderFailed = false;

    public static int getEffectiveStyle(Context context) {
        if (sShaderFailed || android.os.SystemProperties.getBoolean(
                "persist.sys.blaze.safe_ui_fallback", false)) {
            return STYLE_COLOROS_FROSTED;
        }
        if (context == null) {
            context = android.app.ActivityThread.currentApplication();
        }
        if (context == null) return sEffectiveStyle;
        long now = SystemClock.uptimeMillis();
        if (now - sLastCheckMs < 800 && sLastCheckMs != 0) {
            return sEffectiveStyle;
        }
        sLastCheckMs = now;
        try {
            int style = Settings.Secure.getInt(
                    context.getContentResolver(), SETTING_GLASS_STYLE, STYLE_IOS27_DARK_CRYSTAL);
            boolean autoLite = Settings.Secure.getInt(
                    context.getContentResolver(), SETTING_AUTO_LITE_BATTERY_SAVER, 1) != 0;
            if (autoLite) {
                PowerManager pm = context.getSystemService(PowerManager.class);
                if (pm != null) {
                    if (pm.isPowerSaveMode()) {
                        sEffectiveStyle = STYLE_MATERIAL3_LITE;
                        return sEffectiveStyle;
                    }
                    int thermalStatus = pm.getCurrentThermalStatus();
                    if (thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE) {
                        sEffectiveStyle = STYLE_MATERIAL3_LITE;
                        return sEffectiveStyle;
                    }
                    if (thermalStatus >= PowerManager.THERMAL_STATUS_MODERATE
                            && isLiquidLensStyle(style)) {
                        sEffectiveStyle = STYLE_COLOROS_FROSTED;
                        return sEffectiveStyle;
                    }
                }
            }
            sEffectiveStyle = style;
        } catch (RuntimeException ignored) {
        }
        return sEffectiveStyle;
    }

    private static final String SHADER = ""
            + "uniform shader content;\n"
            + "uniform float4 shape;\n"
            + "uniform float radius;\n"
            + "uniform float refractionHeight;\n"
            + "uniform float refractionAmount;\n"
            + "uniform float dispersion;\n"
            + "uniform float saturation;\n"
            + "uniform float rim;\n"
            + "uniform float innerShadow;\n"
            + "uniform float innerShadowWidth;\n"
            + "layout(color) uniform half4 tint;\n"
            + "\n"
            + "float sdf(float2 p, float2 b, float r) {\n"
            + "  float2 q = abs(p) - b + r;\n"
            + "  return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;\n"
            + "}\n"
            + "\n"
            + "float2 sdfNormal(float2 p, float2 b, float r) {\n"
            + "  float2 w = abs(p) - (b - r);\n"
            + "  float2 s = float2(p.x < 0.0 ? -1.0 : 1.0, p.y < 0.0 ? -1.0 : 1.0);\n"
            + "  float2 q = max(w, 0.0);\n"
            + "  float l = length(q);\n"
            + "  if (max(w.x, w.y) > 0.0 && l > 0.0) return s * q / l;\n"
            + "  return s * (w.x > w.y ? float2(1.0, 0.0) : float2(0.0, 1.0));\n"
            + "}\n"
            + "\n"
            + "half4 main(float2 coord) {\n"
            + "  float2 center = (shape.xy + shape.zw) * 0.5;\n"
            + "  float2 halfSize = (shape.zw - shape.xy) * 0.5;\n"
            + "  float2 p = coord - center;\n"
            + "  float d = sdf(p, halfSize, radius);\n"
            + "  if (d > 1.0) return half4(0.0);\n"
            + "  float lensRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));\n"
            + "  float2 n = sdfNormal(p, halfSize, lensRadius);\n"
            + "  float depth = 1.0 - clamp(-d / refractionHeight, 0.0, 1.0);\n"
            + "  float dome = 1.0 - sqrt(1.0 - depth * depth);\n"
            + "  float2 sampleCoord = coord - n * dome * refractionAmount;\n"
            + "  half4 color;\n"
            + "  if (dispersion > 0.0) {\n"
            + "    float2 spread = n * dome * dispersion;\n"
            + "    half4 g = content.eval(sampleCoord);\n"
            + "    color = half4(content.eval(sampleCoord + spread).r, g.g,\n"
            + "        content.eval(sampleCoord - spread).b, g.a);\n"
            + "  } else {\n"
            + "    color = content.eval(sampleCoord);\n"
            + "  }\n"
            + "  half lum = dot(color.rgb, half3(0.2126, 0.7152, 0.0722));\n"
            + "  color.rgb = clamp(mix(half3(lum), color.rgb, saturation), 0.0, 1.0);\n"
            + "  color.rgb = mix(color.rgb, tint.rgb, tint.a);\n"
            + "  float edge = 1.0 - smoothstep(0.0, 1.5, -d);\n"
            + "  float light = abs(dot(n, float2(0.7071, -0.7071)));\n"
            + "  color.rgb += half3(edge * (0.35 + 0.65 * light) * rim);\n"
            + "  float inset = 1.0 - smoothstep(0.0, max(innerShadowWidth, 0.001), -d);\n"
            + "  color.rgb *= 1.0 - innerShadow * inset * inset;\n"
            + "  float alpha = 1.0 - smoothstep(-0.75, 0.75, d);\n"
            + "  return half4(clamp(color.rgb, 0.0, 1.0), 1.0) * alpha;\n"
            + "}\n";

    private RuntimeShader mShader;

    {
        try {
            if (!sShaderFailed) {
                mShader = new RuntimeShader(SHADER);
                mShader.setFloatUniform("innerShadow", 0f);
                mShader.setFloatUniform("innerShadowWidth", 1f);
            }
        } catch (Throwable t) {
            sShaderFailed = true;
            mShader = null;
        }
    }

    public LiquidGlass setInnerShadow(float width, float strength) {
        if (mShader != null) {
            try {
                mShader.setFloatUniform("innerShadowWidth", Math.max(width, 0.001f));
                mShader.setFloatUniform("innerShadow", strength);
            } catch (Throwable t) {
                sShaderFailed = true;
            }
        }
        return this;
    }

    private float mBlurRadius;

    public static final @ColorInt int TINT_LIGHT = 0x78F2F2F7;
    public static final @ColorInt int TINT_DARK = 0x701C1C1E;
    public static final @ColorInt int TINT_DARK_CRYSTAL = 0x7810141E;

    public LiquidGlass setShape(float left, float top, float right, float bottom, float radius) {
        if (mShader != null) {
            try {
                mShader.setFloatUniform("shape", left, top, right, bottom);
                mShader.setFloatUniform("radius", radius);
            } catch (Throwable t) {
                sShaderFailed = true;
            }
        }
        return this;
    }

    public LiquidGlass setLens(float height, float amount, float dispersion) {
        if (mShader != null) {
            try {
                boolean frostedOnly = (sEffectiveStyle == STYLE_COLOROS_FROSTED);
                mShader.setFloatUniform("refractionHeight", Math.max(height, 1f));
                mShader.setFloatUniform("refractionAmount", frostedOnly ? 0f : amount);
                mShader.setFloatUniform("dispersion", frostedOnly ? 0f : dispersion);
            } catch (Throwable t) {
                sShaderFailed = true;
            }
        }
        return this;
    }

    public LiquidGlass setLook(float blurRadius, float saturation, float rim, @ColorInt int tint) {
        mBlurRadius = blurRadius;
        if (mShader != null) {
            try {
                boolean frostedOnly = (sEffectiveStyle == STYLE_COLOROS_FROSTED);
                mShader.setFloatUniform("saturation", frostedOnly ? 1.2f : saturation);
                mShader.setFloatUniform("rim", frostedOnly ? rim * 0.45f : rim);
                mShader.setColorUniform("tint", tint);
            } catch (Throwable t) {
                sShaderFailed = true;
            }
        }
        return this;
    }

    public RenderEffect build() {
        try {
            if (mShader == null || sShaderFailed) {
                if (mBlurRadius > 0f) {
                    return RenderEffect.createBlurEffect(
                            mBlurRadius, mBlurRadius, Shader.TileMode.CLAMP);
                }
                return null;
            }
            RenderEffect lens = RenderEffect.createRuntimeShaderEffect(mShader, "content");
            if (mBlurRadius > 0f) {
                lens = RenderEffect.createChainEffect(lens,
                        RenderEffect.createBlurEffect(
                                mBlurRadius, mBlurRadius, Shader.TileMode.CLAMP));
            }
            return lens;
        } catch (Throwable t) {
            sShaderFailed = true;
            return null;
        }
    }

    public void applyTo(View view) {
        if (view == null) return;
        try {
            if (getEffectiveStyle(view.getContext()) == STYLE_MATERIAL3_LITE) {
                view.setBackdropRenderEffect(null);
                return;
            }
            view.setBackdropRenderEffect(build());
        } catch (Throwable t) {
            sShaderFailed = true;
            try {
                view.setBackdropRenderEffect(null);
            } catch (Throwable ignored) {
            }
        }
    }

    public static void removeFrom(View view) {
        if (view == null) return;
        try {
            view.setBackdropRenderEffect(null);
        } catch (Throwable ignored) {
        }
    }
}
