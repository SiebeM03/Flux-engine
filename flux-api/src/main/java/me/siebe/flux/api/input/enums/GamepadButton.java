package me.siebe.flux.api.input.enums;

import me.siebe.flux.api.input.devices.controller.actions.GamepadAxisInput;

public enum GamepadButton {
    A,
    B,
    X,
    Y,
    LEFT_BUMPER,
    RIGHT_BUMPER,
    BACK,
    START,
    GUIDE,
    LEFT_THUMB,
    RIGHT_THUMB,
    DPAD_UP,
    DPAD_RIGHT,
    DPAD_DOWN,
    DPAD_LEFT;

    // Playstation conversions
    public static final GamepadButton PS_CIRCLE = B;
    public static final GamepadButton PS_CROSS = A;
    public static final GamepadButton PS_SQUARE = X;
    public static final GamepadButton PS_TRIANGLE = Y;
}

