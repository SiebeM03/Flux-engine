package game.core.demos.input.contexts;

import me.siebe.flux.api.input.context.InputContext;
import me.siebe.flux.api.input.devices.controller.actions.GamepadAxisInput;
import me.siebe.flux.api.input.devices.controller.actions.GamepadButtonAxisInput;
import me.siebe.flux.api.input.devices.keyboard.actions.KeyAxisInputAction;
import me.siebe.flux.api.input.devices.mouse.actions.MouseMoveAction;
import me.siebe.flux.api.input.enums.Axis2D;
import me.siebe.flux.api.input.enums.GamepadAxis;
import me.siebe.flux.api.input.enums.GamepadButton;
import me.siebe.flux.api.input.enums.Key;

/**
 * Input context defining gameplay-related input bindings for movement and camera control.
 *
 * <p>
 * This context groups together all input mappings required for a typical
 * free-look 3D game setup, including keyboard, mouse, and gamepad bindings.
 * It exposes logical action names that can be queried through the input
 * system, independent of the actual hardware device.
 * </p>
 *
 * <h2>Context Name</h2>
 * <ul>
 *     <li>{@link #CONTEXT_NAME} – The unique identifier used to register and activate this context.</li>
 * </ul>
 *
 * <h2>Actions</h2>
 * <ul>
 *     <li>{@link #MOVE_FORWARD} – Forward/backward movement axis.</li>
 *     <li>{@link #MOVE_RIGHT} – Right/left (strafe) movement axis.</li>
 *     <li>{@link #MOVE_UP} – Vertical movement axis.</li>
 *     <li>{@link #LOOK_HORIZONTAL} – Horizontal camera rotation axis (yaw).</li>
 *     <li>{@link #LOOK_VERTICAL} – Vertical camera rotation axis (pitch).</li>
 * </ul>
 *
 * <h2>Keyboard &amp; Mouse Bindings</h2>
 * <ul>
 *     <li><b>W / S</b> – Move forward / backward.</li>
 *     <li><b>D / A</b> – Move right / left.</li>
 *     <li><b>Space / Left Shift</b> – Move up / down.</li>
 *     <li><b>Mouse X</b> – Horizontal look.</li>
 *     <li><b>Mouse Y</b> – Vertical look.</li>
 * </ul>
 *
 * <h2>Gamepad Bindings</h2>
 * <ul>
 *     <li><b>Left Stick (X/Y)</b> – Horizontal and forward movement.</li>
 *     <li><b>Right Stick (X/Y)</b> – Camera look control.</li>
 *     <li><b>Circle / Cross</b> – Vertical movement (up/down).</li>
 * </ul>
 *
 * <h2>Input Consumption</h2>
 * <p>
 * {@link #shouldConsumeInput()} returns {@code false}, meaning this context
 * does not prevent other active input contexts from also receiving input events.
 * </p>
 *
 * <p>
 * This class centralizes gameplay input configuration and allows device-agnostic
 * querying of movement and camera control actions.
 * </p>
 *
 * @see InputContext
 */
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
