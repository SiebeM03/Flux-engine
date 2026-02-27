package me.siebe.flux.api.input.actions;

import me.siebe.flux.api.input.enums.InputType;

public abstract class InputAction<T> {
    public abstract T getValue();

    public abstract InputType getTargetDevice();
}
