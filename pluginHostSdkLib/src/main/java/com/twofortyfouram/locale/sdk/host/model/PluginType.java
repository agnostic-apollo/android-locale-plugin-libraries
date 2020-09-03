/*
 * android-plugin-host-sdk-for-locale https://github.com/twofortyfouram/android-plugin-host-sdk-for-locale
 * Copyright 2015 two forty four a.m. LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twofortyfouram.locale.sdk.host.model;

import androidx.annotation.NonNull;
import net.jcip.annotations.ThreadSafe;

import com.twofortyfouram.locale.api.Intent;

import net.jcip.annotations.ThreadSafe;

import static com.twofortyfouram.assertion.Assertions.assertNotEmpty;

/**
 * Enumerates the types of plug-ins for Locale.
 */
@ThreadSafe
public enum PluginType {

    /**
     * A plug-in condition.
     *
     * @see com.twofortyfouram.locale.api.Intent#ACTION_EDIT_CONDITION
     * @see com.twofortyfouram.locale.api.Intent#ACTION_QUERY_CONDITION
     */
    @NonNull
    CONDITION(Intent.ACTION_EDIT_CONDITION,
            Intent.ACTION_QUERY_CONDITION),

    /**
     * A plug-in setting.
     *
     * @see com.twofortyfouram.locale.api.Intent#ACTION_EDIT_SETTING
     * @see com.twofortyfouram.locale.api.Intent#ACTION_FIRE_SETTING
     */
    @NonNull
    SETTING(Intent.ACTION_EDIT_SETTING,
            Intent.ACTION_FIRE_SETTING);

    @NonNull
    private final String mActivityIntentAction;

    @NonNull
    private final String mReceiverIntentAction;

    PluginType(@NonNull final String activityIntentAction,
            @NonNull final String receiverIntentAction) {
        assertNotEmpty(activityIntentAction, "activityIntentAction"); //$NON-NLS-1$
        assertNotEmpty(receiverIntentAction, "receiverIntentAction"); //$NON-NLS-1$

        mActivityIntentAction = activityIntentAction;
        mReceiverIntentAction = receiverIntentAction;
    }

    /**
     * @return The Activity Intent action for the plug-in type.
     */
    @NonNull
    public String getActivityIntentAction() {
        return mActivityIntentAction;
    }

    /**
     * @return The BroadcastReceiver Intent action for the plug-in type.
     */
    @NonNull
    public String getReceiverIntentAction() {
        return mReceiverIntentAction;
    }
}
