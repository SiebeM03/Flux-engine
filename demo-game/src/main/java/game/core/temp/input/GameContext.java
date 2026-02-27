package game.core.temp.input;

import me.siebe.flux.api.input.context.InputContext;
import me.siebe.flux.api.input.devices.controller.actions.GamepadAxisInput;
import me.siebe.flux.api.input.devices.controller.actions.GamepadButtonAxisInput;
import me.siebe.flux.api.input.devices.keyboard.actions.KeyAxisInputAction;
import me.siebe.flux.api.input.devices.mouse.actions.MouseMoveAction;
import me.siebe.flux.api.input.enums.Axis2D;
import me.siebe.flux.api.input.enums.GamepadAxis;
import me.siebe.flux.api.input.enums.GamepadButton;
import me.siebe.flux.api.input.enums.Key;

public class GameContext extends InputContext {
    public static final String CONTEXT_NAME = "game";
    // Action names
    public static final String MOVE_FORWARD = "move_forward";
    public static final String MOVE_RIGHT = "move_right";
    public static final String MOVE_UP = "move_up";
    public static final String LOOK_HORIZONTAL = "look_horizontal";
    public static final String LOOK_VERTICAL = "look_vertical";

    GameContext() {
        super(CONTEXT_NAME);
        bind(MOVE_RIGHT, new KeyAxisInputAction(Key.KEY_D, Key.KEY_A));           // D = right, A = left
        bind(MOVE_FORWARD, new KeyAxisInputAction(Key.KEY_W, Key.KEY_S));         // W = forward, S = backward
        bind(MOVE_UP, new KeyAxisInputAction(Key.KEY_SPACE, Key.KEY_LEFT_SHIFT)); // SPACE = up, SHIFT = down

        bind(LOOK_HORIZONTAL, new MouseMoveAction(Axis2D.X));
        bind(LOOK_VERTICAL, new MouseMoveAction(Axis2D.Y));


        // Controller
        bind(MOVE_RIGHT, new GamepadAxisInput(GamepadAxis.LEFT_X));
        bind(MOVE_FORWARD, new GamepadAxisInput(GamepadAxis.LEFT_Y));
        bind(MOVE_UP, new GamepadButtonAxisInput(GamepadButton.PS_CIRCLE, GamepadButton.PS_CROSS));  // A = up, X = down

        bind(LOOK_HORIZONTAL, new GamepadAxisInput(GamepadAxis.RIGHT_X));
        bind(LOOK_VERTICAL, new GamepadAxisInput(GamepadAxis.RIGHT_Y));
    }

    @Override
    public boolean shouldConsumeInput() {
        return false;
    }
}
