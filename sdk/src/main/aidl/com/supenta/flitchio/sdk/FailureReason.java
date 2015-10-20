package com.supenta.flitchio.sdk;

/**
 * @see FlitchioController#getFailureReason()
 */
public enum FailureReason {
    /**
     * Caused when the Flitchio Manager app is not installed on the user's phone,
     * or if the Flitchio Manager app installed doesn't support this version of the SDK.
     *
     * @see FlitchioController#getPlayStoreIntentForFlitchioManager
     * @see FlitchioController#getFlitchioManagerVersionCode
     * @see FlitchioController#getVersionCode
     */
    MANAGER_UNUSABLE,
    SERVICE_UNREACHABLE,
    SERVICE_REFUSED_CONNECTION,
    SERVICE_SHUTDOWN_CONNECTION
}
