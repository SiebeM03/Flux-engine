package me.siebe.flux.lwjgl.glfw.window;

import me.siebe.flux.api.event.common.FramebufferResizeEvent;
import me.siebe.flux.api.event.common.WindowResizeEvent;
import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.keyboard.Key;
import me.siebe.flux.api.input.keyboard.Modifier;
import me.siebe.flux.api.input.keyboard.event.KeyPressEvent;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowConfig;
import me.siebe.flux.core.AppContext;
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

        Input.init(
                new GlfwKeyboard(getId())
        );

        // Register window event callbacks
        glfwSetWindowSizeCallback(getId(), this::sendWindowResizeEvent);
        glfwSetFramebufferSizeCallback(getId(), this::sendFramebufferResizeEvent);

        // Register event listeners
        AppContext.get().getEventBus().getListenerRegistry().register(WindowResizeEvent.class, this::onWindowResize);
        AppContext.get().getEventBus().getListenerRegistry().register(FramebufferResizeEvent.class, this::onFramebufferResize);

        AppContext.get().getEventBus().getListenerRegistry().register(KeyPressEvent.class, e -> {
            if (!e.isKey(Key.KEY_ESCAPE)) return;
            glfwSetWindowShouldClose(getId(), true);
        });
    }

    @Override
    public void update() {
        glfwSwapBuffers(config.windowId);
        glfwPollEvents();
    }

    private void sendWindowResizeEvent(long windowId, int width, int height) {
        if (isValidSizeChange(width, height)) {
            AppContext.get().getEventBus().post(WindowResizeEvent.class, e -> e.set(config.width, config.height, width, height));
        }
    }

    private void onWindowResize(WindowResizeEvent e) {
        config.width = e.getNewWidth();
        config.height = e.getNewHeight();
    }

    private void sendFramebufferResizeEvent(long windowId, int width, int height) {
        if (isValidSizeChange(width, height)) {
            AppContext.get().getEventBus().post(FramebufferResizeEvent.class, e -> e.set(config.width, config.height, width, height));
        }
    }

    private void onFramebufferResize(FramebufferResizeEvent e) {
        OpenGLState.setViewport(0, 0, e.getNewWidth(), e.getNewHeight());
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

        AppContext.get().getEventBus().getListenerRegistry().unregister(WindowResizeEvent.class, this::onWindowResize);
        AppContext.get().getEventBus().getListenerRegistry().unregister(FramebufferResizeEvent.class, this::onFramebufferResize);
    }

    private boolean isValidSizeChange(int newWidth, int newHeight) {
        if (newWidth == 0 || newHeight == 0) return false;
        return newWidth != config.width || newHeight != config.height;
    }
}
