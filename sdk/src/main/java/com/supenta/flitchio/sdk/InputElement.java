package com.supenta.flitchio.sdk;

/**
 * Base class describing the input elements of Flitchio: buttons and joysticks.
 * <p>
 * Flitchio has 2 main buttons ({@link #BUTTON_TOP} and {@link #BUTTON_BOTTOM})
 * and 2 main joysticks ({@link #JOYSTICK_TOP} and {@link #JOYSTICK_BOTTOM}).
 * But this class also describes additional buttons that correspond to the 4 discrete directions
 * (UP, DOWN, LEFT, RIGHT) for each joystick. These {@link InputElement.Button.DpadButton} buttons
 * behave exactly like normal buttons: they have a pressed/released state, and you receive events
 * for them at the same time that you receive events for the corresponding joystick.
 * <p>
 * The elements defined here can be used to identify the buttons and joysticks in a
 * {@link FlitchioSnapshot}, or as source of {@link ButtonEvent}s and {@link JoystickEvent}s.
 *
 * @see InputElement.Button
 * @see InputElement.Joystick
 * @since 0.5.0
 */
public abstract class InputElement {
    /**
     * Button at the top of Flitchio.
     * When holding Flitchio in gaming (landscape) position, it's the button under the right hand.
     *
     * @since 0.5.0
     */
    public static final Button BUTTON_TOP = new Button(0, "BUTTON_TOP");
    /**
     * Button at the bottom of Flitchio.
     * When holding Flitchio in gaming (landscape) position, it's the button under the left hand.
     *
     * @since 0.5.0
     */
    public static final Button BUTTON_BOTTOM = new Button(1, "BUTTON_BOTTOM");

    /**
     * Special button corresponding to the LEFT direction on {@link #JOYSTICK_TOP}.
     *
     * @since 0.5.0
     */
    public static final Button.DpadButton DPAD_TOP_LEFT = new Button.DpadButton(2, "DPAD_TOP_LEFT");
    /**
     * Special button corresponding to the UP direction on {@link #JOYSTICK_TOP}.
     *
     * @since 0.5.0
     */
    public static final Button.DpadButton DPAD_TOP_UP = new Button.DpadButton(3, "DPAD_TOP_UP");
    /**
     * Special button corresponding to the RIGHT direction on {@link #JOYSTICK_TOP}.
     *
     * @since 0.5.0
     */
    public static final Button.DpadButton DPAD_TOP_RIGHT = new Button.DpadButton(4, "DPAD_TOP_RIGHT");
    /**
     * Special button corresponding to the DOWN direction on {@link #JOYSTICK_TOP}.
     *
     * @since 0.5.0
     */
    public static final Button.DpadButton DPAD_TOP_DOWN = new Button.DpadButton(5, "DPAD_TOP_DOWN");
    /**
     * Special button corresponding to the LEFT direction on {@link #JOYSTICK_BOTTOM}.
     *
     * @since 0.5.0
     */
    public static final Button.DpadButton DPAD_BOTTOM_LEFT = new Button.DpadButton(6, "DPAD_BOTTOM_LEFT");
    /**
     * Special button corresponding to the UP direction on {@link #JOYSTICK_BOTTOM}.
     *
     * @since 0.5.0
     */
    public static final Button.DpadButton DPAD_BOTTOM_UP = new Button.DpadButton(7, "DPAD_BOTTOM_UP");
    /**
     * Special button corresponding to the RIGHT direction on {@link #JOYSTICK_BOTTOM}.
     *
     * @since 0.5.0
     */
    public static final Button.DpadButton DPAD_BOTTOM_RIGHT = new Button.DpadButton(8, "DPAD_BOTTOM_RIGHT");
    /**
     * Special button corresponding to the DOWN direction on {@link #JOYSTICK_BOTTOM}.
     *
     * @since 0.5.0
     */
    public static final Button.DpadButton DPAD_BOTTOM_DOWN = new Button.DpadButton(9, "DPAD_BOTTOM_DOWN");

    /**
     * Joystick at the top of Flitchio.
     * When holding Flitchio in gaming (landscape) position, it's the joystick under the right hand.
     *
     * @since 0.5.0
     */
    public static final Joystick JOYSTICK_TOP = new Joystick(0, "JOYSTICK_TOP",
            DPAD_TOP_LEFT, DPAD_TOP_UP, DPAD_TOP_RIGHT, DPAD_TOP_DOWN);
    /**
     * Joystick at the bottom of Flitchio.
     * When holding Flitchio in gaming (landscape) position, it's the joystick under the left hand.
     *
     * @since 0.5.0
     */
    public static final Joystick JOYSTICK_BOTTOM = new Joystick(1, "JOYSTICK_BOTTOM",
            DPAD_BOTTOM_LEFT, DPAD_BOTTOM_UP, DPAD_BOTTOM_RIGHT, DPAD_BOTTOM_DOWN);

    /**
     * Collection of all the {@link InputElement.Button}s of Flitchio.
     *
     * @since 0.5.0
     */
    public static final InputElement.Button[] BUTTONS = {
            BUTTON_TOP, BUTTON_BOTTOM,
            DPAD_TOP_LEFT, DPAD_TOP_UP, DPAD_TOP_RIGHT, DPAD_TOP_DOWN,
            DPAD_BOTTOM_LEFT, DPAD_BOTTOM_UP, DPAD_BOTTOM_RIGHT, DPAD_BOTTOM_DOWN
    };

    /**
     * Collection of all the {@link InputElement.Joystick}s of Flitchio.
     *
     * @since 0.5.0
     */
    public static final InputElement.Joystick[] JOYSTICKS = {
            JOYSTICK_TOP, JOYSTICK_BOTTOM
    };

    /**
     * Unique identifier for this input element.
     *
     * @since 0.5.0
     */
    public final int code;

    /**
     * Name associated with this input element.
     *
     * @since 0.5.0
     */
    public final String name;

    private InputElement(int code, String name) {
        this.code = code; // TODO remove that as code is entirely defined by order in array
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Object identifying a button of Flitchio.
     *
     * @author david.f
     * @see #BUTTONS
     * @since 0.5.0
     */
    public static class Button extends InputElement {
        private Button(int idx, String name) {
            super(idx, name);
        }

        /**
         * Object identifying a special button of Flitchio that is actually
         * one of the four directions of a joystick.
         *
         * @author david.f
         * @see #BUTTONS
         * @since 0.5.0
         */
        public static class DpadButton extends Button {

            private DpadButton(int idx, String name) {
                super(idx, name);
            }
        }
    }

    /**
     * Object identifying a joystick of Flitchio.
     *
     * @author david.f
     * @see #JOYSTICKS
     * @since 0.5.0
     */
    public static class Joystick extends InputElement {
        /**
         * Special button registered as the LEFT direction for this joystick.
         *
         * @since 0.5.0
         */
        public final Button.DpadButton dpadLeftButton;
        /**
         * Special button registered as the UP direction for this joystick.
         *
         * @since 0.5.0
         */
        public final Button.DpadButton dpadUpButton;
        /**
         * Special button registered as the RIGHT direction for this joystick.
         *
         * @since 0.5.0
         */
        public final Button.DpadButton dpadRightButton;
        /**
         * Special button registered as the DOWN direction for this joystick.
         *
         * @since 0.5.0
         */
        public final Button.DpadButton dpadDownButton;

        private Joystick(int idx,
                         String name,
                         Button.DpadButton dpadLeftButton,
                         Button.DpadButton dpadUpButton,
                         Button.DpadButton dpadRightButton,
                         Button.DpadButton dpadDownButton) {
            super(idx, name);

            this.dpadLeftButton = dpadLeftButton;
            this.dpadUpButton = dpadUpButton;
            this.dpadRightButton = dpadRightButton;
            this.dpadDownButton = dpadDownButton;
        }
    }
}
