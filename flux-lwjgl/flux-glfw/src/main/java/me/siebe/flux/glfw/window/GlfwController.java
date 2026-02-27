package me.siebe.flux.glfw.window;

import me.siebe.flux.api.input.devices.controller.AbstractController;
import me.siebe.flux.api.input.devices.controller.Controller;
import me.siebe.flux.api.input.enums.GamepadAxis;
import me.siebe.flux.api.input.enums.GamepadButton;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import java.util.BitSet;

/**
 * GLFW-backed {@link Controller} implementation. Polls {@link GLFW#glfwGetGamepadState}
 * each frame for the given joystick ID (e.g. {@link GLFW#GLFW_JOYSTICK_1}).
 * All state logic gets updated inside {@link #beginFrame()}
 */
public class GlfwController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(GlfwController.class, LoggingCategories.INPUT);

    private final int joystickId;
    private final GLFWGamepadState state;
    private final BitSet previousButtons;

    GlfwController(int joystickId) {
        this.joystickId = joystickId;
        this.state = GLFWGamepadState.create();
        this.previousButtons = new BitSet(GamepadButton.values().length);
    }

    @Override
    public void beginFrame() {
        if (!GLFW.glfwGetGamepadState(joystickId, state)) {
            super.beginFrame();
            return;
        }

        for (int i = 0; i < GamepadButton.values().length; i++) {
            boolean nowDown = state.buttons(i) == GLFW.GLFW_PRESS;
            boolean wasDown = previousButtons.get(i);
            GamepadButton button = GamepadButton.values()[i];

            if (nowDown && !wasDown) {
                onButtonPress(button);
            }
            if (!nowDown && wasDown) {
                onButtonRelease(button);
            }
            previousButtons.set(i, nowDown);
        }

        for (int i = 0; i < GamepadAxis.values().length; i++) {
            float axisValue = state.axes(i);
            GamepadAxis gamepadAxis = GamepadAxis.values()[i];
            if (gamepadAxis.isInverse()) axisValue *= -1;
            setAxis(gamepadAxis, axisValue);
        }

        super.beginFrame();
    }

    /** Returns whether this gamepad is currently connected and has a mapping. */
    public boolean isPresent() {
        return GLFW.glfwJoystickIsGamepad(joystickId);
    }
}
