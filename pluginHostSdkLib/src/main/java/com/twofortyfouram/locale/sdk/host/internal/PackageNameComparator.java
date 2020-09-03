/*
 * android-plugin-host-sdk-for-locale https://github.com/twofortyfouram/android-plugin-host-sdk-for-locale
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

package com.twofortyfouram.locale.sdk.host.internal;

import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;
import net.jcip.annotations.ThreadSafe;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Helper class that compares {@link ResolveInfo} objects by their package name.
 */
@ThreadSafe
public final class PackageNameComparator implements Comparator<ResolveInfo>,
        Serializable {

    /**
     * Implements the {@link Serializable} interface.
     */
    private static final long serialVersionUID = -5145288289897584068L;

    @Override
    public int compare(@NonNull final ResolveInfo object1, @NonNull final ResolveInfo object2) {
        return object1.activityInfo.packageName
                .compareToIgnoreCase(object2.activityInfo.packageName);
    }
}
