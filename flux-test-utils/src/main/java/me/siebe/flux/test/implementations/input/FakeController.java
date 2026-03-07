package me.siebe.flux.test.implementations.input;

import me.siebe.flux.api.input.devices.controller.Controller;
import me.siebe.flux.api.input.enums.GamepadAxis;
import me.siebe.flux.api.input.enums.GamepadButton;

import java.util.BitSet;

/**
 * Fake gamepad/controller for headless tests. Programmable button and axis state.
 */
public final class FakeController implements Controller {
    private final BitSet buttonsDown = new BitSet(GamepadButton.values().length);
    private final BitSet buttonsPressedThisFrame = new BitSet(GamepadButton.values().length);
    private final BitSet buttonsReleasedThisFrame = new BitSet(GamepadButton.values().length);
    private final float[] axes = new float[GamepadAxis.values().length];

    @Override
    public boolean isButtonDown(GamepadButton button) {
        return buttonsDown.get(button.ordinal());
    }

    @Override
    public boolean isButtonPressed(GamepadButton button) {
        return buttonsPressedThisFrame.get(button.ordinal());
    }

    @Override
    public boolean isButtonReleased(GamepadButton button) {
        return buttonsReleasedThisFrame.get(button.ordinal());
    }

    @Override
    public float getAxis(GamepadAxis axis) {
        return axes[axis.ordinal()];
    }

    @Override
    public void endFrame() {
        buttonsPressedThisFrame.clear();
        buttonsReleasedThisFrame.clear();
    }

    @Override
    public void beginFrame() {
        // No-op
    }

    /** Simulates pressing a button this frame. */
    public void press(GamepadButton button) {
        if (button == null) return;
        buttonsDown.set(button.ordinal());
        buttonsPressedThisFrame.set(button.ordinal());
    }

    /** Simulates releasing a button this frame. */
    public void release(GamepadButton button) {
        if (button == null) return;
        buttonsDown.clear(button.ordinal());
        buttonsReleasedThisFrame.set(button.ordinal());
    }

    /** Sets axis value (e.g. -1 to 1 for sticks). */
    public void setAxis(GamepadAxis axis, float value) {
        if (axis == null) return;
        axes[axis.ordinal()] = value;
    }
}
