package me.siebe.flux.lwjgl.glfw.window;

import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.event.EventBusProvider;
import me.siebe.flux.api.event.common.FramebufferResizeEvent;
import me.siebe.flux.api.event.common.WindowResizeEvent;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowConfig;
import me.siebe.flux.lwjgl.glfw.input.MouseButtonEvent;
import me.siebe.flux.lwjgl.glfw.input.MousePosEvent;
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

        // Register window event callbacks
        glfwSetWindowSizeCallback(getId(), this::sendWindowResizeEvent);
        glfwSetFramebufferSizeCallback(getId(), this::sendFramebufferResizeEvent);

        glfwSetCursorPosCallback(getId(), this::sendMousePosEvent);
        glfwSetMouseButtonCallback(getId(), this::sendMouseClickEvent);

        EventBus eventBus = EventBusProvider.get();
        // Register event listeners
        eventBus.getListenerRegistry().register(WindowResizeEvent.class, this::onWindowResize);
        eventBus.getListenerRegistry().register(FramebufferResizeEvent.class, this::onFramebufferResize);

        // Register all Pooled events
        eventBus.getEventPoolRegistry().register(WindowResizeEvent.class, WindowResizeEvent::new);
        eventBus.getEventPoolRegistry().register(FramebufferResizeEvent.class, FramebufferResizeEvent::new);
        eventBus.getEventPoolRegistry().register(MousePosEvent.class, MousePosEvent::new);
        eventBus.getEventPoolRegistry().register(MouseButtonEvent.class, MouseButtonEvent::new);
    }

    @Override
    public void update() {
        glfwSwapBuffers(config.windowId);
        glfwPollEvents();
    }

    private void sendWindowResizeEvent(long windowId, int width, int height) {
        if (isValidSizeChange(width, height)) {
            EventBusProvider.get().post(WindowResizeEvent.class, e -> e.set(config.width, config.height, width, height));
        }
    }

    private void onWindowResize(WindowResizeEvent e) {
        config.width = e.getNewWidth();
        config.height = e.getNewHeight();
    }

    private void sendFramebufferResizeEvent(long windowId, int width, int height) {
        if (isValidSizeChange(width, height)) {
            EventBusProvider.get().post(FramebufferResizeEvent.class, e -> e.set(config.width, config.height, width, height));
        }
    }

    private void onFramebufferResize(FramebufferResizeEvent e) {
        OpenGLState.setViewport(0, 0, e.getNewWidth(), e.getNewHeight());
    }

    private void sendMousePosEvent(long windowId, double x, double y) {
        EventBusProvider.get().post(MousePosEvent.class, e -> e.set(this, x, y));
    }

    private void sendMouseClickEvent(long windowId, int button, int action, int mods) {
        EventBusProvider.get().post(MouseButtonEvent.class, e -> e.set(button, action));
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

        EventBusProvider.get().getListenerRegistry().unregister(WindowResizeEvent.class, this::onWindowResize);
        EventBusProvider.get().getListenerRegistry().unregister(FramebufferResizeEvent.class, this::onFramebufferResize);
    }

    private boolean isValidSizeChange(int newWidth, int newHeight) {
        if (newWidth == 0 || newHeight == 0) return false;
        return newWidth != config.width || newHeight != config.height;
    }
}
