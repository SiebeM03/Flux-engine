package me.siebe.flux.lwjgl.glfw.window;

import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowConfig;
import me.siebe.flux.lwjgl.opengl.OpenGLState;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import me.siebe.flux.util.string.StringUtils;

import static org.lwjgl.glfw.GLFW.*;

public class GlfwWindow implements Window {
    private static final Logger logger = LoggerFactory.getLogger(GlfwWindow.class, LoggingCategories.WINDOW);
    private final WindowConfig config;

    GlfwWindow(WindowConfig config) {
        this.config = config;
    }

    @Override
    public void init() {
        logger.info("Initializing GlfwWindow with {}", StringUtils.toString(config, true));

        OpenGLState.init();
        OpenGLState.enableDepthTest();
        OpenGLState.setViewport(0, 0, config.width, config.height);
    }

    @Override
    public void update() {
        glfwSwapBuffers(config.windowId);
        glfwPollEvents();
    }

    @Override
    public boolean shouldClose() {
        return glfwWindowShouldClose(config.windowId);
    }

    @Override
    public int getWidth() {
        return config.width;
    }

    @Override
    public int getHeight() {
        return config.height;
    }

    @Override
    public long getId() {
        return config.windowId;
    }

    @Override
    public void destroy() {
        logger.info("Destroying GlfwWindow");
        glfwDestroyWindow(config.windowId);
    }
}
