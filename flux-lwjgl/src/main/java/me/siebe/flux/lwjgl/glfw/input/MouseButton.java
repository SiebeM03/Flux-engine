package me.siebe.flux.lwjgl.glfw.input;

import org.lwjgl.glfw.GLFW;

public enum MouseButton {
    LEFT,
    MIDDLE,
    RIGHT;

    public static MouseButton fromGlfw(int glfwValue) {
        return switch (glfwValue) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> LEFT;
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> RIGHT;
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> MIDDLE;
            default -> null;
        };
    }
}
