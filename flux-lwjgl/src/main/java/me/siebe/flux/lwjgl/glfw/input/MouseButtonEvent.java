package me.siebe.flux.lwjgl.glfw.input;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.traits.Pooled;

public class MouseButtonEvent extends Event implements Pooled {
    private MouseButton button;
    private ButtonAction action;

    public void set(int glfwButton, int glfwAction) {
        button = MouseButton.fromGlfw(glfwButton);
        action = ButtonAction.fromGlfw(glfwAction);
    }

    @Override
    public void reset() {
        button = null;
        action = null;
    }
}
