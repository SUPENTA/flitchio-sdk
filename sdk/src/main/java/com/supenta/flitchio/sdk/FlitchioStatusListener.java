package com.supenta.flitchio.sdk;


/**
 * Listener of {@link FlitchioController} lifecycle events, like binding/unbinding, and
 * connection/disconnection of Flitchio. It must be passed with
 * {@link FlitchioController#onCreate(FlitchioStatusListener)}.
 * TODO insist somewhere in Javadoc about the lifecycle and onResume()/onPause() which are capital.
 *
 * @since 0.6.0
 */
public interface FlitchioStatusListener {
    /**
     * @since 0.7.0
     */
    int STATUS_UNKNOWN = -1;
    /**
     * @since 0.7.0
     */
    int STATUS_UNBOUND = 0;
    /**
     * @since 0.7.0
     */
    int STATUS_BINDING = 1;
    /**
     * @see FailureReason
     * @since 0.7.0
     */
    int STATUS_BINDING_FAILED = 2;
    /**
     * @since 0.7.0
     */
    int STATUS_BOUND = 3;
    /**
     * @since 0.7.0
     */
    int STATUS_CONNECTED = 4;
    /**
     * @since 0.7.0
     */
    int STATUS_DISCONNECTED = 5;

    /**
     * Called when the status of a {@link FlitchioController} has changed. It is called at every
     * point of the controller lifecycle:
     * <ul>
     * <li>After you call {@link FlitchioController#onCreate(FlitchioStatusListener)}, you'll
     * get a first {@link #STATUS_BINDING} callback. Then, the next callback will be
     * {@link #STATUS_BOUND} if the binding worked, or {@link #STATUS_BINDING_FAILED} if it
     * failed.</li>
     * <li>After the {@link #STATUS_BOUND} callback, you will get a callback with the actual
     * connection state of Flitchio: either {@link #STATUS_CONNECTED} if Flitchio is connected
     * to the phone, or {@link #STATUS_DISCONNECTED} otherwise.</li>
     * <li>At this point, if you got the {@link #STATUS_CONNECTED} callback, you can start
     * polling data from Flitchio with {@link FlitchioController#obtainSnapshot()}.</li>
     * <li>At any point, if the binding fails, you will get a {@link #STATUS_BINDING_FAILED}
     * callback. It can happen in the attempt of binding or after the binding is complete.
     * You can check the reason of the failure with
     * {@link FlitchioController#getFailureReason()}.</li>
     * </ul>
     * To get all the callbacks, it is crucial to respect the lifecycle of the
     * {@link FlitchioController}, with onCreate(), onResume(), onPause() and onDestroy().
     *
     * @param status The new connection status: one of the constants declared in
     *               {@link FlitchioStatusListener}.
     * @since 0.7.0
     */
    void onFlitchioStatusChanged(int status);
}
