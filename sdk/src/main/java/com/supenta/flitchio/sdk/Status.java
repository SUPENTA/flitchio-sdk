package com.supenta.flitchio.sdk;

import android.support.annotation.NonNull;

public enum Status {
    /**
     * @since 0.7.0
     */
    UNKNOWN,
    /**
     * The previous binding between {@link FlitchioController} and the Flitchio Manager app has
     * been terminated properly.
     *
     * @since 0.7.0
     */
    UNBOUND,
    /**
     * The {@link FlitchioController} is trying to bind to the Flitchio Manager app.
     *
     * @since 0.7.0
     */
    BINDING,
    /**
     * The {@link FlitchioController} couldn't bind to the Flitchio Manager app, or the previous
     * binding has ended unexpectedly.
     *
     * @since 0.7.0
     */
    BINDING_FAILED {
        // TODO There might still be a problem if we use two controllers: won't the failure reason be shared?

        private FailureReason failureReason;

        @Override
        void setFailureReason(FailureReason failureReason) {
            if (failureReason == null || failureReason == FailureReason.NONE) {
                throw new IllegalArgumentException("You must provide a reason for the failure, provided reason is " + failureReason);
            }
            this.failureReason = failureReason;
        }

        @NonNull
        @Override
        public FailureReason getFailureReason() {
            return failureReason;
        }
    },
    /**
     * The {@link FlitchioController} has successfully bound to the Flitchio Manager app.
     *
     * @since 0.7.0
     */
    BOUND,
    /**
     * The {@link FlitchioController} is bound to the Flitchio Manager app and Flitchio is
     * connected to the phone. You can call {@link FlitchioController#obtainSnapshot()} to poll
     * data. You will also start getting event callbacks if you have declared a
     * {@link FlitchioEventListener}.
     *
     * @since 0.7.0
     */
    CONNECTED,
    /**
     * The {@link FlitchioController} is bound to the Flitchio Manager app and Flitchio is
     * disconnected from the phone.
     *
     * @since 0.7.0
     */
    DISCONNECTED;

    /**
     * @return The reason of the failed binding.
     * @since 0.7.0
     */
    // Note: this is overridden by BINDING_FAILED
    @NonNull
    public FailureReason getFailureReason() {
        return FailureReason.NONE;
    }

    // Note: this is overridden by BINDING_FAILED
    void setFailureReason(FailureReason failureReason) {
        if (failureReason != null && failureReason != FailureReason.NONE) {
            throw new IllegalArgumentException("Not allowed to report a failure if the state is not " + BINDING_FAILED);
        }
    }

    public enum FailureReason {
        /**
         * No failure has been reported. The status is probably not {@link #BINDING_FAILED}.
         *
         * @since 0.7.0
         */
        NONE,
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
}
