package com.supenta.flitchio.sdk;

public class Status {

    /**
     * @since 0.7.0
     */
    public static final int UNKNOWN = -1;
    /**
     * The previous binding between {@link FlitchioController} and the Flitchio Manager app has
     * been terminated properly.
     *
     * @since 0.7.0
     */
    public static final int UNBOUND = 0;
    /**
     * The {@link FlitchioController} is trying to bind to the Flitchio Manager app.
     *
     * @since 0.7.0
     */
    public static final int BINDING = 1;
    /**
     * The {@link FlitchioController} couldn't bind to the Flitchio Manager app, or the previous
     * binding has ended unexpectedly.
     *
     * @see FlitchioController#getFailureReason()
     * @since 0.7.0
     */
    public static final int BINDING_FAILED = 2;
    /**
     * The {@link FlitchioController} has successfully bound to the Flitchio Manager app.
     *
     * @since 0.7.0
     */
    public static final int BOUND = 3;
    /**
     * The {@link FlitchioController} is bound to the Flitchio Manager app and Flitchio is
     * connected to the phone. You can call {@link FlitchioController#obtainSnapshot()} to poll
     * data. You will also start getting event callbacks if you have declared a
     * {@link FlitchioEventListener}.
     *
     * @since 0.7.0
     */
    public static final int CONNECTED = 4;
    /**
     * The {@link FlitchioController} is bound to the Flitchio Manager app and Flitchio is
     * disconnected from the phone.
     *
     * @since 0.7.0
     */
    public static final int DISCONNECTED = 5;

    public final int code;
    public final int failureReason;

    private Status(int code, int failureReason) {
        this.code = code;
        this.failureReason = failureReason;

        if (this.code == BINDING_FAILED && this.failureReason == Failure.NONE) {
            throw new IllegalArgumentException("You need to explain why");
        }
    }

    public static Status withFailure(int failureReason) {
        return new Status(BINDING_FAILED, failureReason);
    }

    public static Status withCode(int statusCode) {
        return new Status(statusCode, Failure.NONE);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Status && ((Status) o).code == code;
    }

    public static class Failure {
        public static final int NONE = -1;
        /**
         * The Flitchio Manager app is not installed on the user's phone,
         * or the version that is installed doesn't support this version of the SDK.
         *
         * @see FlitchioController#getPlayStoreIntentForFlitchioManager
         * @see FlitchioController#getFlitchioManagerVersionCode
         * @see FlitchioController#getVersionCode
         * @since 0.7.0
         */
        public static final int MANAGER_UNUSABLE = 0;
        /**
         * The Flitchio Manager app cannot be reached for unexpected reason.
         *
         * @since 0.7.0
         */
        public static final int SERVICE_UNREACHABLE = 1;
        /**
         * This controller has not been accepted by Flitchio Manager.
         *
         * @since 0.7.0
         */
        public static final int SERVICE_REFUSED_CONNECTION = 2;
        /**
         * The binding with Flitchio Manager ended unexpectedly.
         *
         * @since 0.7.0
         */
        public static final int SERVICE_SHUTDOWN_CONNECTION = 3;
    }
}
