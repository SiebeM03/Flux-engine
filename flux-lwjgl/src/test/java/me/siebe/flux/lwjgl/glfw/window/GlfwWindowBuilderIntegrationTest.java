package me.siebe.flux.lwjgl.glfw.window;

import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFWVidMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.getCapabilities;

public class GlfwWindowBuilderIntegrationTest {
    private static boolean glfwInitialized = false;
    private static long primaryMonitorWidth;
    private static long primaryMonitorHeight;

    @BeforeAll
    static void setupGlfw() {
        // Ensure GLFW initializes correctly
        if (!glfwInit()) {
            fail("Failed to initialize GLFW in integration test — environment likely lacks OpenGL support");
        }
        glfwInitialized = true;

        // Get primary monitor size for FULLSCREEN/BORDERLESS checks
        long primaryMonitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = glfwGetVideoMode(primaryMonitor);
        assertNotNull(vidMode, "Primary monitor video mode cannot be null");
        primaryMonitorWidth = vidMode.width();
        primaryMonitorHeight = vidMode.height();
    }

    @AfterAll
    static void cleanupGlfw() {
        if (glfwInitialized) {
            glfwTerminate();
        }
    }

    @Test
    void testWindowCreation() {
        assertTrue(glfwInitialized, "GLFW must be initialized for test");

        Window window = new GlfwWindowBuilder()
                .title("Test Window")
                .mode(WindowMode.WINDOWED)
                .width(300, 200, 800)
                .height(200, 100, 600)
                .hidden()
                .build();

        window.init();

        assertNotEquals(-1L, window.getId(), "Window ID must not be -1");
        assertFalse(window.shouldClose(), "New window should not be marked for closing");
        assertEquals(300, window.getWidth());
        assertEquals(200, window.getHeight());

        // Ensure OpenGL context exists
        assertNotNull(getCapabilities(), "OpenGL capabilities should not be null after making context current");

        window.destroy();
    }

    @Test
    void testWindowModes() {
        for (WindowMode mode : WindowMode.values()) {
            Window window = new GlfwWindowBuilder()
                    .title(mode.name() + " Test")
                    .mode(mode)
                    .width(400, 200, 800)
                    .height(300, 200, 600)
                    .hidden()
                    .build();

            window.init();

            switch (mode) {
                case WINDOWED -> {
                    assertEquals(400, window.getWidth(), "Windowed width mismatch");
                    assertEquals(300, window.getHeight(), "Windowed height mismatch");
                }
                case FULLSCREEN, BORDERLESS -> {
                    // FULLSCREEN and BORDERLESS should match primary monitor size
                    assertEquals(primaryMonitorWidth, window.getWidth(), "Fullscreen/BORDERLESS width mismatch");
                    assertEquals(primaryMonitorHeight, window.getHeight(), "Fullscreen/BORDERLESS height mismatch");
                }
            }

            window.destroy();
        }
    }

    @Test
    void testResizeCallbackUpdatesConfig() throws InterruptedException {
        Window window = new GlfwWindowBuilder()
                .title("Resize Test")
                .mode(WindowMode.WINDOWED)
                .width(300, 200, 800)
                .height(200, 100, 600)
                .hidden()
                .build();

        window.init();

        long id = window.getId();
        assertEquals(300, window.getWidth());
        assertEquals(200, window.getHeight());

        // Trigger resize event
        glfwSetWindowSize(id, 500, 450);

        // Poll events so the callback fires
        for (int i = 0; i < 5; i++) {
            glfwPollEvents();
            Thread.sleep(10);
        }

        assertEquals(500, window.getWidth());
        assertEquals(450, window.getHeight());

        window.destroy();
    }

    @Test
    void testWindowCloseListener() throws InterruptedException {
        Window window = new GlfwWindowBuilder()
                .title("Close Test")
                .mode(WindowMode.WINDOWED)
                .width(300, 200, 800)
                .height(200, 100, 600)
                .hidden()
                .build();

        window.init();

        long id = window.getId();
        assertFalse(window.shouldClose());

        // Trigger close event
        glfwSetWindowShouldClose(id, true);

        // Poll events for callback
        for (int i = 0; i < 5; i++) {
            glfwPollEvents();
            Thread.sleep(10);
        }

        assertTrue(window.shouldClose(), "Window should now be marked closed");

        window.destroy();
    }

    @Test
    void testBufferSwapAndPollEvents() {
        Window window = new GlfwWindowBuilder()
                .title("Swap Test")
                .mode(WindowMode.WINDOWED)
                .width(300, 200, 800)
                .height(200, 100, 600)
                .vsync(true)
                .hidden()
                .build();

        window.init();

        assertDoesNotThrow(window::update, "update() should call glfwSwapBuffers + glfwPollEvents without error");

        window.destroy();
    }


}
