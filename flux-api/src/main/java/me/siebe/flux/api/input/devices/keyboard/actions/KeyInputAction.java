package me.siebe.flux.api.input.devices.keyboard.actions;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.BooleanInputAction;
import me.siebe.flux.api.input.enums.InputType;
import me.siebe.flux.api.input.enums.Key;
import me.siebe.flux.api.input.enums.Modifier;

public class KeyInputAction extends BooleanInputAction {
    private final Key key;
    private final Modifier[] modifiers;

    public KeyInputAction(Key key, Modifier... modifiers) {
        this.key = key;
        this.modifiers = modifiers; // Could contain duplicates, might want to fix
    }

    @Override
    public Boolean getValue() {
        return Input.keyboard().isKeyDown(key) && Input.keyboard().areModifiersDown(modifiers);
    }

    @Override
    public InputType getTargetDevice() {
        return InputType.KEYBOARD_MOUSE;
    }
}
