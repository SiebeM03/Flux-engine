package me.siebe.flux.api.input.devices.controller.actions;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.DigitalInputAction;
import me.siebe.flux.api.input.enums.GamepadButton;
import me.siebe.flux.api.input.enums.InputType;

public class GamepadButtonInput extends DigitalInputAction {
    private final GamepadButton button;

    public GamepadButtonInput(GamepadButton button) {
        this.button = button;
    }

    @Override
    public boolean isActive() {
        return Input.controller().isButtonDown(button);
    }

    @Override
    public InputType getTargetDevice() {
        return InputType.CONTROLLER;
    }
}
