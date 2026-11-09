/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.server.biometrics.sensors.fingerprint;

import android.annotation.NonNull;
import android.content.Context;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.util.Slog;

import java.util.ArrayList;

/**
 * Contains helper methods for under-display fingerprint HIDL.
 */
public class UdfpsHelper {

    private static final String TAG = "UdfpsHelper";
    private static final int SEM_FINGER_STATE = 22;
    private static final int SEM_FINGER_UP = 1;
    private static final int SEM_FINGER_DOWN = 2;
    private static final int SEH_REQUEST_TRANSACTION_ID = 11;

    public static void sendSamsungSehRequest(IBiometricsFingerprint daemon, int cmd, int param) {
        if (daemon == null) return;
        try {
            ArrayList<String> chain = daemon.interfaceChain();
            String sehDescriptor = null;
            if (chain != null) {
                for (String desc : chain) {
                    if (desc != null && desc.contains("ISehBiometricsFingerprint")) {
                        sehDescriptor = desc;
                        break;
                    }
                }
            }
            if (sehDescriptor == null) return;
            IHwBinder binder = daemon.asBinder();
            if (binder == null) return;
            HwParcel data = new HwParcel();
            HwParcel reply = new HwParcel();
            try {
                data.writeInterfaceToken(sehDescriptor);
                data.writeInt32(cmd);
                data.writeInt32(param);
                data.writeInt8Vector(new ArrayList<Byte>());
                binder.transact(SEH_REQUEST_TRANSACTION_ID, data, reply, 0);
                reply.verifySuccess();
            } finally {
                reply.release();
            }
        } catch (Throwable t) {
            Slog.w(TAG, "sendSamsungSehRequest(" + cmd + ", " + param + ") failed: " + t);
        }
    }

    public static void onFingerDown(IBiometricsFingerprint daemon, int x, int y, float minor,
            float major) {
        android.hardware.biometrics.fingerprint.V2_3.IBiometricsFingerprint extension =
                android.hardware.biometrics.fingerprint.V2_3.IBiometricsFingerprint.castFrom(
                        daemon);
        if (extension != null) {
            try {
                extension.onFingerDown(x, y, minor, major);
                return;
            } catch (RemoteException e) {
                Slog.e(TAG, "onFingerDown | RemoteException: ", e);
            }
        }
        sendSamsungSehRequest(daemon, SEM_FINGER_STATE, SEM_FINGER_DOWN);
    }

    public static void onFingerUp(IBiometricsFingerprint daemon) {
        android.hardware.biometrics.fingerprint.V2_3.IBiometricsFingerprint extension =
                android.hardware.biometrics.fingerprint.V2_3.IBiometricsFingerprint.castFrom(
                        daemon);
        if (extension != null) {
            try {
                extension.onFingerUp();
                return;
            } catch (RemoteException e) {
                Slog.e(TAG, "onFingerUp | RemoteException: ", e);
            }
        }
        sendSamsungSehRequest(daemon, SEM_FINGER_STATE, SEM_FINGER_UP);
    }

    public static boolean isValidAcquisitionMessage(@NonNull Context context,
            int acquireInfo, int vendorCode) {
        return FingerprintManager.getAcquiredString(context, acquireInfo, vendorCode) != null;
    }
}
