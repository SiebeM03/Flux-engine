package me.siebe.flux.glfw.window;


import me.siebe.flux.util.memory.EngineResources;
import org.lwjgl.glfw.*;

import java.io.PrintStream;

class GlfwCallbacks {

    public static GLFWWindowSizeCallback windowSize(GLFWWindowSizeCallbackI cb) {
        GLFWWindowSizeCallback callback = GLFWWindowSizeCallback.create(cb);
        EngineResources.register(callback);
        return callback;
    }

    public static GLFWFramebufferSizeCallback framebufferSize(GLFWFramebufferSizeCallbackI cb) {
        GLFWFramebufferSizeCallback callback = GLFWFramebufferSizeCallback.create(cb);
        EngineResources.register(callback);
        return callback;
    }

    public static GLFWWindowCloseCallback windowClose(GLFWWindowCloseCallbackI cb) {
        GLFWWindowCloseCallback callback = GLFWWindowCloseCallback.create(cb);
        EngineResources.register(callback);
        return callback;
    }

    public static GLFWKeyCallback key(GLFWKeyCallbackI cb) {
        GLFWKeyCallback callback = GLFWKeyCallback.create(cb);
        EngineResources.register(callback);
        return callback;
    }

    public static GLFWCursorPosCallback cursorPos(GLFWCursorPosCallbackI cb) {
        GLFWCursorPosCallback callback = GLFWCursorPosCallback.create(cb);
        EngineResources.register(callback);
        return callback;
    }

    public static GLFWMouseButtonCallback mouseButton(GLFWMouseButtonCallbackI cb) {
        GLFWMouseButtonCallback callback = GLFWMouseButtonCallback.create(cb);
        EngineResources.register(callback);
        return callback;
    }

    public static GLFWScrollCallback scroll(GLFWScrollCallbackI cb) {
        GLFWScrollCallback callback = GLFWScrollCallback.create(cb);
        EngineResources.register(callback);
        return callback;
    }

    public static GLFWErrorCallback errorPrintCallback(PrintStream printStream) {
        GLFWErrorCallback callback = GLFWErrorCallback.createPrint(printStream);
        EngineResources.register(callback);
        return callback;
    }
}