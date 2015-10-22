package com.supenta.flitchio.sdk;


/**
 * Listener of {@link FlitchioController} lifecycle events, like binding/unbinding, and
 * connection/disconnection of Flitchio. It must be passed in
 * {@link FlitchioController#onCreate(FlitchioStatusListener)}.
 *
 * @since 0.6.0
 */
public interface FlitchioStatusListener {
    /**
     * Called when the status of a {@link FlitchioController} has changed.
     * It is called at every point of the controller lifecycle:
     * <ul>
     * <li>After you call {@link FlitchioController#onCreate(FlitchioStatusListener)}, you'll
     * get a first {@link Status#BINDING} callback.
     * Then, the next callback will be {@link Status#BOUND} if the binding worked, or
     * {@link Status#BINDING_FAILED} if it failed.</li>
     * <li>After the {@link Status#BOUND} callback, you will get a callback with the actual
     * connection state of Flitchio: either {@link Status#CONNECTED} if Flitchio is connected
     * to the phone, or {@link Status#DISCONNECTED} otherwise.</li>
     * <li>At this point, if you got the {@link Status#CONNECTED} callback, you can start
     * polling data from Flitchio with {@link FlitchioController#obtainSnapshot()}.
     * You will also start getting event callbacks if you have passed a listener to
     * {@link FlitchioController#onResume(FlitchioEventListener)}.</li>
     * <li>At any point, if the binding fails, you will get a {@link Status#BINDING_FAILED}
     * callback.
     * It can happen in the attempt of binding or if the current binding hangs up.
     * You can check the reason of the failure in {@link Status#failureReason}: its value is
     * one of the constants in {@link Status.FailingStatus}.</li>
     * </ul>
     * <strong>To get all the callbacks, it is crucial to respect the lifecycle of the
     * {@link FlitchioController}, with onCreate(), onResume(), onPause() and onDestroy().</strong>
     *
     * @param status The new connection status: one of the {@link Status} constants.
     * @since 0.7.0
     */
    void onFlitchioStatusChanged(Status status);
}
