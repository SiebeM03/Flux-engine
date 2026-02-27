package me.siebe.flux.api.input.enums;

import me.siebe.flux.api.input.devices.mouse.AbstractMouse;

public enum GamepadAxis {
    LEFT_X,
    LEFT_Y(true),
    RIGHT_X,
    RIGHT_Y(true),
    LEFT_TRIGGER,
    RIGHT_TRIGGER,
    ;

    /**
     * This boolean specifies if the value from the current axis should be inverted.
     * This is needed since the Y values of a gamepad's joystick are inverse from what a mouse would return.
     * <p>
     * {@link AbstractMouse#normalizedDeltaX()} multiplies the deltaY value with -1 so that up returns a positive value
     * and down returns a negative value. This boolean helps us normalize the inputs to be consistent between devices
     */
    private final boolean inverse;

    GamepadAxis() {
        inverse = false;
    }
    GamepadAxis(boolean inverse) {
        this.inverse = inverse;
    }

    public boolean isInverse() {
        return inverse;
    }
}
