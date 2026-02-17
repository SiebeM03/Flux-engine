package me.siebe.flux.api.input.devices.keyboard.actions;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.AnalogInputAction;
import me.siebe.flux.api.input.enums.InputType;
import me.siebe.flux.api.input.enums.Key;

public class KeyAxisInputAction extends AnalogInputAction {
    private final Key positiveKey;
    private final Key negativeKey;

    public KeyAxisInputAction(Key positiveKey, Key negativeKey) {
        this.positiveKey = positiveKey;
        this.negativeKey = negativeKey;
    }

    @Override
    public float getValue() {
        float value = 0.0f;
        if (Input.keyboard().isKeyDown(positiveKey)) {
            value += 1.0f;
        }
        if (Input.keyboard().isKeyDown(negativeKey)) {
            value -= 1.0f;
        }
        return value;
    }

    @Override
    public InputType getTargetDevice() {
        return InputType.KEYBOARD_MOUSE;
    }
}
