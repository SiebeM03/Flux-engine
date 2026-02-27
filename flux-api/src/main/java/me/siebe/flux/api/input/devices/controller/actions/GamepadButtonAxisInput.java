package me.siebe.flux.api.input.devices.controller.actions;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.FloatInputAction;
import me.siebe.flux.api.input.enums.GamepadButton;
import me.siebe.flux.api.input.enums.InputType;

public class GamepadButtonAxisInput extends FloatInputAction {
    private GamepadButton negativeButton;
    private GamepadButton positiveButton;

    public GamepadButtonAxisInput(GamepadButton positiveButton, GamepadButton negativeButton) {
        this.positiveButton = positiveButton;
        this.negativeButton = negativeButton;
    }

    @Override
    public Float getValue() {
        float value = 0.0f;
        if (Input.controller().isButtonDown(positiveButton)) {
            value += 1.0f;
        }
        if (Input.controller().isButtonDown(negativeButton)) {
            value -= 1.0f;
        }
        return value;
    }

    @Override
    public InputType getTargetDevice() {
        return InputType.CONTROLLER;
    }
}
