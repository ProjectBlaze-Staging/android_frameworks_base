/*
 * Copyright (C) 2012 The CyanogenMod Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.internal.os;

import android.view.KeyEvent;

/**
 * Interface for device specific key handling.
 * @hide
 */
public interface DeviceKeyHandler {
    /**
     * Invoked when an unhandled key of a device was pressed
     *
     * @param event The key event to handle
     * @return The same or a new event if it was not handled, or null if the event was consumed.
     */
    KeyEvent handleKeyEvent(KeyEvent event);
}
