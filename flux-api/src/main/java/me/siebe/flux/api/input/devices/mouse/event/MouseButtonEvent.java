package me.siebe.flux.api.input.devices.mouse.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.input.enums.Modifier;
import me.siebe.flux.api.input.enums.MouseButton;

import java.util.Set;

/**
 * Base for mouse button events (click, release, double-click). Carries the button, modifier keys,
 * and cursor position (normalized 0..1) at the time of the event. Events are pooled; use the getters
 * and do not hold references after handling.
 */
abstract class MouseButtonEvent extends Event implements Pooled {
    private MouseButton button;
    private Set<Modifier> modifiers;
    private float x;
    private float y;

    /**
     * Initialises this event for dispatch. Called by the input system; do not call from application code.
     */
    public void set(MouseButton button, Set<Modifier> modifiers, float x, float y) {
        this.button = button;
        this.modifiers = modifiers;
        this.x = x;
        this.y = y;
    }

    @Override
    public void reset() {
        this.button = null;
        this.modifiers = null;
        this.x = 0;
        this.y = 0;
    }

    /** The mouse button that triggered this event. */
    public MouseButton getButton() {
        return button;
    }

    /** Modifier keys (shift, ctrl, etc.) held at the time of the event. */
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    /** Cursor X at event time, normalized 0 (left) to 1 (right). */
    public float getX() {
        return x;
    }

    /** Cursor Y at event time, normalized 0 (top) to 1 (bottom). */
    public float getY() {
        return y;
    }

    /**
     * Returns whether this event was for the given button.
     *
     * @param button the button to test
     * @return true if this event's button equals the given button
     */
    public boolean isButton(MouseButton button) {
        if (this.button == null) return false;
        return this.button.equals(button);
    }

    /**
     * Returns whether the given modifier was held when this event occurred.
     *
     * @param modifier the modifier to test
     * @return true if the modifier was active
     */
    public boolean hasModifier(Modifier modifier) {
        if (this.modifiers == null) return false;
        return this.modifiers.contains(modifier);
    }
}
