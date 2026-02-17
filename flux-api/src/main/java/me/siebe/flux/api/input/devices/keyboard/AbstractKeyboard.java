package me.siebe.flux.api.input.devices.keyboard;

import me.siebe.flux.api.input.devices.keyboard.event.KeyPressEvent;
import me.siebe.flux.api.input.devices.keyboard.event.KeyReleaseEvent;
import me.siebe.flux.api.input.enums.Key;
import me.siebe.flux.api.input.enums.Modifier;
import me.siebe.flux.core.AppContext;

import java.util.BitSet;
import java.util.Set;

/**
 * Base implementation of {@link Keyboard} that tracks key state and posts {@link KeyPressEvent} and
 * {@link KeyReleaseEvent} to the event bus. Backend implementations (e.g. GLFW) should call
 * {@link #onKeyPress}, {@link #onKeyRelease}, and {@link #onKeyRepeat} from their callbacks.
 */
public abstract class AbstractKeyboard implements Keyboard {
    // TODO could potentially be narrowed down to 2 Bitsets: currentKeysPressed & lastKeysPressed
    //  isReleased? -> check if down last frame and not this frame
    //  isPressed? -> check if not down last frame and down this frame
    //  isRepeated? -> check if down on both this and last frame (not sure about this since repeat is OS dependent)
    private final BitSet keysPressedThisFrame;
    private final BitSet keysRepeatedThisFrame;
    private final BitSet keysReleasedThisFrame;
    private final BitSet keysDown;

    protected AbstractKeyboard() {
        int keyCount = Key.values().length;
        this.keysPressedThisFrame = new BitSet(keyCount);
        this.keysRepeatedThisFrame = new BitSet(keyCount);
        this.keysReleasedThisFrame = new BitSet(keyCount);
        this.keysDown = new BitSet(keyCount);
    }


    // =================================================================================================================
    // State getter methods
    // =================================================================================================================
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


    // =================================================================================================================
    // Update methods
    // =================================================================================================================
    /**
     * {@inheritDoc}
     * <p>
     * Clears per-frame key states
     */
    @Override
    public void endFrame() {
        keysReleasedThisFrame.clear();
        keysPressedThisFrame.clear();
        keysRepeatedThisFrame.clear();
    }

    @Override
    public void beginFrame() {
        // Do nothing, keyboard state is changed using glfwPollEvents() which is called right before this method.
        // The keyboard state should already be updated by now
    }


    // =================================================================================================================
    // Input callback handlers
    // =================================================================================================================
    /**
     * Called by the backend when a key is pressed. Updates state and posts {@link KeyPressEvent}.
     *
     * @param key       the key that was pressed
     * @param modifiers active modifier keys at the time of the press
     */
    protected void onKeyPress(Key key, Set<Modifier> modifiers) {
        if (key == null) return;

        keysDown.set(key.ordinal());
        keysPressedThisFrame.set(key.ordinal());

        AppContext.get().getEventBus().post(KeyPressEvent.class, e -> e.set(key, modifiers));
    }

    /**
     * Called by the backend when a key is released. Updates state and posts {@link KeyReleaseEvent}.
     *
     * @param key       the key that was released
     * @param modifiers active modifier keys at the time of the release
     */
    protected void onKeyRelease(Key key, Set<Modifier> modifiers) {
        if (key == null) return;

        keysDown.clear(key.ordinal());
        keysReleasedThisFrame.set(key.ordinal());

        AppContext.get().getEventBus().post(KeyReleaseEvent.class, e -> e.set(key, modifiers));
    }

    /**
     * Called by the backend when a key repeat is received (key held down, OS repeat). Does not post an event.
     *
     * @param key       the key that repeated
     * @param modifiers active modifier keys at the time of the repeat
     */
    protected void onKeyRepeat(Key key, Set<Modifier> modifiers) {
        if (key == null) return;

        keysRepeatedThisFrame.set(key.ordinal());
    }
}
