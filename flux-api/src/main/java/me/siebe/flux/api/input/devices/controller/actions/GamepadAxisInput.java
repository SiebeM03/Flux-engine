package me.siebe.flux.api.input.devices.controller.actions;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.AnalogInputAction;
import me.siebe.flux.api.input.enums.GamepadAxis;
import me.siebe.flux.api.input.enums.InputType;

public class GamepadAxisInput extends AnalogInputAction {
    private final GamepadAxis axis;

    public GamepadAxisInput(GamepadAxis axis) {
        this.axis = axis;
    }

    @Override
    public float getValue() {
        float value = Input.controller().getAxis(axis);
        if (Math.abs(value) < 0.06f) return 0.0f;
        return value;
    }

    @Override
    public InputType getTargetDevice() {
        return InputType.CONTROLLER;
    }
}
