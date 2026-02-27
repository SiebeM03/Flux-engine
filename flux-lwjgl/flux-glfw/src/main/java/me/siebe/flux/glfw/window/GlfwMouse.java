package me.siebe.flux.glfw.window;

import me.siebe.flux.api.input.devices.mouse.AbstractMouse;
import me.siebe.flux.api.input.enums.Modifier;
import me.siebe.flux.api.input.enums.MouseButton;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

public class GlfwMouse extends AbstractMouse {
    private static final Logger logger = LoggerFactory.getLogger(GlfwMouse.class, LoggingCategories.INPUT);

    GlfwMouse(long windowId) {
        glfwSetMouseButtonCallback(windowId, this::mouseButtonCallback);
        glfwSetCursorPosCallback(windowId, this::mousePosCallback);
        glfwSetScrollCallback(windowId, this::scrollCallback);

        double[] mouseX = new double[1];
        double[] mouseY = new double[1];
        glfwGetCursorPos(windowId, mouseX, mouseY);
        setInitialMousePos(mouseX[0], mouseY[0]);
    }

    private void mouseButtonCallback(long window, int button, int action, int mods) {
        MouseButton fluxButton = toMouseButton(button);
        if (fluxButton == null) {
            logger.error("Invalid mouse button received: {}. Make sure it is registered in GlfwMouse::toMouseButton()", button);
            return;
        }
        Set<Modifier> fluxMods = toModifiers(mods);

        logger.trace("ButtonCode: {}, FluxButton: {}, Action: {}, Mods: {}", button, fluxButton, action, fluxMods);

        switch (action) {
            case GLFW_PRESS -> onButtonPress(fluxButton, fluxMods);
            case GLFW_RELEASE -> onButtonRelease(fluxButton, fluxMods);
        }
    }

    private void mousePosCallback(long window, double x, double y) {
        logger.trace("MousePosCallback x: {}, y: {}", x, y);

        onMouseMove(x, y);
    }

    private void scrollCallback(long window, double scrollX, double scrollY) {
        logger.trace("ScrollCallback x: {}, y: {}", scrollX, scrollY);

        onScroll(scrollX, scrollY);
    }

    // TODO duplicate code (GlfwKeyboard)
    private Set<Modifier> toModifiers(int mods) {
        Set<Modifier> modifiers = new HashSet<>();
        if ((mods & GLFW_MOD_SHIFT) != 0) modifiers.add(Modifier.SHIFT);
        if ((mods & GLFW_MOD_CONTROL) != 0) modifiers.add(Modifier.CONTROL);
        if ((mods & GLFW_MOD_ALT) != 0) modifiers.add(Modifier.ALT);
        if ((mods & GLFW_MOD_SUPER) != 0) modifiers.add(Modifier.SUPER);
        if ((mods & GLFW_MOD_CAPS_LOCK) != 0) modifiers.add(Modifier.CAPS_LOCK);
        if ((mods & GLFW_MOD_NUM_LOCK) != 0) modifiers.add(Modifier.NUM_LOCK);
        return modifiers;
    }

    private MouseButton toMouseButton(int button) {
        return switch (button) {
            case GLFW_MOUSE_BUTTON_1 | GLFW_MOUSE_BUTTON_LEFT -> MouseButton.MOUSE_LEFT;
            case GLFW_MOUSE_BUTTON_2 | GLFW_MOUSE_BUTTON_RIGHT -> MouseButton.MOUSE_RIGHT;
            case GLFW_MOUSE_BUTTON_3 | GLFW_MOUSE_BUTTON_MIDDLE -> MouseButton.MOUSE_MIDDLE;
            case GLFW_MOUSE_BUTTON_4 -> MouseButton.MOUSE_BUTTON_4;
            case GLFW_MOUSE_BUTTON_5 -> MouseButton.MOUSE_BUTTON_5;
            case GLFW_MOUSE_BUTTON_6 -> MouseButton.MOUSE_BUTTON_6;
            case GLFW_MOUSE_BUTTON_7 -> MouseButton.MOUSE_BUTTON_7;
            case GLFW_MOUSE_BUTTON_8 -> MouseButton.MOUSE_BUTTON_8;

            default -> null;
        };
    }
}
