package me.siebe.flux.api.input;

import me.siebe.flux.api.input.keyboard.Keyboard;
import me.siebe.flux.api.input.mouse.Mouse;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

/**
 * Base class for components that consume keyboard and mouse input.
 * <p>
 * Subclass this to implement camera controllers, UI handlers, or other input-driven behaviour.
 * The controller holds references to {@link Input#mouse()} and {@link Input#keyboard()} and can
 * enable/disable input consumption via {@link #enableInput()} and {@link #disableInput()}.
 */
public class InputController {
    private static final Logger logger = LoggerFactory.getLogger(InputController.class, LoggingCategories.INPUT);

    /** Mouse instance from {@link Input}; used by subclasses to read state or react to events. */
    protected final Mouse mouse;
    /** When false, subclasses should ignore mouse input. */
    protected boolean isMouseEnabled = true;

    /** Keyboard instance from {@link Input}; used by subclasses to read state or react to events. */
    protected final Keyboard keyboard;
    /** When false, subclasses should ignore keyboard input. */
    protected boolean isKeyboardEnabled = true;

    /**
     * Creates a controller that uses the current {@link Input#mouse()} and {@link Input#keyboard()}.
     * Assumes {@link Input#init(Mouse, Keyboard)} has already been called.
     */
    public InputController() {
        this.mouse = Input.mouse();
        this.keyboard = Input.keyboard();

        if (mouse == null || keyboard == null) {
            logger.error("Keyboard for {} controller is null", this.getClass().getName());
        }
    }

    /**
     * Enables mouse and keyboard input for this controller. Subclasses should respect {@link #isMouseEnabled}
     * and {@link #isKeyboardEnabled} when processing input.
     */
    public void enableInput() {
        this.isMouseEnabled = true;
        this.isKeyboardEnabled = true;
    }

    /**
     * Disables mouse and keyboard input for this controller. Subclasses should skip processing when disabled.
     */
    public void disableInput() {
        this.isMouseEnabled = false;
        this.isKeyboardEnabled = false;
    }
}
