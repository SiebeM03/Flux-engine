package me.siebe.flux.api.input.devices.keyboard;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.devices.InputDevice;
import me.siebe.flux.api.input.enums.Key;
import me.siebe.flux.api.input.enums.Modifier;

/**
 * Read-only view of keyboard state: which keys are held down and per-frame press/release/repeat.
 * <p>
 * Per-frame flags ({@link #isKeyPressed}, {@link #isKeyReleased}, {@link #isKeyRepeated}) are cleared
 * when {@link #endFrame()} is called, typically via {@link Input#endFrame()}.
 */
public interface Keyboard extends InputDevice {
    /**
     * Returns whether the given key is currently held down.
     *
     * @param key the key to check
     * @return true if the key is down
     */
    boolean isKeyDown(Key key);

    /**
     * Returns whether the key was pressed this frame (transition from released to pressed).
     * Cleared on {@link #endFrame()}.
     *
     * @param key the key to check
     * @return true if the key was pressed this frame
     */
    boolean isKeyPressed(Key key);

    /**
     * Returns whether the key was released this frame (transition from pressed to released).
     * Cleared on {@link #endFrame()}.
     *
     * @param key the key to check
     * @return true if the key was released this frame
     */
    boolean isKeyReleased(Key key);

    /**
     * Returns whether the key generated a repeat event this frame (held down, OS repeat).
     * Cleared on {@link #endFrame()}.
     *
     * @param key the key to check
     * @return true if the key repeated this frame
     */
    boolean isKeyRepeated(Key key);

    /**
     * Returns whether the specified modifier is currently held down.
     *
     * @param modifier the modifier to check
     * @return true if the modifier is down
     */
    default boolean isModifierDown(Modifier modifier) {
        for (Key key : modifier.getKeys()) {
            if (isKeyDown(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the specified modifiers are currently held down.
     *
     * @param modifiers the modifiers to check
     * @return true if all specified modifiers are currently down
     */
    default boolean areModifiersDown(Modifier... modifiers) {
        for (Modifier modifier : modifiers) {
            if (!isModifierDown(modifier)) {
                return false;
            }
        }
        return true;
    }
}
