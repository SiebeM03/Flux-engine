package me.siebe.flux.test.implementations.input;

import me.siebe.flux.api.input.devices.mouse.Mouse;
import me.siebe.flux.api.input.enums.MouseButton;

import java.util.BitSet;

/**
 * Fake mouse for headless tests. Programmable button state and cursor position; per-frame state cleared on {@link #endFrame()}.
 */
public final class FakeMouse implements Mouse {
    private final BitSet buttonsDown = new BitSet(MouseButton.values().length);
    private final BitSet buttonsPressedThisFrame = new BitSet(MouseButton.values().length);
    private final BitSet buttonsReleasedThisFrame = new BitSet(MouseButton.values().length);

    private float normalizedX = 0.5f;
    private float normalizedY = 0.5f;
    private float screenX = 400f;
    private float screenY = 300f;
    private float normalizedDeltaX;
    private float normalizedDeltaY;
    private float deltaX;
    private float deltaY;
    private float scrollX;
    private float scrollY;

    private float lastNormalizedX = 0.5f;
    private float lastNormalizedY = 0.5f;
    private float lastScreenX = 400f;
    private float lastScreenY = 300f;

    @Override
    public boolean isButtonDown(MouseButton button) {
        return buttonsDown.get(button.ordinal());
    }

    @Override
    public boolean isButtonPressed(MouseButton button) {
        return buttonsPressedThisFrame.get(button.ordinal());
    }

    @Override
    public boolean isButtonReleased(MouseButton button) {
        return buttonsReleasedThisFrame.get(button.ordinal());
    }

    @Override
    public float normalizedX() {
        return normalizedX;
    }

    @Override
    public float normalizedY() {
        return normalizedY;
    }

    @Override
    public float screenX() {
        return screenX;
    }

    @Override
    public float screenY() {
        return screenY;
    }

    @Override
    public float normalizedDeltaX() {
        return normalizedDeltaX;
    }

    @Override
    public float normalizedDeltaY() {
        return normalizedDeltaY;
    }

    @Override
    public float deltaX() {
        return deltaX;
    }

    @Override
    public float deltaY() {
        return deltaY;
    }

    @Override
    public float scrollX() {
        return scrollX;
    }

    @Override
    public float scrollY() {
        return scrollY;
    }

    @Override
    public void endFrame() {
        buttonsPressedThisFrame.clear();
        buttonsReleasedThisFrame.clear();
        normalizedDeltaX = 0;
        normalizedDeltaY = 0;
        deltaX = 0;
        deltaY = 0;
        scrollX = 0;
        scrollY = 0;
    }

    @Override
    public void beginFrame() {
        normalizedDeltaX = normalizedX - lastNormalizedX;
        normalizedDeltaY = normalizedY - lastNormalizedY;
        deltaX = screenX - lastScreenX;
        deltaY = screenY - lastScreenY;
        lastNormalizedX = normalizedX;
        lastNormalizedY = normalizedY;
        lastScreenX = screenX;
        lastScreenY = screenY;
    }

    /** Sets cursor position (normalized 0–1). Deltas are computed on next {@link #beginFrame()}. */
    public void setPosition(float normalizedX, float normalizedY) {
        this.normalizedX = normalizedX;
        this.normalizedY = normalizedY;
    }

    /** Sets cursor position in pixel space. */
    public void setScreenPosition(float screenX, float screenY) {
        this.screenX = screenX;
        this.screenY = screenY;
    }

    /** Simulates pressing a button this frame. */
    public void press(MouseButton button) {
        if (button == null) return;
        buttonsDown.set(button.ordinal());
        buttonsPressedThisFrame.set(button.ordinal());
    }

    /** Simulates releasing a button this frame. */
    public void release(MouseButton button) {
        if (button == null) return;
        buttonsDown.clear(button.ordinal());
        buttonsReleasedThisFrame.set(button.ordinal());
    }

    /** Sets scroll for this frame (cleared in {@link #endFrame()}). */
    public void setScroll(float scrollX, float scrollY) {
        this.scrollX = scrollX;
        this.scrollY = scrollY;
    }
}
