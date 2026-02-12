package me.siebe.flux.api.input;

import me.siebe.flux.api.input.keyboard.Keyboard;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

public class InputController {
    private static final Logger logger = LoggerFactory.getLogger(InputController.class, LoggingCategories.INPUT);

    protected final Keyboard keyboard;
    protected boolean isKeyboardEnabled = true;

    public InputController() {
        this.keyboard = Input.keyboard();

        if (keyboard == null) {
            logger.error("Keyboard for {} controller is null", this.getClass().getName());
        }
    }

    public void enableInput() {
        this.isKeyboardEnabled = true;
    }
}
