package me.siebe.flux.lwjgl.glfw.time;

import me.siebe.flux.util.time.TimeProvider;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * A {@link TimeProvider} implementation using GLFW's high-precision timer.
 * Uses the same time source as the graphics subsystem, which can improve vsync alignment.
 * <p>
 * GLFW must be initialized (e.g. via window creation) before this provider is used.
 */
public class GlfwTimeProvider implements TimeProvider {
    @Override
    public double getTimeSeconds() {
        return glfwGetTime();
    }
}
