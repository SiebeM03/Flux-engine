package me.siebe.flux.test.implementations.input;

import me.siebe.flux.api.input.devices.keyboard.Keyboard;
import me.siebe.flux.api.input.enums.Key;

import java.util.BitSet;

/**
 * Fake keyboard for headless tests. Programmable key state; per-frame flags are cleared on {@link #endFrame()}.
 */
public final class FakeKeyboard implements Keyboard {
    private final BitSet keysDown = new BitSet(Key.values().length);
    private final BitSet keysPressedThisFrame = new BitSet(Key.values().length);
    private final BitSet keysReleasedThisFrame = new BitSet(Key.values().length);
    private final BitSet keysRepeatedThisFrame = new BitSet(Key.values().length);

    @Override
    public boolean isKeyDown(Key key) {
        return keysDown.get(key.ordinal());
    }

    @Override
    public boolean isKeyPressed(Key key) {
        return keysPressedThisFrame.get(key.ordinal());
    }

    @Override
    public boolean isKeyReleased(Key key) {
        return keysReleasedThisFrame.get(key.ordinal());
    }

    @Override
    public boolean isKeyRepeated(Key key) {
        return keysRepeatedThisFrame.get(key.ordinal());
    }

    @Override
    public void endFrame() {
        keysPressedThisFrame.clear();
        keysReleasedThisFrame.clear();
        keysRepeatedThisFrame.clear();
    }

    @Override
    public void beginFrame() {
        // No-op; state is set by test methods
    }

    /** Simulates pressing a key this frame. Key is down and pressed until {@link #endFrame()}. */
    public void press(Key key) {
        if (key == null) return;
        keysDown.set(key.ordinal());
        keysPressedThisFrame.set(key.ordinal());
    }

    /** Simulates releasing a key this frame. Key is up and released until {@link #endFrame()}. */
    public void release(Key key) {
        if (key == null) return;
        keysDown.clear(key.ordinal());
        keysReleasedThisFrame.set(key.ordinal());
    }

    /** Simulates key repeat (e.g. OS repeat while held). */
    public void repeat(Key key) {
        if (key == null) return;
        keysRepeatedThisFrame.set(key.ordinal());
    }
}
