package com.supenta.flitchio.sdk;

import android.os.Handler;

/**
 * Listener of Flitchio events: button state changed, joystick position changed, status changed
 * (connected/disconnected). A listener must be registered with
 * {@link FlitchioController#onResume(FlitchioListener)} to receive events.
 *
 * @since Flitchio-0.5.0
 */
public interface FlitchioListener {

    /**
     * Called when the state of a button has changed (just pressed, kept pressed or just released).
     * Check {@link ButtonEvent#getAction()} to determine the nature of the event.
     * <p>
     * <strong>Note:</strong> this is called on the thread associated with the {@link Handler}
     * specified when registering the {@link FlitchioListener} or an
     * <strong>arbitrary non-UI thread</strong> if no Handler was specified.
     *
     * @param source The button whose state has changed.
     * @param event  The event corresponding to the button state change.
     * @since Flitchio-0.5.0
     */
    void onFlitchioButtonEvent(InputElement.Button source, ButtonEvent event);

    /**
     * Called when the position of a joystick has changed.
     * <p>
     * <strong>Note:</strong> this is called on the thread associated with the {@link Handler}
     * specified when registering the {@link FlitchioListener} or an
     * <strong>arbitrary non-UI thread</strong> if no Handler was specified.
     *
     * @param source The joystick whose position has changed.
     * @param event  The event corresponding to the joystick position change.
     * @since Flitchio-0.5.0
     */
    void onFlitchioJoystickEvent(InputElement.Joystick source, JoystickEvent event);

    /**
     * Called when the connection status of Flitchio has changed: either a new Flitchio has been
     * detected or the connection has been lost. Also called after you register a new
     * {@link FlitchioListener} to notify you that the binding is effective. <strong>Note:</strong>
     * this is called on the thread associated with the {@link Handler} specified when registering
     * the {@link FlitchioListener} or an <strong>arbitrary non-UI thread</strong> if no Handler
     * was specified.
     *
     * @param isConnected The new connection status.
     * @since Flitchio-0.5.0
     */
    void onFlitchioStatusChanged(boolean isConnected);
}
