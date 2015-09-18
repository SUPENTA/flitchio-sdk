package com.supenta.flitchio.sdk;

import android.os.Handler;

/**
 * Listener of Flitchio events: button state changed, joystick position changed. A listener must be
 * registered with
 * {@link FlitchioController#onResume(FlitchioStatusListener, FlitchioEventListener)} to receive
 * events.
 *
 * @since 0.6.0
 */
public interface FlitchioEventListener {

    /**
     * Called when the state of a button has changed (just pressed, kept pressed or just released).
     * Check {@link ButtonEvent#getAction()} to determine the nature of the event.
     * <p/>
     * <strong>Note:</strong> this is called on the thread associated with the {@link Handler}
     * specified when registering the {@link FlitchioEventListener} or an
     * <strong>arbitrary non-UI thread</strong> if no Handler was specified.
     *
     * @param source The button whose state has changed.
     * @param event  The event corresponding to the button state change.
     * @since 0.6.0
     */
    void onFlitchioButtonEvent(InputElement.Button source, ButtonEvent event);

    /**
     * Called when the position of a joystick has changed.
     * <p/>
     * <strong>Note:</strong> this is called on the thread associated with the {@link Handler}
     * specified when registering the {@link FlitchioEventListener} or an
     * <strong>arbitrary non-UI thread</strong> if no Handler was specified.
     *
     * @param source The joystick whose position has changed.
     * @param event  The event corresponding to the joystick position change.
     * @since 0.6.0
     */
    void onFlitchioJoystickEvent(InputElement.Joystick source, JoystickEvent event);
}
