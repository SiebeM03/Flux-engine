package me.siebe.flux.lwjgl.glfw.window;

import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.api.window.WindowPlatform;
import me.siebe.flux.util.exceptions.WindowException;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

import static me.siebe.flux.util.config.Flux.NULL;
import static org.lwjgl.glfw.GLFW.*;

public class GlfwWindowBuilder extends WindowBuilder {
    private static final Logger logger = LoggerFactory.getLogger(GlfwWindowBuilder.class, LoggingCategories.WINDOW);

    public GlfwWindowBuilder() {
        super(WindowPlatform.GLFW);
    }

    @Override
    public Window build() {
        logger.info("Creating GLFW window instance");

        initGLFW();
        setWindowHints();
        createWindow();
        applySettings();

        setWindowSizeListener();
        setWindowCloseListener();

        return new GlfwWindow(config);
    }

    private void initGLFW() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw WindowException.creationFailed("Failed to initialize GLFW");
        }
    }

    private void createWindow() {
        long primaryMonitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = glfwGetVideoMode(primaryMonitor);
        if (vidMode == null) throw WindowException.creationFailed("Video mode cannot be null");

        switch (config.mode) {
            case FULLSCREEN -> {
                config.width = vidMode.width();
                config.height = vidMode.height();
                config.monitor = primaryMonitor;
                config.windowId = glfwCreateWindow(config.width, config.height, config.title, config.monitor, NULL);
            }
            case WINDOWED -> {
                glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
                config.windowId = glfwCreateWindow(config.width, config.height, config.title, config.monitor, NULL);
            }
            case BORDERLESS -> {
                glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
                config.width = vidMode.width();
                config.height = vidMode.height();
                config.windowId = glfwCreateWindow(config.width, config.height, config.title, config.monitor, NULL);
                glfwSetWindowPos(config.windowId, 0, 0);
            }
        }
        if (config.windowId == -1L || config.windowId == NULL) {
            throw WindowException.creationFailed("Failed to create GLFW window");
        }
    }

    private void setWindowHints() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        // TODO implement debug status
        if (false) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        }

        glfwWindowHint(GLFW_SAMPLES, config.samples);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
    }

    private void applySettings() {
        glfwMakeContextCurrent(config.windowId);
        glfwSetWindowSizeLimits(config.windowId, config.minWidth, config.minHeight, config.maxWidth, config.maxHeight);
        if (showWindow) {
            glfwShowWindow(config.windowId);
        }
        glfwSwapInterval(config.vsync ? 1 : 0);
    }

    private void setWindowSizeListener() {
        glfwSetWindowSizeCallback(config.windowId, (window, width, height) -> {
            config.width = width;
            config.height = height;
        });
    }

    private void setWindowCloseListener() {
        glfwSetWindowCloseCallback(config.windowId, window -> {
            glfwSetWindowShouldClose(config.windowId, true);
        });
    }
}
