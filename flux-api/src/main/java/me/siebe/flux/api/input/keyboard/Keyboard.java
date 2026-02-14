package me.siebe.flux.api.input.keyboard;

import me.siebe.flux.api.input.enums.Key;

/**
 * Read-only view of keyboard state: which keys are held down and per-frame press/release/repeat.
 * <p>
 * Per-frame flags ({@link #isKeyPressed}, {@link #isKeyReleased}, {@link #isKeyRepeated}) are cleared
 * when {@link #nextFrame()} is called, typically via {@link me.siebe.flux.api.input.Input#nextFrame()}.
 */
public interface Keyboard {
    /**
     * Returns whether the given key is currently held down.
     *
     * @param key the key to check
     * @return true if the key is down
     */
    boolean isKeyDown(Key key);

    /**
     * Returns whether the key was pressed this frame (transition from released to pressed).
     * Cleared on {@link #nextFrame()}.
     *
     * @param key the key to check
     * @return true if the key was pressed this frame
     */
    boolean isKeyPressed(Key key);

    /**
     * Returns whether the key was released this frame (transition from pressed to released).
     * Cleared on {@link #nextFrame()}.
     *
     * @param key the key to check
     * @return true if the key was released this frame
     */
    boolean isKeyReleased(Key key);

    /**
     * Returns whether the key generated a repeat event this frame (held down, OS repeat).
     * Cleared on {@link #nextFrame()}.
     *
     * @param key the key to check
     * @return true if the key repeated this frame
     */
    boolean isKeyRepeated(Key key);

    /**
     * Advances internal state to the next frame (clears press/release/repeat flags).
     * Called by {@link me.siebe.flux.api.input.Input#nextFrame()}.
     */
    void nextFrame();
}
