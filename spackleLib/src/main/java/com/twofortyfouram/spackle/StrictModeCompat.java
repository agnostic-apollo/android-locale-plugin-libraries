/*
 * android-spackle https://github.com/twofortyfouram/android-spackle
 * Copyright (C) 2009–2017 two forty four a.m. LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.twofortyfouram.spackle;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;

import androidx.annotation.NonNull;
import net.jcip.annotations.ThreadSafe;

import static com.twofortyfouram.assertion.Assertions.assertNotEmpty;

/**
 * A wrapper class to use {@link android.os.StrictMode} without worrying about backwards
 * compatibility. Calls to this class from unsupported API levels have no effect.
 */
@ThreadSafe
@SuppressLint("NewApi")
public final class StrictModeCompat {

    /**
     * Notes a slow method call.
     *
     * @param name tag for the slow call.
     * @see android.os.StrictMode#noteSlowCall(String)
     */
    public static void noteSlowCall(@NonNull final String name) {
        assertNotEmpty(name, "name"); //$NON-NLS-1$

        if (AndroidSdkVersion.isAtLeastSdk(Build.VERSION_CODES.HONEYCOMB)) {
            StrictMode.noteSlowCall(name);
        }
    }

    /**
     * Enables/disables strict mode with default policies to detect all violations and log them.
     *
     * @param isStrictModeEnabled true to enable strict mode, false to disable strict mode.
     */
    public static void setStrictMode(final boolean isStrictModeEnabled) {
        if (AndroidSdkVersion.isAtLeastSdk(Build.VERSION_CODES.GINGERBREAD)) {
            if (isStrictModeEnabled) {
                StrictMode.enableDefaults();
                StrictMode.setThreadPolicy(new ThreadPolicy.Builder().detectAll().penaltyLog()
                        .build());
                StrictMode.setVmPolicy(new VmPolicy.Builder().detectAll().penaltyLog().build());
            } else {
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
                StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX);
            }
        }
    }

    /**
     * Private constructor prevents instantiation.
     *
     * @throws UnsupportedOperationException because this class cannot be
     *                                       instantiated.
     */
    private StrictModeCompat() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
