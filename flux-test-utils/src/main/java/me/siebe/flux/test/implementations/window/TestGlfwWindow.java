package me.siebe.flux.test.implementations.window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

public class TestGlfwWindow {
    private final long handle;

    public TestGlfwWindow() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to init GLFW");
        }

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        handle = GLFW.glfwCreateWindow(1, 1, "", 0, 0);

        if (handle == 0) {
            throw new RuntimeException("Failed to create window");
        }

        GLFW.glfwMakeContextCurrent(handle);
        GL.createCapabilities();
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(handle);
        GLFW.glfwTerminate();
    }
}
