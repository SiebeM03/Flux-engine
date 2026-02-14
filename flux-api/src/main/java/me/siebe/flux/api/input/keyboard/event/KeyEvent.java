package me.siebe.flux.api.input.keyboard.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.input.enums.Key;
import me.siebe.flux.api.input.enums.Modifier;

import java.util.Set;

/**
 * Base for keyboard events (press, release). Carries the key and modifier state at the time of the event.
 * Events are pooled; use the getters and do not hold references after handling.
 */
abstract class KeyEvent extends Event implements Pooled {
    private Key key;
    private Set<Modifier> modifiers;

    /**
     * Initialises this event for dispatch. Called by the input system; do not call from application code.
     */
    public void set(Key key, Set<Modifier> modifiers) {
        this.key = key;
        this.modifiers = modifiers;
    }

    @Override
    public void reset() {
        key = null;
        modifiers = null;
    }

    /** The key that triggered this event. */
    public Key getKey() {
        return key;
    }

    /** Modifier keys (shift, ctrl, etc.) held at the time of the event. */
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    /**
     * Returns whether this event was for the given key.
     *
     * @param key the key to test
     * @return true if this event's key equals the given key
     */
    public boolean isKey(Key key) {
        if (this.key == null) return false;
        return this.key.equals(key);
    }

    /**
     * Returns whether the given modifier was held when this event occurred.
     *
     * @param modifier the modifier to test
     * @return true if the modifier was active
     */
    public boolean hasModifier(Modifier modifier) {
        if (this.modifiers == null) return false;
        return modifiers.contains(modifier);
    }
}
