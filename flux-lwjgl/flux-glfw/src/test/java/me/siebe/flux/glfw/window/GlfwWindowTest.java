package me.siebe.flux.glfw.window;

import me.siebe.flux.api.event.common.FramebufferResizeEvent;
import me.siebe.flux.api.event.common.WindowResizeEvent;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowMode;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.core.HeadlessContextInjector;
import me.siebe.flux.test.implementations.event.RecordingEventBus;
import me.siebe.flux.test.implementations.event.TestEventBus;
import me.siebe.flux.test.junit.OpenGLResetExtension;
import me.siebe.flux.test.junit.StartupBannerMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static me.siebe.flux.test.assertions.EventTestAssertions.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;

@ExtendWith({StartupBannerMockExtension.class, OpenGLResetExtension.class})
public class GlfwWindowTest {
    private Window window;

    @BeforeEach
    void initGlfwWindow() {
        window = new GlfwWindowBuilder()
                .title("Test Window")
                .mode(WindowMode.WINDOWED)
                .width(300, 200, 800)
                .height(200, 100, 600)
                .hidden()
                .build();
        HeadlessContextInjector.inject(window, null, null, new RecordingEventBus(new TestEventBus()), null);
    }

    @AfterEach
    void destroyGlfwWindow() {
        if (window != null) {
            window.destroy();
        }
        HeadlessContextInjector.resetAppContext();
    }

    @Test
    void windowInitAndDestroy_registerResizeEventListener() {
        assertNoListenerRegistered(FramebufferResizeEvent.class);
        assertNoListenerRegistered(WindowResizeEvent.class);

        window.init();

        assertListenerRegistered(FramebufferResizeEvent.class);
        assertListenerRegistered(WindowResizeEvent.class);

        window.destroy();
        window = null;

        assertNoListenerRegistered(FramebufferResizeEvent.class);
        assertNoListenerRegistered(WindowResizeEvent.class);
    }

    @Test
    void windowResize_postsResizeEvent() {
        window.init();

        // Set window size
        glfwSetWindowSize(window.getId(), 500, 450);
        try {
            Thread.sleep(50); // 50 ms to let events propagate
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        glfwPollEvents();

        // Assert that FramebufferResizeEvent and WindowResizeEvent are both called 1 time
        if (AppContext.get().getEventBus() instanceof RecordingEventBus eventBus) {
            var recordedEvents = eventBus.getRecordedEvents();
            assertRecordedEventCount(recordedEvents, 2);
            assertRecordedEventCountByType(recordedEvents, FramebufferResizeEvent.class, 1);
            assertRecordedEventCountByType(recordedEvents, WindowResizeEvent.class, 1);
        }
    }


}
