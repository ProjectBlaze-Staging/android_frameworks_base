package com.android.systemui.qs.colorcc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
final class Logic {
    static final int BLUETOOTH = 2;
    static final float CASCADE_S = 0.25f;
    static final String COMPONENTS = "wbmlvsd";
    static final int DATA_GREEN = -14700715;
    private static final float DROP_MARGIN = 0.3f;
    private static final float FULLY_OPEN = 0.99f;
    static final int FX_BOB = 9;
    static final int FX_DROP = 4;
    static final int FX_FILL = 11;
    static final int FX_FLICKER = 1;
    static final int FX_FLIP = 8;
    static final int FX_FLY = 2;
    static final int FX_GROW = 7;
    static final int FX_JUMP = 14;
    static final int FX_POP = 0;
    static final int FX_PULSE = 6;
    static final int FX_SHAKE = 13;
    static final int FX_SHUTTER = 12;
    static final int FX_SPIN = 3;
    static final int FX_SWING = 5;
    static final int FX_TURN = 10;
    private static final float MAX_PACE = 1.5f;
    static final int MENU_BLUETOOTH = 2;
    static final int MENU_DATA = 3;
    static final int MENU_DND = 5;
    static final int MENU_HOTSPOT = 4;
    static final int MENU_NONE = 0;
    static final int MENU_OUTPUT = 7;
    static final int MENU_TORCH = 6;
    static final int MENU_WIFI = 1;
    static final String[] MENU_WORDS = {"", "wifi", "bluetooth", "mobile_data", "hotspot", "dnd", "torch", "media_output"};
    static final int MOBILE_DATA = 3;
    static final int OTHER = 0;
    static final int OWNER_PAGER = 1;
    static final int OWNER_SHADE = 2;
    static final int OWNER_UNDECIDED = 0;
    static final char PIN = 'p';
    private static final int STAGGER_MAX_ORDER = 21;
    private static final float STAGGER_SPAN = 0.16f;
    private static final float STAGGER_STEP = 0.038f;
    static final char TILES = 'g';
    private static final float TONE_PEAK = 140.0f;
    private static final float TURN = 0.03f;
    static final int WIFI = 1;

    static boolean closing(boolean z, float f, float f2) {
        if (z) {
            return f < 1.0f && f <= f2 + TURN;
        }
        return f < f2 - TURN || (f <= 0.0f && f2 > 0.0f);
    }

    static boolean controlsPage(int r1, int r2, boolean z) {
        if (r1 == 2 && (r2 == 0 || r2 == 1)) {
            return r2 == 0;
        }
        return z;
    }

    private static float cube(float f) {
        return f * f * f;
    }

    static boolean onControlPage(boolean z, boolean z2) {
        return !z || z2;
    }

    static float rubber(float f, float f2) {
        if (f <= 0.0f) {
            return 0.0f;
        }
        return ((f * f2) * 0.75f) / (f2 + (f * 0.75f));
    }

    static int spanW(char c) {
        if (c == 'B' || c == 'D') {
            return 1;
        }
        if (c == 'M' || c == 'S') {
            return 4;
        }
        if (c == 'W') {
            return 1;
        }
        if (c != 'g') {
            return (c == 'l' || c == 'v') ? 1 : 2;
        }
        return 4;
    }

    private static float sq(float f) {
        return f * f;
    }

    static boolean takesTouches(float f, float f2) {
        return f >= FULLY_OPEN && f2 >= FULLY_OPEN;
    }

    static int thumb(int r2) {
        if (r2 == 2) {
            return 0;
        }
        return r2 == 1 ? 1 : 2;
    }

    static int menu(String str) {
        int r0 = kind(str);
        if (r0 == 1) {
            return 1;
        }
        if (r0 == 2) {
            return 2;
        }
        if (r0 == 3) {
            return 3;
        }
        String lowerCase = str.toLowerCase(Locale.ROOT);
        if (lowerCase.startsWith("custom(")) {
            return 0;
        }
        if (lowerCase.contains("hotspot")) {
            return 4;
        }
        if (lowerCase.equals("dnd") || lowerCase.contains("donotdisturb")) {
            return 5;
        }
        return (lowerCase.contains("flash") || lowerCase.equals("torch")) ? 6 : 0;
    }

    static int kind(String str) {
        if ("wifi".equalsIgnoreCase(str) || "internet".equalsIgnoreCase(str)) {
            return 1;
        }
        if ("bt".equalsIgnoreCase(str) || "Bluetooth".equalsIgnoreCase(str)) {
            return 2;
        }
        return ("cell".equalsIgnoreCase(str) || "cellular".equalsIgnoreCase(str) || "mobile_data".equalsIgnoreCase(str) || "MobileData".equalsIgnoreCase(str)) ? 3 : 0;
    }

    static int[] alphaBounds(byte[] bArr, int r10, int r11, int r12) {
        int r0 = -1;
        int r4 = r11;
        int r5 = r12;
        int r2 = -1;
        for (int r3 = 0; r3 < r12; r3++) {
            for (int r6 = 0; r6 < r11; r6++) {
                if ((bArr[(r3 * r10) + r6] & 255) > 16) {
                    r4 = Math.min(r4, r6);
                    r5 = Math.min(r5, r3);
                    r0 = Math.max(r0, r6);
                    r2 = Math.max(r2, r3);
                }
            }
        }
        if (r0 < 0) {
            return null;
        }
        return new int[]{r4, r5, r0 + 1, r2 + 1};
    }

    static int pages(int r1, int r2) {
        if (r1 <= 0) {
            return 1;
        }
        return ((r1 + r2) - 1) / r2;
    }

    static int swipeOwner(float f, float f2, float f3, boolean z, boolean z2) {
        if (Math.abs(f) <= f3 && Math.abs(f2) <= f3) {
            return 0;
        }
        if (Math.abs(f2) >= Math.abs(f)) {
            return 2;
        }
        if (f < 0.0f) {
            if (z2) {
                return 1;
            }
        } else if (z) {
            return 1;
        }
        return 2;
    }

    static int pageAfterFling(int r2, int r3, float f, int r5) {
        double d = ((double) r2) / ((double) r3);
        return Math.max(0, Math.min(r5 - 1, (int) (f > 0.0f ? Math.ceil(d) : Math.floor(d))));
    }

    static float clamp(float f) {
        if (f < 0.0f) {
            return 0.0f;
        }
        return Math.min(f, 1.0f);
    }

    static float toLinear(float f, float f2, float f3) {
        return f2 + (clamp(f) * (f3 - f2));
    }

    static float toSlider(float f, float f2, float f3) {
        if (f3 <= f2) {
            return 0.0f;
        }
        return clamp((f - f2) / (f3 - f2));
    }

    static float revealTarget(float f, boolean z) {
        if (z) {
            return clamp(f);
        }
        return 0.0f;
    }

    static float elementTarget(float f, int r2) {
        return clamp((f - (Math.min(r2, STAGGER_MAX_ORDER) * STAGGER_STEP)) / STAGGER_SPAN);
    }

    static float openPace(float f) {
        return (clamp((f - 2.0f) / 6.0f) * 0.5f) + 1.0f;
    }

    static float flick(float f) {
        return clamp((f - 1.0f) / 0.5f);
    }

    static float openingSequence(float f) {
        return Math.min(f * MAX_PACE, 1.0f);
    }

    static float cascade(float f, float f2, float f3, float f4) {
        return Math.min(f2, f + ((Math.max(0.0f, Math.min(f3, 0.033333335f)) * f4) / CASCADE_S));
    }

    static float springAt(float[] fArr, float f) {
        float fSqrt = (float) Math.sqrt(fArr[0]);
        float f2 = fArr[1];
        float fSqrt2 = ((float) Math.sqrt(1.0f - (f2 * f2))) * fSqrt;
        double d = f * fSqrt2;
        return 1.0f - (((float) Math.exp(((-f2) * fSqrt) * f)) * (((float) Math.cos(d)) + (((f2 * fSqrt) / fSqrt2) * ((float) Math.sin(d)))));
    }

    static float springSpeed(float[] fArr, float f) {
        float fSqrt = (float) Math.sqrt(fArr[0]);
        float f2 = fArr[1];
        float fSqrt2 = ((float) Math.sqrt(1.0f - (f2 * f2))) * fSqrt;
        return ((((float) Math.exp(((-f2) * fSqrt) * f)) * fArr[0]) / fSqrt2) * ((float) Math.sin(fSqrt2 * f));
    }

    static float springPeak(float[] fArr) {
        float fSqrt = (float) Math.sqrt(fArr[0]);
        float f = fArr[1];
        return 3.1415927f / (fSqrt * ((float) Math.sqrt(1.0f - (f * f))));
    }

    static float springTimeOf(float[] fArr, float f) {
        float fSpringPeak = springPeak(fArr);
        float f2 = 0.0f;
        for (int r2 = 0; r2 < 30; r2++) {
            float f3 = (f2 + fSpringPeak) / 2.0f;
            if (springAt(fArr, f3) < f) {
                f2 = f3;
            } else {
                fSpringPeak = f3;
            }
        }
        return (f2 + fSpringPeak) / 2.0f;
    }

    static float closingSequence(float f) {
        return clamp((f - CASCADE_S) / 0.72f);
    }

    static LinkedHashMap<String, int[]> pins(String str) {
        LinkedHashMap<String, int[]> linkedHashMap = new LinkedHashMap<>();
        if (str != null) {
            for (String str2 : str.split("\n")) {
                String[] strArrSplit = str2.split("\t");
                if ((strArrSplit.length == 3 || strArrSplit.length == 5) && !strArrSplit[0].isEmpty()) {
                    try {
                        int r8 = Integer.parseInt(strArrSplit[1]);
                        int r9 = Integer.parseInt(strArrSplit[2]);
                        int r6 = strArrSplit.length == 5 ? Integer.parseInt(strArrSplit[3]) : 1;
                        int r7 = strArrSplit.length == 5 ? Integer.parseInt(strArrSplit[4]) : 1;
                        int[] r10 = fit(PIN, r6, r7);
                        if (r8 >= 0 && r8 + r6 <= 4 && r9 >= 0 && r10[0] == r6 && r10[1] == r7) {
                            linkedHashMap.put(strArrSplit[0], (r6 == 1 && r7 == 1) ? new int[]{r8, r9} : new int[]{r8, r9, r6, r7});
                        }
                    } catch (NumberFormatException unused) {
                    }
                }
            }
        }
        return linkedHashMap;
    }

    static String encodePins(Map<String, int[]> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, int[]> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(entry.getKey());
            sb.append('\t');
            sb.append(entry.getValue()[0]);
            sb.append('\t');
            sb.append(entry.getValue()[1]);
            int[] r1 = pinSize(entry.getValue());
            if (r1[0] != 1 || r1[1] != 1) {
                sb.append('\t');
                sb.append(r1[0]);
                sb.append('\t');
                sb.append(r1[1]);
            }
        }
        return sb.toString();
    }

    static float[][] tunes(String str, int r9, int r10) {
        float[][] fArr = (float[][]) Array.newInstance((Class<?>) Float.TYPE, r9, r10);
        for (float[] fArr2 : fArr) {
            Arrays.fill(fArr2, 1.0f);
        }
        if (str != null) {
            String[] strArrSplit = str.split(",");
            if (strArrSplit.length == r9 * r10) {
                float[][] fArr3 = (float[][]) Array.newInstance((Class<?>) Float.TYPE, r9, r10);
                for (int r3 = 0; r3 < strArrSplit.length; r3++) {
                    try {
                        float f = Float.parseFloat(strArrSplit[r3]);
                        if (!Float.isNaN(f) && !Float.isInfinite(f)) {
                            fArr3[r3 / r10][r3 % r10] = f;
                        }
                    } catch (NumberFormatException unused) {
                    }
                }
                return fArr3;
            }
        }
        return fArr;
    }

    static String encodeTunes(float[][] fArr) {
        StringBuilder sb = new StringBuilder();
        for (float[] fArr2 : fArr) {
            for (float f : fArr2) {
                sb.append(sb.length() == 0 ? "" : ",");
                sb.append(f);
            }
        }
        return sb.toString();
    }

    static int[] pinSize(int[] r5) {
        return (r5 == null || r5.length < 4) ? new int[]{1, 1} : new int[]{r5[2], r5[3]};
    }

    static String[] withTiles(String str, String str2) {
        if (str.indexOf(103) >= 0) {
            return new String[]{str, str2};
        }
        int[][] r1 = sizes(str2, str);
        int[][] r8 = spots(str2, str, 4);
        int[][] r2 = (int[][]) Arrays.copyOf(r8, r8.length + 1);
        r2[r8.length] = new int[]{0, rows(r8, r1[1])};
        int[][] r3 = {Arrays.copyOf(r1[0], r8.length + 1), Arrays.copyOf(r1[1], r8.length + 1)};
        r3[0][r8.length] = spanW(TILES);
        r3[1][r8.length] = spanH(TILES);
        return new String[]{str + TILES, encode(r2, r3)};
    }

    static String layout(String str) {
        if (str == null) {
            return COMPONENTS;
        }
        if (str.indexOf(116) >= 0) {
            StringBuilder sb = new StringBuilder();
            for (char c : str.toCharArray()) {
                int r5 = "cCmMrRt".indexOf(c);
                if (r5 < 0) {
                    return COMPONENTS;
                }
                sb.append(new String[]{"wb", "bw", "mlv", "lvm", "sd", "ds", ""}[r5]);
            }
            str = sb.toString();
        }
        StringBuilder sb2 = new StringBuilder();
        StringBuilder sb3 = new StringBuilder();
        for (char c2 : str.toCharArray()) {
            char lowerCase = Character.toLowerCase(c2);
            if ((COMPONENTS.indexOf(lowerCase) < 0 && lowerCase != 'g') || sb3.indexOf(String.valueOf(lowerCase)) >= 0) {
                return COMPONENTS;
            }
            sb3.append(lowerCase);
            sb2.append(c2);
        }
        return sb2.toString();
    }

    static int[] nextSize(char c, int r4, int r5) {
        int[] r0 = limits(c);
        if (r4 < r0[1]) {
            return fit(c, r4 + 1, r5);
        }
        return fit(c, r0[0], r5 < r0[3] ? r5 + 1 : r0[2]);
    }

    static int spanH(char c) {
        char lowerCase = Character.toLowerCase(c);
        if (lowerCase == 'g') {
            return 4;
        }
        return (lowerCase == 'm' || lowerCase == 'l' || lowerCase == 'v') ? 2 : 1;
    }

    static int[][] place(int[] r16, int[] r17, int r18) {
        int[][] r2 = new int[r16.length][];
        ArrayList arrayList = new ArrayList();
        for (int r5 = 0; r5 < r16.length; r5++) {
            int r6 = Math.max(1, Math.min(r16[r5], r18));
            int r8 = Math.max(1, r17[r5]);
            int r9 = r6 != 2 ? 1 : 2;
            int r10 = 0;
            while (r2[r5] == null) {
                int r11 = 0;
                while (true) {
                    int r12 = r11 + r6;
                    if (r12 > r18 || r2[r5] != null) {
                        break;
                    }
                    if (free(arrayList, r11, r10, r6, r8)) {
                        for (int r13 = r10; r13 < r10 + r8; r13++) {
                            while (arrayList.size() <= r13) {
                                arrayList.add(new boolean[r18]);
                            }
                            for (int r14 = r11; r14 < r12; r14++) {
                                ((boolean[]) arrayList.get(r13))[r14] = true;
                            }
                        }
                        r2[r5] = new int[]{r11, r10};
                    }
                    r11 += r9;
                }
                r10++;
            }
        }
        return r2;
    }

    private static boolean free(List<boolean[]> list, int r4, int r5, int r6, int r7) {
        for (int r0 = r5; r0 < r5 + r7 && r0 < list.size(); r0++) {
            for (int r1 = r4; r1 < r4 + r6; r1++) {
                if (list.get(r0)[r1]) {
                    return false;
                }
            }
        }
        return true;
    }

    static int dropIndex(int r8, int r9, float f, float f2, int r12) {
        float f3 = 0.0f;
        float f4 = Float.MAX_VALUE;
        int r3 = r9;
        for (int r2 = 0; r2 < r8; r2++) {
            float fHypot = (float) Math.hypot(((r2 % r12) + 0.5f) - f, ((r2 / r12) + 0.5f) - f2);
            if (r2 == r9) {
                f3 = fHypot;
            }
            if (fHypot < f4) {
                r3 = r2;
                f4 = fHypot;
            }
        }
        return f3 - f4 > DROP_MARGIN ? r3 : r9;
    }

    static int snap(float f, int r3, int r4, int r5) {
        float f2 = r5;
        return Math.abs(f - ((float) r3)) > (f2 / 2.0f) + DROP_MARGIN ? Math.max(0, Math.min(r4, Math.round(f / f2) * r5)) : r3;
    }

    static int[][] shove(int[] r7, int[] r8, int[][] r9, int r10, int r11, int r12, int r13) {
        int r6;
        int[] r1;
        int[] r2;
        int[][] r3 = new int[r9.length][];
        r3[r10] = new int[]{r11, r12};
        int r4 = 0;
        while (r4 < r9.length) {
            if (r4 != r10) {
                int[] r14 = r9[r4];
                r1 = r7;
                r2 = r8;
                if (clear(r1, r2, r3, r4, r14[0], r14[1])) {
                    r3[r4] = r9[r4];
                }
            } else {
                r1 = r7;
                r2 = r8;
            }
            r4++;
            r7 = r1;
            r8 = r2;
        }
        int[] r5 = r7;
        int[] r15 = r8;
        int r16 = 0;
        while (r16 < r9.length) {
            if (r3[r16] == null) {
                r6 = r13;
                r3[r16] = nearest(r5, r15, r3, r16, r9[r16], r6);
            } else {
                r6 = r13;
            }
            r16++;
            r13 = r6;
        }
        return r3;
    }

    static int[] nearest(int[] r17, int[] r18, int[][] r19, int r20, int[] r21, int r22) {
        int r5;
        int[][] r2 = r19;
        int r3 = r20;
        int r6 = 0;
        int r7 = 0;
        for (int r0 = 0; r0 < r2.length; r0++) {
            int[] r1 = r2[r0];
            if (r1 != null && r0 != r3) {
                r7 = Math.max(r7, r1[1] + r18[r0]);
            }
        }
        int[] r4 = null;
        double d = Double.MAX_VALUE;
        int r8 = 0;
        while (r8 <= r7) {
            int[] r9 = r4;
            double d2 = d;
            int r10 = r6;
            while (r17[r3] + r10 <= r22) {
                int r15 = r6;
                int r16 = r7;
                double dHypot = Math.hypot(r10 - r21[r6], r8 - r21[1]);
                int r13 = 2;
                if (dHypot < d2) {
                    r5 = r8;
                    if (clear(r17, r18, r2, r3, r10, r5)) {
                        r9 = new int[2];
                        r9[r15] = r10;
                        r9[1] = r5;
                        d2 = dHypot;
                    }
                } else {
                    r5 = r8;
                }
                if (r17[r20] != 2) {
                    r13 = 1;
                }
                r10 += r13;
                r2 = r19;
                r3 = r20;
                r8 = r5;
                r6 = r15;
                r7 = r16;
            }
            r8++;
            r2 = r19;
            r3 = r20;
            r4 = r9;
            d = d2;
        }
        return r4;
    }

    private static boolean clear(int[] r6, int[] r7, int[][] r8, int r9, int r10, int r11) {
        int[] r2;
        for (int r1 = 0; r1 < r8.length; r1++) {
            if (r1 != r9 && (r2 = r8[r1]) != null) {
                int r4 = r2[0];
                if (r10 < r6[r1] + r4 && r4 < r6[r9] + r10) {
                    int r3 = r2[1];
                    if (r11 < r7[r1] + r3 && r3 < r7[r9] + r11) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static int[][] liftWith(int[][] r3, int[] r4, int r5) {
        int length = r3.length;
        int r1 = length + 1;
        int[][] r2 = (int[][]) Arrays.copyOf(r3, r1);
        r2[length] = new int[]{0, Math.min(r5, rows(r3, r4))};
        return lift(r2, Arrays.copyOf(r4, r1));
    }

    static int[][] lift(int[][] r8, int[] r9) {
        int[][] r0 = new int[r8.length][];
        for (int r2 = 0; r2 < r8.length; r2++) {
            r0[r2] = (int[]) r8[r2].clone();
        }
        int r3 = rows(r8, r9) - 1;
        while (r3 >= 0) {
            boolean z = true;
            for (int r4 = 0; r4 < r8.length; r4++) {
                int r6 = r8[r4][1];
                z &= r3 < r6 || r3 >= r6 + r9[r4];
            }
            if (z) {
                for (int r5 = 0; r5 < r8.length; r5++) {
                    if (r8[r5][1] > r3) {
                        int[] r7 = r0[r5];
                        r7[1] = r7[1] - 1;
                    }
                }
            }
            r3--;
        }
        return r0;
    }

    /* JADX WARN: Code duplicated, block: B:4:0x000b  */
    static int[][] spots(String str, String str2, int r14) {
        int r3;
        int[][] r0 = sizes(str, str2);
        int length = str2.length();
        if (str == null) {
            r3 = 0;
        } else if (str.length() == length * 4) {
            r3 = 4;
        } else if (str.length() == length * 2) {
            r3 = 2;
        } else {
            r3 = 0;
        }
        if (r3 == 0) {
            return place(r0[0], r0[1], r14);
        }
        int[] r7 = (int[]) r0[1].clone();
        int r13 = str2.indexOf(103);
        if (r13 >= 0) {
            r7[r13] = 0;
        }
        int[][] r8 = new int[length][];
        for (int r9 = 0; r9 < length; r9++) {
            int r15 = r3 * r9;
            int r10 = Character.digit(str.charAt(r15), 36);
            int r11 = Character.digit(str.charAt(r15 + 1), 36);
            if (r10 >= 0 && r11 >= 0) {
                int[] r6 = r0[0];
                if (r6[r9] + r10 <= r14 && clear(r6, r7, r8, r9, r10, r11)) {
                    r8[r9] = new int[]{r10, r11};
                }
            }
            return place(r0[0], r0[1], r14);
        }
        return r8;
    }

    static String encode(int[][] r6, int[][] r7) {
        StringBuilder sb = new StringBuilder();
        for (int r2 = 0; r2 < r6.length; r2++) {
            sb.append(Character.forDigit(r6[r2][0], 36));
            sb.append(Character.forDigit(r6[r2][1], 36));
            sb.append(Character.forDigit(r7[0][r2], 36));
            sb.append(Character.forDigit(r7[1][r2], 36));
        }
        return sb.toString();
    }

    static int[] limits(char c) {
        char lowerCase = Character.toLowerCase(c);
        if (lowerCase == 'g') {
            return new int[]{4, 4, 4, 4};
        }
        if (lowerCase == 's') {
            return new int[]{2, 4, 1, 1};
        }
        if (lowerCase == 'v' || lowerCase == 'l') {
            return new int[]{1, 4, 1, 4};
        }
        if (lowerCase == 'm') {
            return new int[]{2, 4, 2, 4};
        }
        return new int[]{1, 4, 1, 2};
    }

    static int[] fit(char c, int r6, int r7) {
        int[] r0 = limits(c);
        int r8 = Math.max(r0[0], Math.min(r0[1], r6));
        int r9 = Math.max(r0[2], Math.min(r0[3], r7));
        char lowerCase = Character.toLowerCase(c);
        return new int[]{r8, ((lowerCase == 'l' || lowerCase == 'v') && r8 == 1 && r9 == 1) ? 2 : r9};
    }

    static int[][] sizes(String str, String str2) {
        int[][] r0 = spans(str2);
        int length = str2.length();
        if (str != null && str.length() == length * 4) {
            for (int r3 = 0; r3 < length; r3++) {
                int r4 = r3 * 4;
                int r5 = Character.digit(str.charAt(r4 + 2), 36);
                int r6 = Character.digit(str.charAt(r4 + 3), 36);
                int[] r7 = fit(str2.charAt(r3), r5, r6);
                if (r7[0] != r5 || r7[1] != r6) {
                    return spans(str2);
                }
                r0[0][r3] = r5;
                r0[1][r3] = r6;
            }
        }
        return r0;
    }

    static int beam(float f, float f2) {
        float fSqrt;
        float f3;
        float f4;
        float f5;
        if (f2 < 0.42f) {
            fSqrt = ((1.0f - cube(1.0f - (f2 / 0.42f))) * 0.85f) + 0.15f;
        } else {
            fSqrt = (float) Math.sqrt(Math.max(0.0f, 1.0f - sq((f2 - 0.42f) / 0.58000004f)));
        }
        float fAbs = fSqrt < 0.001f ? 2.0f : Math.abs(f) / fSqrt;
        float fSmooth = (1.0f - smooth(1.0f - ((smooth(0.2f, 0.9f, f2) * 0.45f) + 0.15f), 1.0f, fAbs)) * 0.92f * ((float) Math.exp(-sq(Math.max(0.0f, f2 - 0.1f) / 0.38f)));
        float fExp = ((float) Math.exp(-(sq(f / 0.75f) + sq((f2 - 0.08f) / 0.22f)))) * (1.0f - smooth(0.8f, 1.05f, fAbs));
        float f6 = (fAbs - 0.92f) / STAGGER_SPAN;
        float fSin = (f6 <= 0.0f || f6 >= 1.0f) ? 0.0f : ((float) Math.sin(((double) f6) * 3.141592653589793d)) * smooth(TURN, 0.1f, f2) * (1.0f - smooth(0.22f, 0.4f, f2)) * (1.0f - fExp);
        float fMin = Math.min(1.0f, Math.max(Math.max(fSmooth, fExp), fSin * 0.55f));
        int r9 = 0;
        if (fMin <= 0.002f) {
            return 0;
        }
        if (f < 0.0f) {
            f3 = f6 * 130.0f;
            f4 = 50.0f;
        } else {
            f3 = f6 * 90.0f;
            f4 = 190.0f;
        }
        int r14 = hue(f3 + f4, 0.55f);
        float fSmooth2 = smooth(0.05f, 0.5f, f2) * (1.0f - fExp);
        int r4 = 16;
        while (r4 >= 0) {
            if (r4 == 16) {
                f5 = 0.0f;
            } else {
                f5 = r4 == FX_FLIP ? 18.0f : 34.0f;
            }
            float f7 = 255.0f - (f5 * fSmooth2);
            r9 = (r9 << FX_FLIP) | Math.round(f7 + ((((r14 >> r4) & 255) - f7) * fSin * 0.9f));
            r4 -= 8;
        }
        return (Math.round(fMin * 255.0f) << 24) | r9;
    }

    private static int hue(float f, float f2) {
        int r0 = 0;
        for (int r1 = 5; r1 >= 1; r1 -= 2) {
            float f3 = (r1 + (f / 60.0f)) % 6.0f;
            r0 = (r0 << FX_FLIP) | Math.round((1.0f - (Math.max(0.0f, Math.min(Math.min(f3, 4.0f - f3), 1.0f)) * f2)) * 255.0f);
        }
        return r0;
    }

    private static float smooth(float f, float f2, float f3) {
        float fClamp = clamp((f3 - f) / (f2 - f));
        return fClamp * fClamp * (3.0f - (fClamp * 2.0f));
    }

    static void smooth(int[] r18, int r19, int r20) {
        int[] r3 = new int[r18.length];
        int r4 = 0;
        int r5 = 0;
        while (true) {
            char c = 2;
            if (r5 >= 2) {
                return;
            }
            int[] r7 = r5 == 0 ? r18 : r3;
            int[] r8 = r5 == 0 ? r3 : r18;
            int r9 = r4;
            while (r9 < r20) {
                int r10 = r4;
                while (r10 < r19) {
                    int r11 = r9 * r19;
                    int r12 = r11 + r10;
                    int r13 = r5 == 0 ? Math.max(r4, r10 - 1) + r11 : (Math.max(r4, r9 - 1) * r19) + r10;
                    int r14 = r5 == 0 ? r11 + Math.min(r19 - 1, r10 + 1) : (Math.min(r20 - 1, r9 + 1) * r19) + r10;
                    int r15 = r4;
                    int r16 = r15;
                    while (r15 < 32) {
                        r16 |= ((((((r7[r13] >>> r15) & 255) + (((r7[r12] >>> r15) & 255) * 2)) + ((r7[r14] >>> r15) & 255)) + 2) / 4) << r15;
                        r15 += FX_FLIP;
                        c = c;
                    }
                    r8[r12] = r16;
                    r10++;
                    r4 = 0;
                }
                r9++;
                r4 = 0;
            }
            r5++;
            r4 = 0;
        }
    }

    static float detail(int[] r6, int r7) {
        long jAbs = 0;
        int r2 = 0;
        for (int r3 = 1; r3 < r6.length; r3++) {
            if (r3 % r7 != 0) {
                jAbs += (long) Math.abs(((r6[r3] >> FX_FLIP) & 255) - ((r6[r3 - 1] >> FX_FLIP) & 255));
                r2++;
            }
        }
        if (r2 == 0) {
            return 0.0f;
        }
        return jAbs / r2;
    }

    static int seekColor(int[] r9) {
        int r3 = 24;
        int r2 = 0;
        for (int r4 : r9) {
            int r5 = (r4 >> 16) & 255;
            int r6 = (r4 >> FX_FLIP) & 255;
            int r7 = r4 & 255;
            int r8 = Math.max(r5, Math.max(r6, r7)) - Math.min(r5, Math.min(r6, r7));
            if (r8 > r3) {
                r2 = r4;
                r3 = r8;
            }
        }
        if (r2 == 0) {
            return -1;
        }
        float f = (r2 >> 16) & 255;
        float f2 = (r2 >> FX_FLIP) & 255;
        float f3 = r2 & 255;
        float fMax = Math.max(f, Math.max(f2, f3));
        float fMin = fMax - Math.min(f, Math.min(f2, f3));
        float fMax2 = Math.max(0.45f, Math.min(0.65f, fMin / fMax));
        return (Math.round((1.0f - (((fMax - f) * fMax2) / fMin)) * 255.0f) << 16) | (-16777216) | (Math.round((1.0f - (((fMax - f2) * fMax2) / fMin)) * 255.0f) << FX_FLIP) | Math.round((1.0f - ((fMax2 * (fMax - f3)) / fMin)) * 255.0f);
    }

    static int tilesRow(int[][] r7, int[] r8, int r9) {
        int r10 = Math.max(0, Math.min(r9, rows(r7, r8)));
        boolean z = true;
        while (z) {
            z = false;
            for (int r3 = 0; r3 < r7.length; r3++) {
                int r4 = r7[r3][1];
                if (r4 < r10) {
                    int r5 = r8[r3];
                    if (r4 + r5 > r10) {
                        r10 = r4 + r5;
                        z = true;
                    }
                }
            }
        }
        return r10;
    }

    static int[][] below(int[][] r7, int r8, int r9) {
        int[][] r0 = new int[r7.length][];
        for (int r2 = 0; r2 < r7.length; r2++) {
            int[] r3 = r7[r2];
            int r5 = r3[1];
            r0[r2] = r5 >= r8 ? new int[]{r3[0], r5 + r9} : (int[]) r3.clone();
        }
        return r0;
    }

    static float level(float[] fArr, int r2, float f) {
        return r2 < fArr.length ? fArr[r2] : fArr[1] * f;
    }

    static int rows(int[][] r4, int[] r5) {
        int r1 = 0;
        for (int r0 = 0; r0 < r4.length; r0++) {
            r1 = Math.max(r1, r4[r0][1] + r5[r0]);
        }
        return r1;
    }

    static int[][] spans(String str) {
        int[][] r1 = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 2, str.length());
        for (int r2 = 0; r2 < str.length(); r2++) {
            r1[0][r2] = spanW(str.charAt(r2));
            r1[1][r2] = spanH(str.charAt(r2));
        }
        return r1;
    }

    static int effect(String str) {
        if (str == null) {
            return 0;
        }
        String lowerCase = str.toLowerCase(Locale.ROOT);
        if (has(lowerCase, "flashlight", "torch")) {
            return 1;
        }
        if (has(lowerCase, "airplane", "aeroplane", "flight")) {
            return 2;
        }
        if (has(lowerCase, "rotat")) {
            return 3;
        }
        if (has(lowerCase, "location", "gps")) {
            return 4;
        }
        if (has(lowerCase, "wallet", "pay", "home", "device", "multi", "font", "link", "dex")) {
            return FX_JUMP;
        }
        if (has(lowerCase, "hotspot", "tether", "nfc", "share", "nearby", "cast", "smartview", "mirror")) {
            return 6;
        }
        if (has(lowerCase, "wifi", "internet")) {
            return 7;
        }
        if (has(lowerCase, "bluetooth") || lowerCase.equals("bt")) {
            return FX_FLIP;
        }
        if (has(lowerCase, "data", "cell")) {
            return FX_BOB;
        }
        if (has(lowerCase, "dark", "uimode", "invert", "sync")) {
            return FX_TURN;
        }
        if (has(lowerCase, "dnd", "disturb", "zen", "night", "bluelight", "eye", "sleep", "bedtime")) {
            return 5;
        }
        if (has(lowerCase, "power", "battery", "saver")) {
            return FX_FILL;
        }
        if (has(lowerCase, "record", "screenshot", "capture", "camera", "qr")) {
            return FX_SHUTTER;
        }
        if (has(lowerCase, "sound", "vibrat", "mute", "alarm", "ringer")) {
            return FX_SHAKE;
        }
        return 0;
    }

    static int accent(String str, int r8) {
        if (str != null) {
            if (kind(str) == 3) {
                return DATA_GREEN;
            }
            String lowerCase = str.toLowerCase(Locale.ROOT);
            if (has(lowerCase, "flashlight", "torch", "dnd", "disturb", "zen", "sleep", "bedtime")) {
                return -11651332;
            }
            if (has(lowerCase, "airplane", "aeroplane", "flight")) {
                return -101120;
            }
            if (has(lowerCase, "rotat", "sound", "vibrat", "mute", "record")) {
                return -2286833;
            }
            if (has(lowerCase, "power", "battery", "saver", "eye", "bluelight")) {
                return -936946;
            }
        }
        return r8;
    }

    private static boolean has(String str, String... strArr) {
        for (String str2 : strArr) {
            if (str.contains(str2)) {
                return true;
            }
        }
        return false;
    }

    static int[] artTones(int[] r16, int r17) {
        long[] jArr = new long[6];
        int[] r3 = new int[2];
        for (int r5 = 0; r5 < r17; r5++) {
            for (int r7 = 0; r7 < r17; r7++) {
                int r8 = r16[(r5 * r17) + r7];
                int r9 = r7 + r5 < r17 ? 0 : 1;
                int r10 = r9 * 3;
                jArr[r10] = jArr[r10] + ((long) ((r8 >> 16) & 255));
                int r11 = r10 + 1;
                jArr[r11] = jArr[r11] + ((long) ((r8 >> FX_FLIP) & 255));
                int r12 = r10 + 2;
                jArr[r12] = jArr[r12] + ((long) (r8 & 255));
                r3[r9] = r3[r9] + 1;
            }
        }
        int[] r0 = new int[2];
        for (int r4 = 0; r4 < 2; r4++) {
            int r13 = r4 * 3;
            float fMax = Math.max(1, r3[r4]);
            r0[r4] = tone(jArr[r13] / fMax, jArr[r13 + 1] / fMax, jArr[r13 + 2] / fMax);
        }
        return r0;
    }

    static int tone(float f, float f2, float f3) {
        float f4 = (0.299f * f) + (0.587f * f2) + (0.114f * f3);
        float[] fArr = {((f - f4) * 1.35f) + f4, ((f2 - f4) * 1.35f) + f4, f4 + ((f3 - f4) * 1.35f)};
        float fMin = Math.min(TONE_PEAK / Math.max(1.0f, Math.max(fArr[0], Math.max(fArr[1], fArr[2]))), 1.6f);
        int r5 = -16777216;
        for (int r2 = 0; r2 < 3; r2++) {
            r5 |= Math.round(Math.max(0.0f, Math.min(255.0f, fArr[r2] * fMin))) << (16 - (r2 * FX_FLIP));
        }
        return r5;
    }

    static String clock(long j) {
        long jMax = Math.max(0L, j) / 1000;
        long j2 = jMax / 3600;
        long j3 = (jMax / 60) % 60;
        long j4 = jMax % 60;
        if (j2 > 0) {
            return String.format(Locale.ROOT, "%d:%02d:%02d", Long.valueOf(j2), Long.valueOf(j3), Long.valueOf(j4));
        }
        return String.format(Locale.ROOT, "%d:%02d", Long.valueOf(j3), Long.valueOf(j4));
    }

    static final class Spring {
        private static final float MAX_DT = 0.033333335f;
        private static final float SUB_STEP = 0.004166667f;
        private float damping;
        private float stiffness;
        float target;
        float value;
        float velocity;
        static final float[] BOUNCY = {380.0f, 0.62f};
        static final float[] SNAPPY = {700.0f, 0.85f};
        static final float[] PRESS = {600.0f, 0.55f};
        static final float[] PRESS_IN = {900.0f, 1.0f};
        static final float[] PRESS_OUT = {420.0f, 0.55f};
        static final float[] REVEAL = {380.0f, 0.68f};
        static final float[] OPEN = {360.0f, 0.82f};
        static final float[] PULL = of(0.37f, -0.15f);
        static final float[] MENU = {260.0f, 0.72f};
        static final float[] SWING = {420.0f, 0.12f};
        static final float[] BUZZ = {2400.0f, 0.07f};
        static final float[] PAGE = of(0.28f, 0.12f);
        static final float[] FLOOD = {Logic.TONE_PEAK, 1.0f};
        static final float[] SLOSH = {170.0f, 0.35f};
        static final float[] WOBBLE = {300.0f, Logic.DROP_MARGIN};
        static final float[] JITTER = {900.0f, 0.15f};

        static float[] of(float f, float f2) {
            float f3 = (float) (6.283185307179586d / ((double) f));
            return new float[]{f3 * f3, 1.0f - f2};
        }

        Spring(float[] fArr) {
            tune(fArr);
        }

        void tune(float[] fArr) {
            float f = fArr[0];
            this.stiffness = f;
            this.damping = fArr[1] * 2.0f * ((float) Math.sqrt(f));
        }

        boolean step(float f) {
            float fMin = Math.min(f, MAX_DT);
            while (fMin > 0.0f) {
                float fMin2 = Math.min(fMin, SUB_STEP);
                float f2 = this.velocity;
                float f3 = this.stiffness;
                float f4 = this.target;
                float f5 = this.value;
                float f6 = f2 + (((f3 * (f4 - f5)) - (this.damping * f2)) * fMin2);
                this.velocity = f6;
                this.value = f5 + (f6 * fMin2);
                fMin -= fMin2;
            }
            if (Math.abs(this.target - this.value) >= 0.0015f || Math.abs(this.velocity) >= 0.02f) {
                return false;
            }
            this.value = this.target;
            this.velocity = 0.0f;
            return true;
        }
    }

    static float stretch(float f, float f2, float f3) {
        return Math.min(f3, Math.abs(f) * f2);
    }

    private Logic() {
    }
}
