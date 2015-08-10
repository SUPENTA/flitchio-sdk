package com.supenta.flitchio.sdk;

import android.os.Parcel;

/**
 * Base class for button and joystick events that are fired in listening mode.
 * <p>
 * Each event has one source that is an {@link InputElement} and one action ({@link #getAction()}).
 *
 * @since Flitchio-0.5.0
 */
public abstract class InputEvent<T extends InputElement> {
    /**
     * Action code for an event whose source is being pressed for the first time.
     * Note: {@link JoystickEvent}s never use this action.
     *
     * @since Flitchio-0.5.0
     */
    public static final int ACTION_DOWN = 0;            // 0000

    /**
     * Action code for an event whose source is being moved or changed.
     * For example: a change in position in a {@link JoystickEvent},
     * or a repeated press in a {@link ButtonEvent}.
     *
     * @since Flitchio-0.5.0
     */
    public static final int ACTION_MOVE = 1;            // 0001

    /**
     * Action code for an event whose source is being released.
     * Note: {@link JoystickEvent}s never use this action.
     *
     * @since Flitchio-0.5.0
     */
    public static final int ACTION_UP = 2;              // 0010

    /**
     * Action code when there is no action. Typically, the input element is not being touched.
     *
     * @since Flitchio-0.5.0
     */
    public static final int ACTION_NONE = 3;            // 0011

    /**
     * Not to be used by 3rd-party developers.
     *
     * @hide
     * @deprecated
     */
    public static final int FLAG_DISPATCH = 4;          // 0100

    /**
     * @hide
     */
    protected final T source;

    /**
     * @hide
     */
    protected final long eventTime;

    /**
     * @hide
     */
    protected final int flaggedAction; // ACTION_* + potential FLAG_DISPATCH

    protected InputEvent(T source, long eventTime, int action) {
        this.source = source;
        this.eventTime = eventTime;
        this.flaggedAction = action;
    }

    protected InputEvent(Parcel in, T source) {
        this.source = source;
        eventTime = in.readLong();
        flaggedAction = in.readInt();
    }

    /**
     * Retrieve the source of this event, that is the {@link InputElement} that caused it.
     *
     * @return The source of this event.
     * @since Flitchio-0.5.0
     */
    public T getSource() {
        return source;
    }

    /**
     * Retrieve the time at which this event occurred, in the
     * {@link android.os.SystemClock#uptimeMillis()} time base.
     *
     * @return The time at which this event occurred, in the
     * {@link android.os.SystemClock#uptimeMillis()} time base.
     * @since Flitchio-0.5.0
     */
    public long getEventTime() {
        return eventTime;
    }

    /**
     * Retrieve the action represented by this event.
     *
     * @return One of {@link #ACTION_DOWN}, {@link #ACTION_MOVE}, {@link #ACTION_UP} or
     * {@link #ACTION_NONE}.
     * @since Flitchio-0.5.0
     */
    public int getAction() {
        return flaggedAction & ~FLAG_DISPATCH;
    }

    /**
     * @hide
     */
    protected void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(source.code);
        dest.writeLong(eventTime);
        dest.writeInt(flaggedAction);
    }

    /**
     * Not to be used by 3rd-party developers.
     *
     * @hide
     * @deprecated
     */
    public boolean shouldBeDispatched() {
        return (flaggedAction & FLAG_DISPATCH) == FLAG_DISPATCH;
    }
}
