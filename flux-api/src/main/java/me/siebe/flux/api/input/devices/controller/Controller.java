package me.siebe.flux.api.input.devices.controller;

import me.siebe.flux.api.input.devices.InputDevice;
import me.siebe.flux.api.input.enums.GamepadAxis;
import me.siebe.flux.api.input.enums.GamepadButton;

public interface Controller extends InputDevice {
    boolean isButtonDown(GamepadButton button);

    boolean isButtonPressed(GamepadButton button);

    boolean isButtonReleased(GamepadButton button);

    float getAxis(GamepadAxis axis);
}
