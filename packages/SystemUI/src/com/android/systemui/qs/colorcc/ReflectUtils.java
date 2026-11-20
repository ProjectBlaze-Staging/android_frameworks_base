/*
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.qs.colorcc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Lightweight AOSP reflection helper replacing de.robv.android.xposed.XposedHelpers
 * for internal framework/SystemUI method & field access inside ColorCC.
 */
final class ReflectUtils {

    private ReflectUtils() {
    }

    static Class<?> findClass(String className, ClassLoader classLoader) {
        try {
            if (classLoader != null) {
                return Class.forName(className, false, classLoader);
            }
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static Field findFieldIfExists(Class<?> clazz, String fieldName) {
        Class<?> c = clazz;
        while (c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    static Field findField(Class<?> clazz, String fieldName) {
        Field f = findFieldIfExists(clazz, fieldName);
        if (f == null) {
            throw new RuntimeException("NoSuchField: " + clazz.getName() + "#" + fieldName);
        }
        return f;
    }

    static Object getObjectField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean getBooleanField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getBoolean(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static int getIntField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getInt(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static float getFloatField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getFloat(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static void setFloatField(Object obj, String fieldName, float value) {
        try {
            findField(obj.getClass(), fieldName).setFloat(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static long getStaticLongField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).getLong(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean getStaticBooleanField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).getBoolean(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static Object newInstance(Class<?> clazz, Object[] args) {
        try {
            int len = args == null ? 0 : args.length;
            for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
                Class<?>[] pTypes = ctor.getParameterTypes();
                if (pTypes.length == len && matchParams(pTypes, args)) {
                    ctor.setAccessible(true);
                    return ctor.newInstance(args);
                }
            }
            throw new NoSuchMethodException(clazz.getName() + ".<init>");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Object callMethod(Object obj, String methodName, Object[] args) {
        if (obj == null) return null;
        return callMethodInternal(obj.getClass(), obj, methodName, null, args);
    }

    static Object callMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object[] args) {
        if (obj == null) return null;
        return callMethodInternal(obj.getClass(), obj, methodName, parameterTypes, args);
    }

    static Object callStaticMethod(Class<?> clazz, String methodName, Object[] args) {
        return callMethodInternal(clazz, null, methodName, null, args);
    }

    private static Object callMethodInternal(Class<?> clazz, Object receiver, String methodName,
            Class<?>[] explicitTypes, Object[] args) {
        try {
            int len = args == null ? 0 : args.length;
            if (explicitTypes != null) {
                Method m = findMethodExact(clazz, methodName, explicitTypes);
                m.setAccessible(true);
                return m.invoke(receiver, args);
            }
            Class<?> c = clazz;
            while (c != null) {
                for (Method m : c.getDeclaredMethods()) {
                    if (m.getName().equals(methodName)) {
                        Class<?>[] pTypes = m.getParameterTypes();
                        if (pTypes.length == len && matchParams(pTypes, args)) {
                            m.setAccessible(true);
                            return m.invoke(receiver, args);
                        }
                    }
                }
                c = c.getSuperclass();
            }
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(methodName)) {
                    Class<?>[] pTypes = m.getParameterTypes();
                    if (pTypes.length == len && matchParams(pTypes, args)) {
                        m.setAccessible(true);
                        return m.invoke(receiver, args);
                    }
                }
            }
            throw new NoSuchMethodException(clazz.getName() + "#" + methodName + "(" + Arrays.toString(args) + ")");
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }

    private static Method findMethodExact(Class<?> clazz, String methodName, Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        Class<?> c = clazz;
        while (c != null) {
            try {
                return c.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                c = c.getSuperclass();
            }
        }
        return clazz.getMethod(methodName, parameterTypes);
    }

    private static boolean matchParams(Class<?>[] paramTypes, Object[] args) {
        if (args == null || args.length == 0) return paramTypes.length == 0;
        for (int i = 0; i < paramTypes.length; i++) {
            Object arg = args[i];
            Class<?> pt = paramTypes[i];
            if (arg == null) {
                if (pt.isPrimitive()) return false;
            } else {
                Class<?> at = arg.getClass();
                if (!wrap(pt).isAssignableFrom(wrap(at))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Class<?> wrap(Class<?> c) {
        if (!c.isPrimitive()) return c;
        if (c == int.class) return Integer.class;
        if (c == boolean.class) return Boolean.class;
        if (c == float.class) return Float.class;
        if (c == long.class) return Long.class;
        if (c == double.class) return Double.class;
        if (c == byte.class) return Byte.class;
        if (c == short.class) return Short.class;
        if (c == char.class) return Character.class;
        return c;
    }
}
