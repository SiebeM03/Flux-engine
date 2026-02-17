package me.siebe.flux.api.input.actions;

import me.siebe.flux.api.input.enums.InputType;

public abstract class InputAction {
    public abstract InputType getTargetDevice();
}
