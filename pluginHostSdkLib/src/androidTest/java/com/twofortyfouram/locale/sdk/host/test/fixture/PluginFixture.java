/*
 * android-plugin-host-sdk-for-locale https://github.com/twofortyfouram/android-plugin-host-sdk-for-locale
 * Copyright 2015-2016 two forty four a.m. LLC
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

package com.twofortyfouram.locale.sdk.host.test.fixture;

import android.support.annotation.NonNull;

import com.twofortyfouram.locale.sdk.host.model.Plugin;
import com.twofortyfouram.locale.sdk.host.model.PluginConfiguration;
import com.twofortyfouram.locale.sdk.host.model.PluginType;

import net.jcip.annotations.ThreadSafe;

/**
 * Fixture values for {@link com.twofortyfouram.locale.sdk.host.model.Plugin}.
 */
@ThreadSafe
public final class PluginFixture {

    @NonNull
    public static final String DEFAULT_PACKAGE = "com.twofortyfouram.locale"; //$NON-NLS-1$

    @NonNull
    public static final String DEFAULT_ACTIVITY
            = "com.twofortyfouram.locale.ui.activity.SomeActivity"; //$NON-NLS-1$

    @NonNull
    public static final String DEFAULT_RECEIVER
            = "com.twofortyfouram.locale.receiver.SomeReceiver"; //$NON-NLS-1$

    public static final int DEFAULT_VERSION_CODE = 1;

    @NonNull
    public static final PluginConfiguration DEFAULT_CONFIGURATION = PluginConfigurationFixture
            .newPluginConfiguration();

    @NonNull
    public static Plugin newDefaultPlugin() {
        return new Plugin(PluginType.CONDITION, DEFAULT_PACKAGE, DEFAULT_ACTIVITY,
                DEFAULT_RECEIVER, DEFAULT_VERSION_CODE,
                PluginConfigurationFixture.newPluginConfiguration());
    }

    /**
     * Private constructor prevents instantiation.
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PluginFixture() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
