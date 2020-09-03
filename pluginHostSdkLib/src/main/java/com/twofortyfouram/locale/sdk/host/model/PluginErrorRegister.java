package com.twofortyfouram.locale.sdk.host.model;

import com.twofortyfouram.locale.api.Intent;

import androidx.annotation.NonNull;
import net.jcip.annotations.ThreadSafe;

import static com.twofortyfouram.assertion.Assertions.assertNotNull;
import static com.twofortyfouram.log.Lumberjack.formatMessage;

/**
 * Possible errors that may occur during the register phase of interacting with plug-ins.
 */
@ThreadSafe
public enum PluginErrorRegister implements IPluginError {

    @NonNull
    ACTIVITY_REQUIRES_PERMISSION(
            "The Activity requires a permission that is not granted to the host.  To resolve this issue, remove the permission attribute from the Activity's entry in the Android Manifest.",
                    true), //$NON-NLS-1$

    @NonNull
    ACTIVITY_NOT_ENABLED(
            "The Activity is disabled.  To resolve this issue, remove enabled=\"false\" from the Activity element in the Android Manifest.",
                    true), //$NON-NLS-1$

    @NonNull
    ACTIVITY_NOT_EXPORTED(
            "The Activity is not exported.  To resolve this issue, remove exported=\"false\" from the Activity element in the Android Manifest.",
                    true), //$NON-NLS-1$

    @NonNull
    INSTALL_LOCATION_BAD(
            "Plug-ins must be installed on internal storage.  To resolve this issue, set installLocation=\"internalOnly\" in the AndroidManifest",
                    false), //$NON-NLS-1$

    @NonNull
    RECEIVER_REQUIRES_PERMISSION(
            "The BroadcastReceiver requires a permission that is not granted to the host.  To resolve this issue, remove the permission attribute from the BroadcastReceiver's entry in the Android Manifest.",
                    true), //$NON-NLS-1$

    @NonNull
    APPLICATION_NOT_ENABLED(
            "The Application is disabled.  To resolve this issue, remove enabled=\"false\" from the Application element in the Android Manifest.",
                    true), //$NON-NLS-1$

    @NonNull
    RECEIVER_NOT_ENABLED(
            "The BroadcastReceiver is disabled.  To resolve this issue, remove enabled=\"false\" from the BroadcastReceiver element in the Android Manifest.",
                    true), //$NON-NLS-1$

    @NonNull
    RECEIVER_NOT_EXPORTED(
            "The BroadcastReceiver is not exported.  To resolve this issue, remove exported=\"false\" from the BroadcastReceiver element in the Android Manifest.",
                    true), //$NON-NLS-1$

    @NonNull
    RECEIVER_DUPLICATE(
            formatMessage(
            "The plug-in has multiple BroadcastReceivers for the plug-in Intent action.  To resolve this issue, each plug-in must only have a single BroadcastReceiver for %s and/or %s",
            //$NON-NLS-1$
            Intent.ACTION_QUERY_CONDITION,
            Intent.ACTION_FIRE_SETTING), true
            ),

    @NonNull
    MISSING_RECEIVER(
            formatMessage(
            "The plug-in has no BroadcastReceivers for the plug-in Intent action.  To resolve this issue, each plug-in must have a single BroadcastReceiver for %s and/or %s",
            //$NON-NLS-1$
            Intent.ACTION_QUERY_CONDITION,
            Intent.ACTION_FIRE_SETTING), true
            );

    /**
     * A non-localized message describing the error.
     */
    @NonNull
    private final String mDeveloperExplanation;

    /**
     * Indicates whether the error is fatal or is just a warning.
     */
    private final boolean mIsFatal;

    PluginErrorRegister(@NonNull final String developerExplanation, final boolean isFatal) {
        assertNotNull(developerExplanation, "developerExplanation"); //$NON-NLS-1$

        mDeveloperExplanation = developerExplanation;
        mIsFatal = isFatal;
    }


    @Override
    @NonNull
    public String getDeveloperExplanation() {
        return mDeveloperExplanation;
    }

    @Override
    public boolean isFatal() {
        return mIsFatal;
    }
}
