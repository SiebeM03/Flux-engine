package me.siebe.flux.api.input;

import me.siebe.flux.api.input.keyboard.Keyboard;
import me.siebe.flux.api.input.mouse.Mouse;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

public class InputController {
    private static final Logger logger = LoggerFactory.getLogger(InputController.class, LoggingCategories.INPUT);

    protected final Mouse mouse;
    protected boolean isMouseEnabled = true;

    protected final Keyboard keyboard;
    protected boolean isKeyboardEnabled = true;

    public InputController() {
        this.mouse = Input.mouse();
        this.keyboard = Input.keyboard();

        if (mouse == null || keyboard == null) {
            logger.error("Keyboard for {} controller is null", this.getClass().getName());
        }
    }

    public void enableInput() {
        this.isMouseEnabled = true;
        this.isKeyboardEnabled = true;
    }

    public void disableInput() {
        this.isMouseEnabled = false;
        this.isKeyboardEnabled = false;
    }
}
