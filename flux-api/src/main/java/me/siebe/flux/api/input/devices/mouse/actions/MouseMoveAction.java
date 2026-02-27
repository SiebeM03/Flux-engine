package me.siebe.flux.api.input.devices.mouse.actions;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.FloatInputAction;
import me.siebe.flux.api.input.enums.InputType;

public class MouseMoveAction extends FloatInputAction {
    private final char axis;

    public MouseMoveAction(char axis) {
        this.axis = axis;
    }

    @Override
    public Float getValue() {
        if (axis == 'x') {
            return Input.mouse().deltaX();
        } else if (axis == 'y') {
            return Input.mouse().deltaY();
        }
        return 0.0f;
    }

    @Override
    public InputType getTargetDevice() {
        return InputType.KEYBOARD_MOUSE;
    }
}
