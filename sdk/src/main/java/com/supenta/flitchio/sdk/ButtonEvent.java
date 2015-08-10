package com.supenta.flitchio.sdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object used to report button events. In addition to the standard concept of button press/release,
 * a Flitchio button event provides information about the pressure applied on the button, in the
 * interval [0.0 ; 1.0].
 * <p>
 * Each button press is described by a sequence of button events. A button press starts with a
 * button event with {@link #ACTION_DOWN}. If the button is held sufficiently long, then the initial
 * down is followed by additional button events with {@link #ACTION_MOVE} and a non-zero value for
 * {@link #getRepeatCount()}. Important note: repeated press events for one button will happen 500ms
 * after the original press, and then every 50ms (that's the standard behaviour for gamepads in
 * Android). That means in particular that even though the event does contain information about the
 * current pressure of the button, you won't get a new event every time the pressure changes.
 * The only way to monitor precisely pressure changes on buttons is through polling mode.
 * The last button event is a {@link #ACTION_UP} for the button up.
 * <p>
 * Each event has one source that is one of the buttons defined in {@link InputElement#BUTTONS}.
 *
 * @since Flitchio-0.5.0
 */
public class ButtonEvent extends InputEvent<InputElement.Button> implements Parcelable {
    /**
     * @hide
     */
    public static final Parcelable.Creator<ButtonEvent> CREATOR =
            new Parcelable.Creator<ButtonEvent>() {
                public ButtonEvent createFromParcel(Parcel in) {
                    return new ButtonEvent(in, InputElement.BUTTONS[in.readInt()]);
                }

                public ButtonEvent[] newArray(int size) {
                    return new ButtonEvent[size];
                }
            };

    private final float pressure;
    private final int repeatCount;
    private final long firstDownTime;

    /**
     * Not to be used by 3rd-party developers.
     *
     * @hide
     * @deprecated
     */
    public ButtonEvent(InputElement.Button source, long firstDownTime, long eventTime, int action,
                       float pressure, int repeatCount) {
        super(source, eventTime, action);

        this.pressure = pressure;
        this.repeatCount = repeatCount;
        this.firstDownTime = firstDownTime;
    }

    private ButtonEvent(Parcel in, InputElement.Button source) {
        super(in, source);

        pressure = in.readFloat();
        repeatCount = in.readInt();
        firstDownTime = in.readLong();
    }

    /**
     * Retrieve the pressure of the button in this event.
     *
     * @return A value ranging from 0.0 (if not pressed) to 1.0 (maximum pressure).
     * @since Flitchio-0.5.0
     */
    public float getPressure() {
        return pressure;
    }

    /**
     * Retrieve the repeat count of the button in this event.
     * Pressing the same button for a long time will trigger successive {@link ButtonEvent}s
     * with incremental repeat count.
     *
     * @return The number of times this button has been repeatedly pressed.
     * @since Flitchio-0.5.0
     */
    public final int getRepeatCount() {
        return repeatCount;
    }

    /**
     * Retrieve the time at which the corresponding button was first pressed, in the
     * {@link android.os.SystemClock#uptimeMillis()} time base.
     *
     * @return The time at which the corresponding button was first pressed, in the
     * {@link android.os.SystemClock#uptimeMillis()} time base.
     * @since Flitchio-0.5.0
     */
    public long getFirstDownTime() {
        return firstDownTime;
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

        dest.writeFloat(pressure);
        dest.writeInt(repeatCount);
        dest.writeLong(firstDownTime);
    }

    @Override
    public String toString() {
        return "ButtonEvent "
                + "source=" + source
                + ", eventTime=" + eventTime
                + ", action=" + getAction()
                + ", repeatCount=" + repeatCount
                + ", pressure=" + pressure;
    }
}
