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
     * @see #failureReason
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

    Status(int code) {
        this(code, FailingStatus.REASON_NONE);
    }

    private Status(int code, int failureReason) {
        this.code = code;
        this.failureReason = failureReason;

        if (this.code == BINDING_FAILED && this.failureReason == FailingStatus.REASON_NONE) {
            throw new IllegalArgumentException("Declared a BINDING_FAILED status without providing a failure reason");
        } else if (this.code != BINDING_FAILED && this.failureReason != FailingStatus.REASON_NONE) {
            throw new IllegalArgumentException("Declared status is not BINDING_FAILED but a failure reason has been provided");
        }
    }

    public static class FailingStatus extends Status {
        public static final int REASON_NONE = -1;
        /**
         * The Flitchio Manager app is not installed on the user's phone,
         * or the version that is installed doesn't support this version of the SDK.
         *
         * @see FlitchioController#getPlayStoreIntentForFlitchioManager
         * @see FlitchioController#getFlitchioManagerVersionCode
         * @see FlitchioController#getVersionCode
         * @since 0.7.0
         */
        public static final int REASON_MANAGER_UNUSABLE = 0;
        /**
         * The Flitchio Manager app cannot be reached for unexpected reason.
         *
         * @since 0.7.0
         */
        public static final int REASON_SERVICE_UNREACHABLE = 1;
        /**
         * This controller has not been accepted by Flitchio Manager.
         *
         * @since 0.7.0
         */
        public static final int REASON_SERVICE_REFUSED_CONNECTION = 2;
        /**
         * The binding with Flitchio Manager ended unexpectedly.
         *
         * @since 0.7.0
         */
        public static final int REASON_SERVICE_SHUTDOWN_CONNECTION = 3;

        FailingStatus(int failureReason) {
            super(BINDING_FAILED, failureReason);
        }
    }
}
