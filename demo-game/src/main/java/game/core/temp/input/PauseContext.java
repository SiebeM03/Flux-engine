package game.core.temp.input;

import me.siebe.flux.api.input.context.InputContext;
import me.siebe.flux.api.input.devices.controller.actions.GamepadAxisInput;
import me.siebe.flux.api.input.devices.controller.actions.GamepadButtonInput;
import me.siebe.flux.api.input.devices.keyboard.actions.KeyInputAction;
import me.siebe.flux.api.input.devices.mouse.actions.MouseMoveAction;
import me.siebe.flux.api.input.enums.GamepadAxis;
import me.siebe.flux.api.input.enums.GamepadButton;
import me.siebe.flux.api.input.enums.Key;

public class PauseContext extends InputContext {
    public static final String CONTEXT_NAME = "pause";
    public static final String UNPAUSE = "unpause";
    public static final String CHANGE_GREEN = "change_green";
    public static final String CHANGE_BLUE = "change_blue";

    PauseContext() {
        super(CONTEXT_NAME);
        bind(UNPAUSE, new KeyInputAction(Key.KEY_ESCAPE));
        bind(UNPAUSE, new GamepadButtonInput(GamepadButton.START));

        bind(CHANGE_GREEN, new MouseMoveAction('x'));
        bind(CHANGE_GREEN, new GamepadAxisInput(GamepadAxis.LEFT_X));
//        bind(CHANGE_BLUE, new MouseScrollAction());
    }

    @Override
    public boolean shouldConsumeInput() {
        return true;
    }
}
