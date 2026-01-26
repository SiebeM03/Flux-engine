package me.siebe.flux.lwjgl.glfw.input;

import org.lwjgl.glfw.GLFW;

public enum ButtonAction {
    PRESS,
    RELEASE;

    public static ButtonAction fromGlfw(int glfwValue) {
        return switch (glfwValue) {
            case GLFW.GLFW_RELEASE -> RELEASE;
            case GLFW.GLFW_PRESS -> PRESS;
            default -> null;
        };
    }
}
