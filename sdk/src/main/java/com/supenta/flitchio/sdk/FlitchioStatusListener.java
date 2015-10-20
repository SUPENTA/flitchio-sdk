package com.supenta.flitchio.sdk;


import android.os.Handler;

/**
 * Listener of Flitchio status update events like connection and disconnection. A listener must be
 * registered with
 * {@link FlitchioController#onResume(FlitchioStatusListener, FlitchioEventListener)} to receive
 * events.
 *
 * @since 0.6.0
 */
public interface FlitchioStatusListener {

    /**
     * @since 0.7.0
     */
    int STATUS_UNKNOWN = -1;
    int STATUS_BINDING = 1;
    int STATUS_BINDING_FAILED = 2;
    /**
     * @since 0.7.0
     */
    int STATUS_BOUND = 3;
    /**
     * @since 0.7.0
     */
    int STATUS_UNBOUND = 0;
    /**
     * @since 0.7.0
     */
    int STATUS_CONNECTED = 4;
    /**
     * @since 0.7.0
     */
    int STATUS_DISCONNECTED = 5;

    /**
     * Called when the connection status of Flitchio has changed: either a new Flitchio has been
     * detected or the connection has been lost. Also called after you register a new
     * {@link FlitchioStatusListener} to notify you that the binding is effective. <strong>Note:</strong>
     * this is called on the thread associated with the {@link Handler} specified when registering
     * the {@link FlitchioStatusListener} or an <strong>arbitrary non-UI thread</strong> if no Handler
     * was specified. TODO review this Javadoc
     *
     * @param status The new connection status: one of {@link #STATUS_CONNECTED},
     *               {@link #STATUS_DISCONNECTED} and {@link #STATUS_UNKNOWN}.
     * @since 0.7.0
     */
    void onFlitchioStatusChanged(int status);
}
