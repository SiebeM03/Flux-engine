package me.siebe.flux.api.input.devices.mouse.actions;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.BooleanInputAction;
import me.siebe.flux.api.input.enums.InputType;
import me.siebe.flux.api.input.enums.Modifier;
import me.siebe.flux.api.input.enums.MouseButton;

public class MouseClickAction extends BooleanInputAction {
    private final MouseButton button;
    private final Modifier[] modifiers;

    public MouseClickAction(MouseButton button, Modifier[] modifiers) {
        this.button = button;
        this.modifiers = modifiers;
    }

    @Override
    public Boolean getValue() {
        return Input.mouse().isButtonDown(button) && Input.keyboard().areModifiersDown(modifiers);
    }

    @Override
    public InputType getTargetDevice() {
        return InputType.KEYBOARD_MOUSE;
    }
}
