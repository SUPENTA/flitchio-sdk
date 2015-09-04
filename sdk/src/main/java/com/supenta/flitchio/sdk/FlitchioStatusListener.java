package com.supenta.flitchio.sdk;

public interface FlitchioStatusListener {

    /**
     * Called when the connection status of Flitchio has changed: either a new Flitchio has been
     * detected or the connection has been lost. Also called after you register a new
     * {@link FlitchioStatusListener} to notify you that the binding is effective. <strong>Note:</strong>
     * this is called on the thread associated with the {@link Handler} specified when registering
     * the {@link FlitchioStatusListener} or an <strong>arbitrary non-UI thread</strong> if no Handler
     * was specified.
     *
     * @param isConnected The new connection status.
     * @since TODO update when we release a new version
     */
    void onFlitchioStatusChanged(boolean isConnected);
}
