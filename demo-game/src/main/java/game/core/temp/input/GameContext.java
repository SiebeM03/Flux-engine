package game.core.temp.input;

import me.siebe.flux.api.input.context.InputContext;
import me.siebe.flux.api.input.devices.controller.actions.GamepadAxisInput;
import me.siebe.flux.api.input.devices.controller.actions.GamepadButtonAxisInput;
import me.siebe.flux.api.input.devices.keyboard.actions.KeyAxisInputAction;
import me.siebe.flux.api.input.devices.mouse.actions.MouseMoveAction;
import me.siebe.flux.api.input.enums.GamepadAxis;
import me.siebe.flux.api.input.enums.GamepadButton;
import me.siebe.flux.api.input.enums.Key;

public class GameContext extends InputContext {
    public static final String CONTEXT_NAME = "game";
    public static final String MOVE_FORWARD = "move_forward";
    public static final String MOVE_BACKWARD = "move_backward";
    public static final String MOVE_LEFT = "move_left";
    public static final String MOVE_RIGHT = "move_right";
    public static final String MOVE_UP = "move_up";
    public static final String MOVE_DOWN = "move_down";
    public static final String LOOK_AROUND = "look_around";

    GameContext() {
        super(CONTEXT_NAME);
        bind("move_right", new KeyAxisInputAction(Key.KEY_D, Key.KEY_A));           // D = right, A = left
        bind("move_forward", new KeyAxisInputAction(Key.KEY_W, Key.KEY_S));         // W = forward, S = backward
        bind("move_up", new KeyAxisInputAction(Key.KEY_SPACE, Key.KEY_LEFT_SHIFT)); // SPACE = up, SHIFT = down

        bind("look_horizontal", new MouseMoveAction('x'));
        bind("look_vertical", new MouseMoveAction('y'));


        // Controller
        bind("move_right", new GamepadAxisInput(GamepadAxis.LEFT_X));
        bind("move_forward", new GamepadAxisInput(GamepadAxis.LEFT_Y));
        bind("move_up", new GamepadButtonAxisInput(GamepadButton.PS_CIRCLE, GamepadButton.PS_CROSS));  // A = up, X = down

        bind("look_horizontal", new GamepadAxisInput(GamepadAxis.RIGHT_X));
        bind("look_vertical", new GamepadAxisInput(GamepadAxis.RIGHT_Y));
    }

    @Override
    public boolean shouldConsumeInput() {
        return false;
    }
}
