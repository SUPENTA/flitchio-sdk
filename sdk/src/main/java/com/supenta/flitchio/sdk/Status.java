package com.supenta.flitchio.sdk;

/**
 * Object describing the status of a {@link FlitchioController}.
 * You can query a controller for its status with {@link FlitchioController#getStatus()}, and you
 * can get notified of status changes by implementing a {@link FlitchioStatusListener} and passing
 * it to {@link FlitchioController#onCreate(FlitchioStatusListener)}.
 *
 * @since 0.7.0
 */
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
     * A status with this code is a {@link FailingStatus} and it holds a reason for the failure in
     * {@link #failureReason}.
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

    /**
     * Identifier for this status.
     * Its value is one of the {@link Status} constants.
     *
     * @since 0.7.0
     */
    public final int code;

    /**
     * If this status' {@link #code} is {@link #BINDING_FAILED}, this holds the reason of the
     * failure and its value is either one of {@link FailingStatus#REASON_MANAGER_UNUSABLE},
     * {@link FailingStatus#REASON_SERVICE_REFUSED_CONNECTION},
     * {@link FailingStatus#REASON_SERVICE_UNREACHABLE} or
     * {@link FailingStatus#REASON_SERVICE_SHUTDOWN_CONNECTION}.
     * If this status' {@link #code} is not {@link #BINDING_FAILED}, its value is
     * {@link FailingStatus#NOT_FAILED}.
     *
     * @since 0.7.0
     */
    public final int failureReason;

    /**
     * Create a new non-failing status.
     *
     * @param code Identifier for this status: one of the {@link Status} constants.
     */
    Status(int code) {
        this(code, FailingStatus.NOT_FAILED);
    }

    private Status(int code, int failureReason) {
        this.code = code;
        this.failureReason = failureReason;

        if (this.code == BINDING_FAILED && this.failureReason == FailingStatus.NOT_FAILED) {
            throw new IllegalArgumentException("Declared a BINDING_FAILED status without providing a failure reason");
        } else if (this.code != BINDING_FAILED && this.failureReason != FailingStatus.NOT_FAILED) {
            throw new IllegalArgumentException("Declared status is not BINDING_FAILED but a failure reason has been provided");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o != null
                && o instanceof Status
                && code == ((Status) o).code
                && failureReason == ((Status) o).failureReason;
    }

    /**
     * Subclass of {@link Status} describing that a failure has happened.
     * A failing status has a {@link #BINDING_FAILED} code and it holds the reason of the
     * failure in {@link #failureReason}.
     *
     * @since 0.7.0
     */
    public static class FailingStatus extends Status {
        /**
         * Default failure reason when the status is not failing.
         *
         * @since 0.7.0
         */
        public static final int NOT_FAILED = -1;
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
         * The Flitchio Manager app exists but cannot be reached.
         *
         * @since 0.7.0
         */
        public static final int REASON_SERVICE_UNREACHABLE = 1;
        /**
         * This controller has not been accepted by the Flitchio Manager app.
         *
         * @since 0.7.0
         */
        public static final int REASON_SERVICE_REFUSED_CONNECTION = 2;
        /**
         * The binding with the Flitchio Manager app ended unexpectedly.
         *
         * @since 0.7.0
         */
        public static final int REASON_SERVICE_SHUTDOWN_CONNECTION = 3;

        /**
         * Create a new {@link #BINDING_FAILED} status with a failure reason.
         *
         * @param failureReason The failure reason: either one of
         *                      {@link FailingStatus#REASON_MANAGER_UNUSABLE},
         *                      {@link FailingStatus#REASON_SERVICE_REFUSED_CONNECTION},
         *                      {@link FailingStatus#REASON_SERVICE_UNREACHABLE} or
         *                      {@link FailingStatus#REASON_SERVICE_SHUTDOWN_CONNECTION}.
         */
        FailingStatus(int failureReason) {
            super(BINDING_FAILED, failureReason);
        }
    }
}
