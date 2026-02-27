package me.siebe.flux.api.input.devices.mouse.actions;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.FloatInputAction;
import me.siebe.flux.api.input.enums.Axis2D;
import me.siebe.flux.api.input.enums.InputType;

public class MouseScrollAction extends FloatInputAction {
    private final Axis2D axis;

    public MouseScrollAction(Axis2D axis) {
        this.axis = axis;
    }

    @Override
    public Float getValue() {
        if (axis == Axis2D.X) {
            return Input.mouse().scrollX();
        } else if (axis == Axis2D.Y) {
            return Input.mouse().scrollY();
        }
        return 0.0f;
    }

    @Override
    public InputType getTargetDevice() {
        return InputType.KEYBOARD_MOUSE;
    }
}
