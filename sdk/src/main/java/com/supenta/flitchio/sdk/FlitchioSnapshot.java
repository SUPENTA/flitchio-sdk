package com.supenta.flitchio.sdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object describing a consistent state of Flitchio at a given moment. It behaves partly like an
 * {@link ButtonEvent} and partly like a {@link JoystickEvent}, in the sense that it contains
 * information about all the buttons and all the joysticks at the same time. The snapshot should be
 * retrieved in polling mode, once per update loop iteration. If you don't use polling mode in your
 * app, you should rather consider registering a {@link FlitchioListener} to receive the equivalent
 * {@link ButtonEvent}s and {@link JoystickEvent}s.
 * <p>
 * <h3>Buttons</h3>
 * <p>
 * All the buttons are defined in {@link InputElement#BUTTONS}. For all the methods below, you can
 * identify a button either by passing directly a {@link InputElement.Button} instance or with the
 * integer code of the button.
 * <p>
 * For each button, you can check its state in the current snapshot with
 * {@link #getButtonState(InputElement.Button)}. The possible states are listed in
 * {@link InputElement.Button}. You can also retrieve the current pressure with
 * {@link #getButtonPressure(InputElement.Button)}, in the interval [0.0 ; 1.0] (a pressure value
 * of 0.0 means that the button is not currently pressed).
 * <p>
 * <h3>Joysticks</h3>
 * <p>
 * All the joysticks are defined in {@link InputElement#JOYSTICKS}. For all the methods below, you
 * can identify a joystick either by passing directly a {@link InputElement.Joystick} instance
 * or with the integer code of the joystick.
 * <p>
 * For each joystick, you can access its X and Y coordinates in the current snapshot with
 * {@link #getJoystickX(InputElement.Joystick)} and {@link #getJoystickY(InputElement.Joystick)}.
 * The value retrieved is in the interval [-1.0 ; 1.0].
 *
 * @see ButtonEvent
 * @see JoystickEvent
 * @since 0.5.0
 */
public final class FlitchioSnapshot implements Parcelable {

    // TODO a polling client may miss one of the states if the snapshot is updated faster than the
    // polling process. It can lead to unexpected behaviour, esp. if it's waiting for STATE_PRESSING
    // to take some action (that was the case of AngryBots for instance).

    /**
     * State of a button that is being pressed for the first time.
     *
     * @since 0.5.0
     */
    public static final int STATE_PRESSING = InputEvent.ACTION_DOWN;

    /**
     * State of a button that is being pressed.
     *
     * @since 0.5.0
     */
    public static final int STATE_PRESSED = InputEvent.ACTION_MOVE;

    /**
     * State of a button that is being released.
     *
     * @since 0.5.0
     */
    public static final int STATE_RELEASING = InputEvent.ACTION_UP;

    /**
     * State of a button that is not pressed.
     *
     * @since 0.5.0
     */
    public static final int STATE_RELEASED = InputEvent.ACTION_NONE;

    /**
     * @hide
     */
    public static final Parcelable.Creator<FlitchioSnapshot> CREATOR =
            new Parcelable.Creator<FlitchioSnapshot>() {
                public FlitchioSnapshot createFromParcel(Parcel in) {
                    return new FlitchioSnapshot(in);
                }

                public FlitchioSnapshot[] newArray(int size) {
                    return new FlitchioSnapshot[size];
                }
            };

    private final ButtonEvent[] buttonEvents;
    private final JoystickEvent[] joystickEvents;

    FlitchioSnapshot() {
        this.buttonEvents = new ButtonEvent[InputElement.BUTTONS.length];
        this.joystickEvents = new JoystickEvent[InputElement.JOYSTICKS.length];
    }

    /**
     * Not to be used by 3rd-party developers.
     *
     * @hide
     * @deprecated
     */
    public FlitchioSnapshot(ButtonEvent[] buttonEvents, JoystickEvent[] joystickEvents) {
        /*
         * It's important to have an array of float as argument rather than multiple float
         * arguments (one per button). This way, we ensure backward compatibility with whatever
         * comes from the service. /!\ We don't make a copy of the array here because a new
         * allocation is done every time.
         */
        this.buttonEvents = buttonEvents;
        this.joystickEvents = joystickEvents;
    }

    private FlitchioSnapshot(Parcel in) {
        int buttonEventsInParcel = in.readInt();
        int joystickEventsInParcel = in.readInt();

        if (buttonEventsInParcel < InputElement.BUTTONS.length) {
            throw new RuntimeException("Invalid array length: less buttons than expected");
        }
        if (joystickEventsInParcel < InputElement.JOYSTICKS.length) {
            throw new RuntimeException("Invalid array length: less joysticks than expected");
        }


        buttonEvents = new ButtonEvent[InputElement.BUTTONS.length];
        joystickEvents = new JoystickEvent[InputElement.JOYSTICKS.length];


        // We read manually the Parcel, consuming the extra info sent
        // and not handled by this version of the SDK

        // BUTTONS
        for (int i = 0; i < InputElement.BUTTONS.length; i++) {
            buttonEvents[i] = in.readParcelable(ButtonEvent.class.getClassLoader());
            buttonEventsInParcel--;
        }
        while (buttonEventsInParcel > 0) {
            in.readParcelable(ButtonEvent.class.getClassLoader());
            buttonEventsInParcel--;
        }

        // JOYSTICKS
        for (int i = 0; i < InputElement.JOYSTICKS.length; i++) {
            joystickEvents[i] = in.readParcelable(JoystickEvent.class.getClassLoader());
            joystickEventsInParcel--;
        }
        while (joystickEventsInParcel > 0) {
            in.readParcelable(JoystickEvent.class.getClassLoader());
            joystickEventsInParcel--;
        }
    }

    /**
     * @hide
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(buttonEvents.length);
        out.writeInt(joystickEvents.length);

        for (ButtonEvent buttonEvent : buttonEvents) {
            out.writeParcelable(buttonEvent, flags);
        }
        for (JoystickEvent joystickEvent : joystickEvents) {
            out.writeParcelable(joystickEvent, flags);
        }
    }

    /**
     * @hide
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Retrieve the pressure of the given button.
     *
     * @return A value ranging from 0.0 (if not pressed) to 1.0 (maximum pressure).
     * @see #getButtonPressure(InputElement.Button)
     * @since 0.5.0
     */
    // !!! KEEP THE JAVADOC SYNCED WITH ButtonEvent !!!
    public final float getButtonPressure(int buttonCode) {
        if (buttonEvents[buttonCode] != null) {
            return buttonEvents[buttonCode].getPressure();
        } else {
            return 0.0f;
        }
    }

    /**
     * Retrieve the pressure of the given button.
     *
     * @return A value ranging from 0.0 (if not pressed) to 1.0 (maximum pressure).
     * @see #getButtonPressure(int)
     * @since 0.5.0
     */
    public final float getButtonPressure(InputElement.Button button) {
        return getButtonPressure(button.code);
    }

    /**
     * Retrieve the state of the given button.
     *
     * @return One of {@link #STATE_PRESSING}, {@link #STATE_PRESSED}, {@link #STATE_RELEASING} or
     * {@link #STATE_RELEASED}.
     * @see #getButtonState(InputElement.Button)
     * @since 0.5.0
     */
    public final int getButtonState(int buttonCode) {
        if (buttonEvents[buttonCode] != null) {
            return buttonEvents[buttonCode].getAction(); // Action will be translated to State
        } else {
            return STATE_RELEASED;
        }
    }

    /**
     * Retrieve the state of the given button.
     *
     * @return One of {@link #STATE_PRESSING}, {@link #STATE_PRESSED}, {@link #STATE_RELEASING} or
     * {@link #STATE_RELEASED}.
     * @see #getButtonState(int)
     * @since 0.5.0
     */
    public final float getButtonState(InputElement.Button button) {
        return getButtonState(button.code);
    }

    /**
     * Retrieve the X position of the given joystick.
     *
     * @return A value ranging from -1.0 to 1.0.
     * @see #getJoystickX(InputElement.Joystick)
     * @since 0.5.0
     */
    public final float getJoystickX(int joystickCode) {
        if (joystickEvents[joystickCode] != null) {
            return joystickEvents[joystickCode].getX();
        } else {
            return 0.0f;
        }
    }

    /**
     * Retrieve the X position of the given joystick.
     *
     * @return A value ranging from -1.0 to 1.0.
     * @see #getJoystickX(int)
     * @since 0.5.0
     */
    public final float getJoystickX(InputElement.Joystick joystick) {
        return getJoystickX(joystick.code);
    }

    /**
     * Retrieve the Y position of the given joystick.
     *
     * @return A value ranging from -1.0 to 1.0.
     * @see #getJoystickY(InputElement.Joystick)
     * @since 0.5.0
     */
    public final float getJoystickY(int joystickCode) {
        if (joystickEvents[joystickCode] != null) {
            return joystickEvents[joystickCode].getY();
        } else {
            return 0.0f;
        }
    }

    /**
     * Retrieve the Y position of the given joystick.
     *
     * @return A value ranging from -1.0 to 1.0.
     * @see #getJoystickY(int)
     * @since 0.5.0
     */
    public final float getJoystickY(InputElement.Joystick joystick) {
        return getJoystickY(joystick.code);
    }

    /**
     * Retrieve the angular position of the given joystick. The reference direction is
     * horizontal right and the angle is measured clockwise.
     *
     * @return A value in degrees from 0 to 360 (excluded).
     * @see #getJoystickAngle(InputElement.Joystick)
     * @since 0.5.0
     */
    public final float getJoystickAngle(int joystickCode) {
        if (joystickEvents[joystickCode] != null) {
            return joystickEvents[joystickCode].getAngle();
        } else {
            return 0.0f;
        }
    }

    /**
     * Retrieve the angular position of the given joystick. The reference direction is
     * horizontal right and the angle is measured clockwise.
     *
     * @return A value in degrees from 0 to 360 (excluded).
     * @see #getJoystickAngle(int)
     * @since 0.5.0
     */
    public final float getJoystickAngle(InputElement.Joystick joystick) {
        return getJoystickAngle(joystick.code);
    }

    /**
     * Retrieve the distance between the centre of the given joystick and its current position.
     *
     * @return A value ranging from 0.0 to sqrt(2).
     * @see #getJoystickDistance(InputElement.Joystick)
     * @since 0.5.0
     */
    public final float getJoystickDistance(int joystickCode) {
        if (joystickEvents[joystickCode] != null) {
            return joystickEvents[joystickCode].getDistance();
        } else {
            return 0.0f;
        }
    }

    /**
     * Retrieve the distance between the centre of the given joystick and its current position.
     *
     * @return A value ranging from 0.0 to sqrt(2).
     * @see #getJoystickDistance(int)
     * @since 0.5.0
     */
    public final float getJoystickDistance(InputElement.Joystick joystick) {
        return getJoystickDistance(joystick.code);
    }
}
