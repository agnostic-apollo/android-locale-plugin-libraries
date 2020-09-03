package com.twofortyfouram.locale.sdk.host.model;

import androidx.annotation.NonNull;

/**
 * Common interface for plug-in errors.  An error is a problem that occurs while interacting with
 * the plug-in.
 */
public interface IPluginError {

    /**
     * @return A non-localized error message with an explanation of the error.
     * This is intended to display to plug-in developers via a log
     * message.
     */
    @NonNull
    String getDeveloperExplanation();

    /**
     * @return True if the error is fatal.  If false, the error is non-fatal and should be
     * considered to be a warning.
     */
    boolean isFatal();
}
