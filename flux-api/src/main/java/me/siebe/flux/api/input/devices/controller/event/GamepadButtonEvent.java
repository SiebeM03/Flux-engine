package me.siebe.flux.api.input.devices.controller.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.input.enums.GamepadButton;

/**
 * Base for gamepad button events (press, release). Carries the button that triggered the event.
 * Events are pooled; use the getters and do not hold references after handling.
 */
abstract class GamepadButtonEvent extends Event implements Pooled {
    private GamepadButton button;

    /**
     * Initialises this event for dispatch. Called by the input system; do not call from application code.
     */
    public void set(GamepadButton button) {
        this.button = button;
    }

    @Override
    public void reset() {
        this.button = null;
    }

    /** The gamepad button that triggered this event. */
    public GamepadButton getButton() {
        return button;
    }

    /**
     * Returns whether this event was for the given button.
     *
     * @param button the button to test
     * @return true if this event's button equals the given button
     */
    public boolean isButton(GamepadButton button) {
        if (this.button == null) return false;
        return this.button.equals(button);
    }
}
