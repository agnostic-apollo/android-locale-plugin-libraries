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

package com.twofortyfouram.locale.sdk.host.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.twofortyfouram.annotation.Slow;
import com.twofortyfouram.annotation.Slow.Speed;
import com.twofortyfouram.locale.sdk.host.model.Plugin;
import com.twofortyfouram.locale.sdk.host.model.PluginType;
import com.twofortyfouram.log.Lumberjack;
import com.twofortyfouram.spackle.ContextUtil;
import com.twofortyfouram.spackle.ThreadUtil;
import com.twofortyfouram.spackle.ThreadUtil.ThreadPriority;
import com.twofortyfouram.spackle.bundle.BundleScrubber;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.twofortyfouram.assertion.Assertions.assertNotNull;

/**
 * Handler to process loading and monitoring for changes to Locale plug-ins.
 * <p/>
 * After the handler has been initialized, it must be destroyed.
 * <p/>
 * After the Handler is initialized, the public API to retrieve the loaded map
 * of plug-ins is by calling {@link #getConditions()} and {@link #getSettings()}.
 */
public final class PluginRegistryHandler extends Handler {
    /*
     * Design notes: The helper methods such as init(), onPackageAdded(),
     * onPackageChanged(), and onPackageRemoved() only modify the private
     * internal state of this object. The global state of the registry
     * is only modified when messages are processed by the handleMessage()
     * method of the callback. This design allows for easier unit testing of the
     * implementation details of the callback object.
     */

    /**
     * Message to initialize the Handler.
     * <p/>
     * {@link Message#obj} is a {@link CountDownLatch} that will be decremented
     * after loading completes.
     */
    public static final int MESSAGE_INIT = 0;

    /**
     * Message for a package being added.
     * <p/>
     * {@link Message#obj} is a {@code String} representing the package name.
     */
    private static final int MESSAGE_PACKAGE_ADDED = 1;

    /**
     * Message for a package being removed.
     * <p/>
     * {@link Message#obj} is a {@code String} representing the package name.
     */
    private static final int MESSAGE_PACKAGE_REMOVED = 2;

    /**
     * Message for a package changing.
     * <p/>
     * {@link Message#obj} is a {@code String} representing the package name.
     */
    private static final int MESSAGE_PACKAGE_CHANGED = 3;

    /**
     * Empty message to shut down the handler.
     */
    public static final int MESSAGE_DESTROY = 4;

    /**
     * Application context
     */
    @NonNull
    private final Context mContext;

    /**
     * Intent broadcast when the registry changes
     */
    @Nullable
    private final Intent mRegistryReloadedIntent;

    /**
     * Permission for securing {@link #mRegistryReloadedIntent}.
     */
    @Nullable
    private final String mRegistryReloadedPermission;

    /**
     * Map of the registry name to {@link Plugin} for all Conditions.
     * <p/>
     * This field is lazily initialized. This map is mutable.
     */
    @Nullable
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    /* package */ volatile Map<String, Plugin> mMutableConditionMap = null;

    /**
     * Map of the registry name to {@link Plugin} for all Conditions.
     * <p/>
     * This field is lazily initialized. This map is mutable.
     */
    @Nullable
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    /* package */ volatile Map<String, Plugin> mMutableSettingMap = null;

    /**
     * Map of the registry name to {@link Plugin} for all Conditions.
     * <p/>
     * This field is lazily initialized. Once this field is initialized, it will
     * point to an immutable map (e.g. {@link Collections#unmodifiableMap(Map)}.
     */
    @Nullable
    private volatile Map<String, Plugin> mImmutableConditionMap = null;

    /**
     * Map of the registry name to {@link Plugin} for all Settings.
     * <p/>
     * This field is lazily initialized. Once this field is initialized, it will
     * point to an immutable map (e.g. {@link Collections#unmodifiableMap(Map)}.
     */
    @Nullable
    private volatile Map<String, Plugin> mImmutableSettingMap = null;

    /**
     * Handler thread where the BroadcastReceiver runs.
     */
    @Nullable
    private volatile HandlerThread mReceiverHandlerThread = null;

    /**
     * Receiver to detect changes to installed plug-ins.
     * <p/>
     * When non-null, the receiver is registered. When null, no receiver is
     * registered.
     */
    /*
     * It is very important to run the receiver on a separate thread. Because
     * loading the registry can be slow (greater than 10 seconds), it is
     * possible that the PluginRegistryHandler could be blocked for a
     * while. We don't want the BroadcastReceiver to be blocked waiting for the
     * PluginRegistryHandler, as then Android would think the app was not
     * responding.
     */
    @NonNull
    private final BroadcastReceiver mReceiver = new RegistryReceiver();

    /**
     * Construct a new {@link PluginRegistryHandler}.
     *
     * @param looper                 Thread to run the handler on.
     * @param context                Application context.
     * @param notificationAction     Intent action to broadcast when the registry changes.
     * @param notificationPermission Permission to guard {@code notificationAction}.
     */
    public PluginRegistryHandler(@NonNull final Looper looper,
            @NonNull final Context context,
            @NonNull final String notificationAction,
            @NonNull final String notificationPermission) {
        super(looper);
        assertNotNull(context, "context"); //$NON-NLS-1$
        assertNotNull(notificationAction, "notificationAction"); //$NON-NLS-1$
        assertNotNull(notificationPermission, "notificationPermission"); //$NON-NLS-1$

        mContext = ContextUtil.cleanContext(context);

        mRegistryReloadedIntent = new Intent(notificationAction);
        mRegistryReloadedIntent.setPackage(context.getPackageName());
        mRegistryReloadedPermission = notificationPermission;
    }

    /**
     * Constructs a registry with no permission on the broadcast intent.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    /*package*/ PluginRegistryHandler(@NonNull final Looper looper,
            @NonNull final Context context,
            @NonNull final String notificationAction) {
        super(looper);
        assertNotNull(context, "context"); //$NON-NLS-1$
        assertNotNull(notificationAction, "notificationAction"); //$NON-NLS-1$

        mContext = ContextUtil.cleanContext(context);

        mRegistryReloadedIntent = new Intent(notificationAction);
        mRegistryReloadedIntent.setPackage(context.getPackageName());
        mRegistryReloadedPermission = null;
    }

    @Override
    public void handleMessage(@NonNull final Message message) {
        Lumberjack.v("Got what=%s %s", nameThatMessage(message.what), message); //$NON-NLS-1$

        switch (message.what) {
            case MESSAGE_INIT: {
                final CountDownLatch latch = (CountDownLatch) message.obj;

                try {
                    handleInit();

                } finally {
                    latch.countDown();
                }

                break;
            }
            case MESSAGE_PACKAGE_ADDED: {
                final String packageName = (String) message.obj;

                processPackageResult(handlePackageAdded(packageName));

                break;
            }
            case MESSAGE_PACKAGE_CHANGED: {
                final String packageName = (String) message.obj;

                processPackageResult(handlePackageChanged(packageName));

                break;
            }
            case MESSAGE_PACKAGE_REMOVED: {
                final String packageName = (String) message.obj;

                processPackageResult(handlePackageRemoved(packageName));

                break;
            }
            case MESSAGE_DESTROY: {
                handleDestroy();

                break;
            }
            default: {
                throw new AssertionError(
                        Lumberjack.formatMessage("Unrecognized what=%d", message.what));
            }
        }
    }

    /**
     * @param what Message what
     * @return Human-readable string of {@code what}.  Useful for debugging.
     */
    @NonNull
    private static String nameThatMessage(final int what) {
        switch (what) {
            case MESSAGE_INIT: {
                return "MESSAGE_INIT"; //$NON-NLS
            }
            case MESSAGE_PACKAGE_ADDED: {
                return "MESSAGE_PACKAGE_ADDED"; //$NON-NLS
            }
            case MESSAGE_PACKAGE_REMOVED: {
                return "MESSAGE_PACKAGE_REMOVED"; //$NON-NLS
            }
            case MESSAGE_PACKAGE_CHANGED: {
                return "MESSAGE_PACKAGE_CHANGED"; //$NON-NLS
            }
            case MESSAGE_DESTROY: {
                return "MESSAGE_DESTROY"; //$NON-NLS
            }
            default: {
                return "UNKNOWN"; //$NON-NLS
            }
        }
    }

    /**
     * Initially loads the registry.
     *
     * @see #MESSAGE_INIT
     */
    @Slow(Speed.SECONDS)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    /* package */void handleInit() {
        mReceiverHandlerThread = ThreadUtil.newHandlerThread(
                RegistryReceiver.class.getName(),
                ThreadPriority.BACKGROUND);
        final Handler receiverHandler = new Handler(mReceiverHandlerThread.getLooper());
        {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            filter.addDataScheme("package"); //$NON-NLS-1$

            mContext.registerReceiver(mReceiver, filter, null, receiverHandler);
        }

        {
            final IntentFilter externalStorageFilter = new IntentFilter();
            externalStorageFilter
                    .addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            externalStorageFilter
                    .addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mContext.registerReceiver(mReceiver, externalStorageFilter, null,
                    receiverHandler);
        }

        mMutableConditionMap = PluginPackageScanner.loadPluginMap(mContext,
                PluginType.CONDITION, null);
        mMutableSettingMap = PluginPackageScanner.loadPluginMap(mContext,
                PluginType.SETTING, null);

        setConditions();
        setSettings();

        sendBroadcast();
    }

    /**
     * Destroys the registry.
     *
     * @see #MESSAGE_DESTROY
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    /* package */void handleDestroy() {
        mContext.unregisterReceiver(mReceiver);
        mReceiverHandlerThread.quit();
    }

    /**
     * Call when a package is removed to scan and see if the registry should be
     * changed.
     *
     * @param packageName String name of the package that was removed.
     * @return A {@link PackageResult}.
     * @effects if the result is something other than
     * {@link PackageResult#NOTHING_CHANGED}, then the internal state
     * of this callback object was mutated.
     * @see #MESSAGE_PACKAGE_REMOVED
     */
    @NonNull
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    /* package */PackageResult handlePackageRemoved(@NonNull final String packageName) {
        assertNotNull(packageName, "packageName"); //$NON-NLS-1$

        final boolean conditionsChanged = isPluginRemoved(PluginType.CONDITION, packageName,
                PluginPackageScanner.loadPluginMap(mContext, PluginType.CONDITION, packageName));
        final boolean settingsChanged = isPluginRemoved(PluginType.SETTING, packageName,
                PluginPackageScanner.loadPluginMap(mContext, PluginType.SETTING, packageName));

        return PackageResult.get(conditionsChanged, settingsChanged);
    }

    /**
     * Call when a package is added to scan and see if the registry should be
     * changed.
     *
     * @param packageName String name of the package that was added.
     * @return A {@link PackageResult}.
     * @effects if the result is something other than
     * {@link PackageResult#NOTHING_CHANGED}, then the internal state
     * of this callback object was mutated.
     * @see #MESSAGE_PACKAGE_ADDED
     */
    @NonNull
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    /* package */PackageResult handlePackageAdded(@NonNull final String packageName) {
        assertNotNull(packageName, "packageName"); //$NON-NLS-1$

        final boolean conditionsChanged = isPluginAdded(PluginType.CONDITION,
                PluginPackageScanner.loadPluginMap(mContext, PluginType.CONDITION, packageName));
        final boolean settingsChanged = isPluginAdded(PluginType.SETTING,
                PluginPackageScanner.loadPluginMap(mContext, PluginType.SETTING, packageName));

        return PackageResult.get(conditionsChanged, settingsChanged);
    }

    /**
     * Call when a package is changed to scan and see if the registry should be
     * changed.
     *
     * @param packageName String name of the package that was changed.
     * @return A {@link PackageResult}.
     * @effects if the result is something other than
     * {@link PackageResult#NOTHING_CHANGED}, then the internal state
     * of this callback object was mutated.
     * @see #MESSAGE_PACKAGE_CHANGED
     */
    @NonNull
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    /* package */PackageResult handlePackageChanged(@NonNull final String packageName) {
        assertNotNull(packageName, "packageName"); //$NON-NLS-1$

        final Map<String, Plugin> scannedConditions = PluginPackageScanner.loadPluginMap(mContext,
                PluginType.CONDITION, packageName);
        final Map<String, Plugin> scannedSettings = PluginPackageScanner.loadPluginMap(mContext,
                PluginType.SETTING, packageName);

        boolean conditionsChanged = isPluginRemoved(PluginType.CONDITION, packageName,
                scannedConditions)
                || isPluginAdded(PluginType.CONDITION, scannedConditions);
        final boolean settingsChanged =
                isPluginRemoved(PluginType.SETTING, packageName, scannedSettings)
                        || isPluginAdded(PluginType.SETTING, scannedSettings);

        /*
         * Look for updated plug-in conditions so that their processes can be
         * re-launched by a detected registry change. Note that this looks for a
         * versionCode change, so no change will be detected if a package is
         * merely reinstalled. This is only necessary for conditions, because
         * they may need to reschedule alarms or restart services after being
         * upgraded. It is not necessary to relaunch settings, because they do
         * not typically have alarms or services that need to be restarted.
         */
        for (final Plugin newCondition : scannedConditions.values()) {
            if (mImmutableConditionMap.containsKey(newCondition
                    .getRegistryName())) {
                final int oldConditionVersion = mImmutableConditionMap
                        .get(newCondition.getRegistryName()).getVersionCode();
                final int newConditionVersion = newCondition.getVersionCode();
                if (oldConditionVersion != newConditionVersion) {
                    Lumberjack
                            .v("Package %s changed from versionCode=%d to versionCode=%d",
                                    //$NON-NLS-1$
                                    packageName, oldConditionVersion, newConditionVersion);

                    conditionsChanged = true;
                }
            }
        }

        return PackageResult.get(conditionsChanged, settingsChanged);
    }

    /**
     * Scans {@code packageName} for new plug-ins.
     *
     * @param type           Plug-in type.
     * @param scannedPlugins The plug-ins that were scanned.
     * @return True if new plug-ins were added. False if no new plug-ins were
     * added.
     * @effects The mutable map for {@code type} will be mutated if this method
     * returns true.
     */
    private boolean isPluginAdded(@NonNull final PluginType type,
            @NonNull final Map<String, Plugin> scannedPlugins) {
        assertNotNull(type, "type"); //$NON-NLS-1$
        assertNotNull(scannedPlugins, "scannedPlugins"); //$NON-NLS-1$

        final Map<String, Plugin> oldPlugins = getMutablePluginMap(type);

        boolean isChanged = false;
        if (!oldPlugins.keySet().containsAll(scannedPlugins.keySet())) {
            Lumberjack
                    .v("New plug-ins detected: %s", scannedPlugins); //$NON-NLS-1$
            oldPlugins.putAll(scannedPlugins);

            isChanged = true;
        }

        return isChanged;
    }

    /**
     * Scans {@code packageName} for removed plug-ins.
     *
     * @param type           Plug-in type.
     * @param scannedPlugins The plug-ins that were scanned.
     * @return True if plug-ins were removed. False if no plug-ins were removed.
     * @effects The mutable map for {@code type} will be mutated if this method
     * returns true.
     */
    private boolean isPluginRemoved(@NonNull final PluginType type,
            @NonNull final String packageName,
            @NonNull final Map<String, Plugin> scannedPlugins) {
        assertNotNull(type, "type"); //$NON-NLS-1$
        assertNotNull(scannedPlugins, "scannedPlugins"); //$NON-NLS-1$

        boolean isChanged = false;
        final Iterator<Plugin> oldPluginsIterator = getMutablePluginMap(type).values()
                .iterator();
        while (oldPluginsIterator.hasNext()) {
            final Plugin factory = oldPluginsIterator.next();

            if (packageName.equals(factory.getPackageName())) {
                if (!scannedPlugins.containsKey(factory.getRegistryName())) {
                    Lumberjack
                            .v("Removing plug-in %s %s", type,
                                    factory.getRegistryName()); //$NON-NLS-1$

                    oldPluginsIterator.remove();
                    isChanged = true;
                }
            }
        }

        return isChanged;
    }

    @NonNull
    private Map<String, Plugin> getMutablePluginMap(@NonNull final PluginType type) {
        assertNotNull(type, "type"); //$NON-NLS-1$

        final Map<String, Plugin> pluginMap;
        switch (type) {
            case CONDITION: {
                pluginMap = mMutableConditionMap;
                break;
            }
            case SETTING: {
                pluginMap = mMutableSettingMap;
                break;
            }
            default: {
                throw new AssertionError();
            }
        }

        return pluginMap;
    }

    /**
     * Processes a {@link PackageResult} return value.
     *
     * @param result value to process.
     */
    private void processPackageResult(@NonNull final PackageResult result) {
        assertNotNull(result, "result"); //$NON-NLS-1$

        switch (result) {
            case NOTHING_CHANGED: {
                break;
            }
            case CONDITIONS_AND_SETTINGS_CHANGED: {
                setConditions();
                setSettings();

                sendBroadcast();

                break;
            }
            case CONDITIONS_CHANGED: {
                setConditions();

                sendBroadcast();

                break;
            }
            case SETTINGS_CHANGED: {
                setSettings();

                sendBroadcast();
                break;
            }
        }
    }

    /**
     * Sends a broadcast to notify clients that the registry changed.
     */
    private void sendBroadcast() {
        if (null != mRegistryReloadedIntent) {
            mContext.sendBroadcast(mRegistryReloadedIntent, mRegistryReloadedPermission);
        }
    }

    /**
     * Clients MUST NOT attempt to modify the map.
     *
     * @return A snapshot of plug-in conditions. This method will return
     * {@code null} until the handler completes initialization.
     */
    @Nullable
    public Map<String, Plugin> getConditions() {
        return mImmutableConditionMap;
    }

    /**
     * Clients MUST NOT attempt to modify the map.
     *
     * @return A snapshot of plug-in settings. This method will return
     * {@code null} until the handler completes initialization.
     */
    @Nullable
    public Map<String, Plugin> getSettings() {
        return mImmutableSettingMap;
    }

    /**
     * Mutate the global registry state with the latest changes to conditions.
     *
     * @effects {@link #mImmutableConditionMap} will be
     * reassigned to a new object after this method completes.
     */
    private void setConditions() {
        /*
         * In addition to making it immutable, making a copy is also required.
         * Otherwise if a copy weren't made, changes to the
         * mMutableConditionMap would be reflected by the
         * mImmutableConditionMap.
         */
        mImmutableConditionMap = Collections
                .unmodifiableMap(new HashMap<String, Plugin>(
                        mMutableConditionMap));
    }

    /**
     * Mutate the global registry state with the latest changes to settings.
     *
     * @effects {@link #mImmutableSettingMap} will be
     * reassigned to a new object after this method completes.
     */
    private void setSettings() {
        /*
         * In addition to making it immutable, making a copy is also required.
         * Otherwise if a copy weren't made, changes to the
         * mMutableSettingMap would be reflected by the
         * mImmutableSettingMap.
         */
        mImmutableSettingMap = Collections
                .unmodifiableMap(new HashMap<String, Plugin>(
                        mMutableSettingMap));
    }

    /**
     * Dynamically registered BroadcastReceiver to keep a fresh view of what
     * plug-ins are installed.
     * <p/>
     * Expected {@code Intent}s are:
     * <ul>
     * <li>{@link Intent#ACTION_PACKAGE_ADDED}.</li>
     * <li>{@link Intent#ACTION_PACKAGE_REMOVED}.</li>
     * <li>{@link Intent#ACTION_EXTERNAL_APPLICATIONS_AVAILABLE}.</li>
     * <li>{@link Intent#ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE}.</li>
     * <li>{@link Intent#ACTION_PACKAGE_CHANGED}.</li>
     * </ul>
     */
    private final class RegistryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            /*
             * THREADING: This runs on mReceiverHandlerThread.
             */

            if (BundleScrubber.scrub(intent)) {
                return;
            }

            Lumberjack.v("Received %s", intent); //$NON-NLS-1$

            final String action = intent.getAction();

            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                final String changedPackage = getChangedPackage(intent);

                if (!isReplacing(intent)) {
                    sendPackageAddedMessage(changedPackage);
                }
            } else if (Intent.ACTION_PACKAGE_REMOVED
                    .equals(action)) {
                final String changedPackage = getChangedPackage(intent);

                if (!isReplacing(intent)) {
                    sendPackageRemovedMessage(changedPackage);
                }
            } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action) || Intent
                    .ACTION_PACKAGE_CHANGED.equals(action)) {
                final String changedPackage = getChangedPackage(intent);

                sendPackageChangedMessage(changedPackage);
            } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action) || Intent
                    .ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
                final String[] changedPackages = intent
                        .getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);

                if (null != changedPackages) {
                    for (final String packageName : changedPackages) {
                        sendPackageChangedMessage(packageName);
                    }
                }
            }
        }

        /**
         * @param intent A package change Intent.
         * @return True if {@code intent} contains
         * {@link Intent#EXTRA_REPLACING} with a value of true.
         */
        private boolean isReplacing(@NonNull final Intent intent) {
            final boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            return isReplacing;
        }

        @Nullable
        private String getChangedPackage(@NonNull final Intent intent) {
            String changedPackage = null;

            final Uri data = intent.getData();

            if (null != data) {
                changedPackage = data.getSchemeSpecificPart();
            }

            return changedPackage;
        }

        private void sendPackageAddedMessage(@Nullable final String packageName) {
            if (null != packageName) {
                sendMessage(obtainMessage(
                        MESSAGE_PACKAGE_ADDED, packageName));
            }
        }

        private void sendPackageRemovedMessage(@Nullable final String packageName) {
            if (null != packageName) {
                sendMessage(obtainMessage(
                        MESSAGE_PACKAGE_REMOVED, packageName));
            }
        }

        private void sendPackageChangedMessage(@Nullable final String packageName) {
            if (null != packageName) {
                sendMessage(obtainMessage(
                        MESSAGE_PACKAGE_CHANGED, packageName));
            }
        }
    }
}
