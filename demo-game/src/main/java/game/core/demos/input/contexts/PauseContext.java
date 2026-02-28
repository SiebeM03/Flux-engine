package game.core.demos.input.contexts;

import me.siebe.flux.api.input.context.InputContext;
import me.siebe.flux.api.input.devices.controller.actions.GamepadAxisInput;
import me.siebe.flux.api.input.devices.controller.actions.GamepadButtonAxisInput;
import me.siebe.flux.api.input.devices.controller.actions.GamepadButtonInput;
import me.siebe.flux.api.input.devices.keyboard.actions.KeyInputAction;
import me.siebe.flux.api.input.devices.mouse.actions.MouseMoveAction;
import me.siebe.flux.api.input.devices.mouse.actions.MouseScrollAction;
import me.siebe.flux.api.input.enums.Axis2D;
import me.siebe.flux.api.input.enums.GamepadAxis;
import me.siebe.flux.api.input.enums.GamepadButton;
import me.siebe.flux.api.input.enums.Key;

/**
 * Input context defining controls available while the game is paused.
 *
 * <p>
 * This context maps keyboard, mouse, and gamepad inputs to pause-menu related
 * actions. Unlike gameplay contexts, this one consumes input events to prevent
 * them from propagating to other active contexts.
 * </p>
 *
 * <h2>Context Name</h2>
 * <ul>
 *     <li>{@link #CONTEXT_NAME} – Unique identifier used to register and activate this pause context.</li>
 * </ul>
 *
 * <h2>Actions</h2>
 * <ul>
 *     <li>{@link #UNPAUSE} – Leaves the pause state and resumes gameplay.</li>
 *     <li>{@link #CHANGE_GREEN} – Adjusts the green color component (axis-based).</li>
 *     <li>{@link #CHANGE_BLUE} – Adjusts the blue color component (axis-based).</li>
 * </ul>
 *
 * <h2>Keyboard &amp; Mouse Bindings</h2>
 * <ul>
 *     <li><b>Escape</b> – Unpause the game.</li>
 *     <li><b>Mouse X movement</b> – Adjust clear color's green component.</li>
 *     <li><b>Mouse scroll Y</b> – Adjust clear color's blue component.</li>
 * </ul>
 *
 * <h2>Gamepad Bindings</h2>
 * <ul>
 *     <li><b>Start button</b> – Unpause the game.</li>
 *     <li><b>Left Stick X</b> – Adjust clear color's green component.</li>
 *     <li><b>D-Pad Up / Down</b> – Adjust clear color's blue component.</li>
 * </ul>
 *
 * <h2>Input Consumption</h2>
 * <p>
 * {@link #shouldConsumeInput()} returns {@code true}, meaning this context
 * consumes input events and prevents other active contexts from processing
 * them while the game is paused.
 * </p>
 *
 * <p>
 * This ensures that gameplay controls are disabled when the pause context
 * is active.
 * </p>
 *
 * @see InputContext
 */
public class PauseContext extends InputContext {
    public static final String CONTEXT_NAME = "pause";
    public static final String UNPAUSE = "unpause";
    public static final String CHANGE_GREEN = "change_green";
    public static final String CHANGE_BLUE = "change_blue";

    PauseContext() {
        super(CONTEXT_NAME);
        bind(UNPAUSE, new KeyInputAction(Key.KEY_ESCAPE));
        bind(CHANGE_GREEN, new MouseMoveAction(Axis2D.X));
        bind(CHANGE_BLUE, new MouseScrollAction(Axis2D.Y));

        bind(UNPAUSE, new GamepadButtonInput(GamepadButton.START));
        bind(CHANGE_GREEN, new GamepadAxisInput(GamepadAxis.LEFT_X));
        bind(CHANGE_BLUE, new GamepadButtonAxisInput(GamepadButton.DPAD_UP, GamepadButton.DPAD_DOWN));
    }

    @Override
    public boolean shouldConsumeInput() {
        return true;
    }
}
