package com.supenta.flitchio.sdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object used to report joystick events.
 * <p>
 * A joystick event is simply an update of the position of the joystick. You can retrieve the
 * position described in this event with {@link #getX()} and {@link #getY()}. Joystick events don't
 * handle the concept of press/release so the action associated with such events is always
 * {@link #ACTION_MOVE}.
 * <p>
 * Each event has one source that is one of the joysticks defined in {@link InputElement#JOYSTICKS}.
 *
 * @since Flitchio-0.5.0
 */
public class JoystickEvent extends InputEvent<InputElement.Joystick> implements Parcelable {
    /**
     * @hide
     */
    public static final Parcelable.Creator<JoystickEvent> CREATOR =
            new Parcelable.Creator<JoystickEvent>() {
                public JoystickEvent createFromParcel(Parcel in) {
                    return new JoystickEvent(in, InputElement.JOYSTICKS[in.readInt()]);
                }

                public JoystickEvent[] newArray(int size) {
                    return new JoystickEvent[size];
                }
            };

    private final float x;
    private final float y;

    /**
     * Not to be used by 3rd-party developers.
     *
     * @hide
     * @deprecated
     */
    public JoystickEvent(InputElement.Joystick source, long eventTime, int action,
                         float x, float y) {
        super(source, eventTime, action);

        this.x = x;
        this.y = y;
    }

    private JoystickEvent(Parcel in, InputElement.Joystick source) {
        super(in, source);

        x = in.readFloat();
        y = in.readFloat();
    }

    /**
     * Retrieve the X position of the joystick in this event.
     *
     * @return A value ranging from -1.0 to 1.0.
     * @since Flitchio-0.5.0
     */
    public float getX() {
        return x;
    }

    /**
     * Retrieve the Y position of the joystick in this event.
     *
     * @return A value ranging from -1.0 to 1.0.
     * @since Flitchio-0.5.0
     */
    public float getY() {
        return y;
    }

    /**
     * Retrieve the angular position of the joystick in this event. The reference direction is
     * horizontal right and the angle is measured clockwise.
     *
     * @return A value in degrees from 0 to 360 (excluded).
     * @since Flitchio-0.5.0
     */
    public float getAngle() {
        float angle = (float) Math.toDegrees(Math.atan2(y, x));
        if (angle < 0f) {
            angle += 360f;
        }

        return angle;
    }

    /**
     * Retrieve the distance between the centre of the joystick and its current position in this
     * event.
     *
     * @return A value ranging from 0.0 to sqrt(2).
     * @since Flitchio-0.5.0
     */
    public float getDistance() {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * @hide
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @hide
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeFloat(x);
        dest.writeFloat(y);
    }


    @Override
    public String toString() {
        return "JoystickEvent "
                + "source=" + source
                + ", eventTime=" + eventTime
                + ", action=" + getAction()
                + ", x=" + x
                + ", y=" + y;
    }
}
