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

package com.twofortyfouram.locale.sdk.host.ui.loader;

import androidx.loader.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.twofortyfouram.locale.sdk.host.api.PluginRegistry;
import com.twofortyfouram.locale.sdk.host.model.Plugin;
import com.twofortyfouram.locale.sdk.host.model.PluginType;
import com.twofortyfouram.log.Lumberjack;
import com.twofortyfouram.spackle.ContextUtil;
import com.twofortyfouram.spackle.bundle.BundleScrubber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import net.jcip.annotations.NotThreadSafe;

import java.util.Map;

import static com.twofortyfouram.assertion.Assertions.assertIsMainThread;
import static com.twofortyfouram.assertion.Assertions.assertNotNull;

/**
 * An {@link AsyncTaskLoader} for loading the {@link PluginRegistry}.
 * <p>
 * In addition to reloading when the registry changes, this also reloads when an
 * interesting configuration change occurs that could affect the display of
 * plug-ins in the UI.
 */
@NotThreadSafe
public final class SupportPluginRegistryLoader extends
        androidx.loader.content.AsyncTaskLoader<Map<String, Plugin>> {

    /**
     * Plug-in type.
     */
    @NonNull
    private final PluginType mType;

    /**
     * Cached reference to the singleton registry.
     */
    @NonNull
    private final PluginRegistry mPluginRegistry;

    /**
     * Listens for registry changes.
     * <p/>
     * When {@code null}, indicates that no receiver is registered.
     */
    @Nullable
    private RegistryReloadReceiver mReceiver = null;

    /**
     * Tracks the configuration of the device since the last load.
     */
    @NonNull
    private final com.twofortyfouram.locale.sdk.host.internal.UiConfigChangeChecker mLastConfig
            = new com.twofortyfouram.locale.sdk.host.internal.UiConfigChangeChecker();

    /**
     * The loaded registry.
     */
    @Nullable
    private Map<String, Plugin> mResult = null;

    /**
     * @param context {@code Context}.
     * @param type    The plug-in type to load.
     */
    public SupportPluginRegistryLoader(@NonNull final Context context,
                                       @NonNull final PluginType type) {
        super(ContextUtil.cleanContext(context));

        assertNotNull(type, "type"); //$NON-NLS-1$

        mType = type;
        mPluginRegistry = PluginRegistry.getInstance(getContext());
    }

    @Override
    @WorkerThread
    public Map<String, Plugin> loadInBackground() {
        /*
         * THREADING: This will be called on a background thread
         */

        return mPluginRegistry.getPluginMap(mType);
    }

    @Override
    public void deliverResult(final Map<String, Plugin> result) {
        assertIsMainThread();

        // The registry for the other plugin type (e.g. conditions vs settings) could have changed,
        // leaving the plugin type monitored by this class unchanged.  == is appropriate here.
        //noinspection ObjectEquality
        if (result != mResult) {
            // Registry Map is immutable, so this inspection is safe to suppress
            //noinspection AssignmentToCollectionOrArrayFieldFromParameter
            mResult = result;

            if (isStarted()) {
                super.deliverResult(mResult);
            }
        }
    }

    @Override
    protected void onReset() {
        assertIsMainThread();

        onStopLoading();

        if (null != mReceiver) {
            getContext().unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        mResult = null;

        super.onReset();
    }

    @Override
    protected void onStartLoading() {
        assertIsMainThread();

        if (null == mReceiver) {
            final IntentFilter filter = new IntentFilter(mPluginRegistry
                    .getChangeIntentAction());
            mReceiver = new RegistryReloadReceiver();

            getContext().registerReceiver(mReceiver, filter, mPluginRegistry.getChangeIntentPermission(), null);
        }

        if (null != mResult) {
            deliverResult(mResult);
        }

        final boolean configChange = mLastConfig.checkNewConfig(getContext().getResources());

        if (takeContentChanged() || null == mResult || configChange) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        assertIsMainThread();

        cancelLoad();
    }

    /**
     * A dynamically registered BroadcastReceiver to receive change
     * notifications from the {@link PluginRegistry}.
     */
    @NotThreadSafe
    private final class RegistryReloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            assertIsMainThread();

            if (BundleScrubber.scrub(intent)) {
                return;
            }

            Lumberjack.v("Received %s", intent); //$NON-NLS-1$

            //noinspection ObjectEquality
            if (this != mReceiver) {
                return;
            }

            onContentChanged();
        }
    }
}
