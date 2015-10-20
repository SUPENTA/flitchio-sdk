package com.supenta.flitchio.sdk;

/**
 * Different reasons of failure that leads to a
 * {@link FlitchioStatusListener#STATUS_BINDING_FAILED} status.
 *
 * @see FlitchioController#getFailureReason()
 * @since 0.7.0
 */
public enum FailureReason {
    /**
     * The Flitchio Manager app is not installed on the user's phone,
     * or the version that is installed doesn't support this version of the SDK.
     *
     * @see FlitchioController#getPlayStoreIntentForFlitchioManager
     * @see FlitchioController#getFlitchioManagerVersionCode
     * @see FlitchioController#getVersionCode
     * @since 0.7.0
     */
    MANAGER_UNUSABLE,
    /**
     * The Flitchio Manager app cannot be reached for unexpected reason.
     *
     * @since 0.7.0
     */
    SERVICE_UNREACHABLE,
    /**
     * This controller has not been accepted by Flitchio Manager.
     *
     * @since 0.7.0
     */
    SERVICE_REFUSED_CONNECTION,
    /**
     * The binding with Flitchio Manager ended unexpectedly.
     *
     * @since 0.7.0
     */
    SERVICE_SHUTDOWN_CONNECTION
}
