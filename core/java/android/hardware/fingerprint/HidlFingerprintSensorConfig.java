/*
 * Copyright (C) 2023 The Android Open Source Project
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

package android.hardware.fingerprint;

import android.annotation.NonNull;
import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.common.CommonProps;
import android.hardware.biometrics.common.SensorStrength;
import android.hardware.biometrics.fingerprint.FingerprintSensorType;
import android.hardware.biometrics.fingerprint.SensorLocation;
import android.hardware.biometrics.fingerprint.SensorProps;
import android.os.Build;
import android.os.SystemProperties;
import android.util.DisplayMetrics;

import com.android.internal.R;
import com.android.internal.util.ArrayUtils;

import java.io.File;
import java.util.Locale;

/**
 * Parse HIDL fingerprint sensor config and map it to SensorProps.aidl to match AIDL.
 * See core/res/res/values/config.xml config_biometric_sensors
 * @hide
 */
public final class HidlFingerprintSensorConfig extends SensorProps {
    private int mSensorId;
    private int mModality;
    private int mStrength;

    /**
     * Parse through the config string and map it to SensorProps.aidl.
     * @throws IllegalArgumentException when config string has unexpected format
     */
    public void parse(@NonNull String config, @NonNull Context context)
            throws IllegalArgumentException {
        final String[] elems = config.split(":");
        if (elems.length < 3) {
            throw new IllegalArgumentException();
        }
        mSensorId = Integer.parseInt(elems[0]);
        mModality = Integer.parseInt(elems[1]);
        mStrength = Integer.parseInt(elems[2]);
        mapHidlToAidlSensorConfiguration(context);
    }

    @BiometricAuthenticator.Modality
    public int getModality() {
        return mModality;
    }

    private void mapHidlToAidlSensorConfiguration(@NonNull Context context) {
        commonProps = new CommonProps();
        commonProps.componentInfo = null;
        commonProps.sensorId = mSensorId;
        commonProps.sensorStrength = authenticatorStrengthToPropertyStrength(mStrength);
        commonProps.maxEnrollmentsPerUser = context.getResources().getInteger(
                R.integer.config_fingerprintMaxTemplatesPerUser);
        halControlsIllumination = false;
        sensorLocations = new SensorLocation[1];

        final int[] udfpsProps = context.getResources().getIntArray(
                com.android.internal.R.array.config_udfps_sensor_props);
        final String vendorBrand = SystemProperties.get("ro.product.vendor.brand",
                Build.BRAND != null ? Build.BRAND : "").toLowerCase(Locale.ROOT);
        final String vendorDevice = SystemProperties.get("ro.product.vendor.device",
                Build.DEVICE != null ? Build.DEVICE : "").toLowerCase(Locale.ROOT);
        final boolean isSamsung = vendorBrand.contains("samsung");
        final boolean isSamsungUltrasonicDevice = isSamsung && (
                vendorDevice.startsWith("o1") || vendorDevice.startsWith("t2")
                || vendorDevice.startsWith("p3") || vendorDevice.startsWith("x1")
                || vendorDevice.startsWith("y2") || vendorDevice.startsWith("z3")
                || vendorDevice.startsWith("c1") || vendorDevice.startsWith("c2")
                || vendorDevice.startsWith("r0") || vendorDevice.startsWith("g0")
                || vendorDevice.startsWith("b0") || vendorDevice.startsWith("beyond1")
                || vendorDevice.startsWith("beyond2") || vendorDevice.startsWith("d1")
                || vendorDevice.startsWith("d2"));
        final boolean isSamsungOpticalDevice = isSamsung && (
                vendorDevice.startsWith("r8") || vendorDevice.startsWith("r9")
                || vendorDevice.startsWith("a5") || vendorDevice.startsWith("a7")
                || vendorDevice.startsWith("a3") || vendorDevice.startsWith("m5"));
        final boolean hasSamsungUdfpsSysfs = new File("/sys/class/fingerprint/fingerprint/position").exists();
        final boolean isUdfps = !ArrayUtils.isEmpty(udfpsProps)
                || isSamsungUltrasonicDevice
                || isSamsungOpticalDevice
                || hasSamsungUdfpsSysfs
                || SystemProperties.getBoolean("persist.sys.phh.fod", false);
        // config_is_powerbutton_fps indicates whether device has a power button fingerprint sensor.
        final boolean isPowerbuttonFps = context.getResources().getBoolean(
                R.bool.config_is_powerbutton_fps)
                || vendorDevice.startsWith("spes")
                || vendorDevice.startsWith("sweet")
                || vendorDevice.startsWith("alioth")
                || vendorDevice.startsWith("munch")
                || vendorDevice.startsWith("surya")
                || vendorDevice.startsWith("mojito");

        if (isUdfps) {
            sensorType = isSamsungUltrasonicDevice
                    ? FingerprintSensorType.UNDER_DISPLAY_ULTRASONIC
                    : FingerprintSensorType.UNDER_DISPLAY_OPTICAL;
            halControlsIllumination = (sensorType == FingerprintSensorType.UNDER_DISPLAY_OPTICAL);
        } else if (isPowerbuttonFps) {
            sensorType = FingerprintSensorType.POWER_BUTTON;
        } else {
            sensorType = FingerprintSensorType.REAR;
        }

        if (isUdfps && udfpsProps.length == 3) {
            setSensorLocation(udfpsProps[0], udfpsProps[1], udfpsProps[2]);
        } else {
            final DisplayMetrics dm = context.getResources().getDisplayMetrics();
            final int w = (dm != null && dm.widthPixels > 0) ? dm.widthPixels : 1080;
            final int h = (dm != null && dm.heightPixels > 0) ? dm.heightPixels : 2400;
            setSensorLocation(w / 2, (int) (h * 0.815f), (int) (w * 0.105f));
        }

    }

    private void setSensorLocation(int sensorLocationX,
            int sensorLocationY, int sensorRadius) {
        sensorLocations[0] = new SensorLocation();
        sensorLocations[0].display = "";
        sensorLocations[0].sensorLocationX = sensorLocationX;
        sensorLocations[0].sensorLocationY = sensorLocationY;
        sensorLocations[0].sensorRadius = sensorRadius;
    }

    private byte authenticatorStrengthToPropertyStrength(
            @BiometricManager.Authenticators.Types int strength) {
        switch (strength) {
            case BiometricManager.Authenticators.BIOMETRIC_CONVENIENCE:
                return SensorStrength.CONVENIENCE;
            case BiometricManager.Authenticators.BIOMETRIC_WEAK:
                return SensorStrength.WEAK;
            case BiometricManager.Authenticators.BIOMETRIC_STRONG:
                return SensorStrength.STRONG;
            default:
                throw new IllegalArgumentException("Unknown strength: " + strength);
        }
    }
}
