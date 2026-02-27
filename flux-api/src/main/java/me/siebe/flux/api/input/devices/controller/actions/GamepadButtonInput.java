package me.siebe.flux.api.input.devices.controller.actions;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.BooleanInputAction;
import me.siebe.flux.api.input.enums.GamepadButton;
import me.siebe.flux.api.input.enums.InputType;

public class GamepadButtonInput extends BooleanInputAction {
    private final GamepadButton button;

    public GamepadButtonInput(GamepadButton button) {
        this.button = button;
    }

    @Override
    public Boolean getValue() {
        return Input.controller().isButtonDown(button);
    }

    @Override
    public InputType getTargetDevice() {
        return InputType.CONTROLLER;
    }
}
