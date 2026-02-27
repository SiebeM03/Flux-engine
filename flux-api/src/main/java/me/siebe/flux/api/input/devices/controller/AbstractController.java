package me.siebe.flux.api.input.devices.controller;


import me.siebe.flux.api.input.devices.controller.event.GamepadButtonPressEvent;
import me.siebe.flux.api.input.devices.controller.event.GamepadButtonReleaseEvent;
import me.siebe.flux.api.input.enums.GamepadAxis;
import me.siebe.flux.api.input.enums.GamepadButton;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.util.ValueUtils;

import java.util.BitSet;

public abstract class AbstractController implements Controller {
    private final BitSet buttonsDown;
    private final BitSet buttonsPressedThisFrame;
    private final BitSet buttonsReleasedThisFrame;
    private final float[] axes;

    protected AbstractController() {
        int buttonCount = GamepadButton.values().length;
        this.buttonsDown = new BitSet(buttonCount);
        this.buttonsPressedThisFrame = new BitSet(buttonCount);
        this.buttonsReleasedThisFrame = new BitSet(buttonCount);
        this.axes = new float[GamepadAxis.values().length];
    }


    // =================================================================================================================
    // State getter methods
    // =================================================================================================================
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


    // =================================================================================================================
    // Update methods
    // =================================================================================================================
    /**
     * {@inheritDoc}
     * <p>
     * Clears per-frame button state.
     */
    @Override
    public void endFrame() {
        buttonsPressedThisFrame.clear();
        buttonsReleasedThisFrame.clear();
    }

    @Override
    public void beginFrame() {
        // Do nothing, controller state will be read and updated inside the subclasses postPoll() method
    }


    // =================================================================================================================
    // Input callback handlers
    // =================================================================================================================
    /**
     * Called by the backend when a gamepad button is pressed. Posts {@link GamepadButtonPressEvent}.
     *
     * @param button the button that was pressed
     */
    protected void onButtonPress(GamepadButton button) {
        if (button == null) return;

        buttonsDown.set(button.ordinal());
        buttonsPressedThisFrame.set(button.ordinal());

        AppContext.get().getEventBus().post(GamepadButtonPressEvent.class, e -> e.set(button));
    }

    /**
     * Called by the backend when a gamepad button is released. Posts {@link GamepadButtonReleaseEvent}.
     *
     * @param button the button that was released
     */
    protected void onButtonRelease(GamepadButton button) {
        if (button == null) return;

        buttonsDown.clear(button.ordinal());
        buttonsReleasedThisFrame.set(button.ordinal());

        AppContext.get().getEventBus().post(GamepadButtonReleaseEvent.class, e -> e.set(button));
    }

    /**
     * Called by the backend when an axis value is read, the value is clamped to (-1..1)
     *
     * @param axis  the axis to update
     * @param value value between -1 and 1
     */
    protected void setAxis(GamepadAxis axis, float value) {
        if (axis == null) return;
        axes[axis.ordinal()] = ValueUtils.clampedValue(value, -1f, 1f);
    }
}
