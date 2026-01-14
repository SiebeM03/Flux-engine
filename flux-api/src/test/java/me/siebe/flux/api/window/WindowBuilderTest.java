package me.siebe.flux.api.window;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class WindowBuilderTest {

    private WindowBuilder createMockBuilder() {
        WindowBuilder builder = Mockito.mock(WindowBuilder.class, Mockito.withSettings()
                .useConstructor(WindowPlatform.GLFW)
                .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        when(builder.build()).thenReturn(null);
        return builder;
    }

    @Test
    void builderSetsPropertiesCorrectly() {
        WindowBuilder builder = createMockBuilder();

        builder.title("Test")
                .mode(WindowMode.WINDOWED)
                .width(800, 200, 1600)
                .height(600, 300, 1200)
                .vsync(true)
                .samples(4)
                .targetFps(150);

        WindowConfig config = builder.config;

        // Check all set properties
        assertEquals("Test", config.title);
        assertEquals(WindowMode.WINDOWED, config.mode);
        assertEquals(800, config.width);
        assertEquals(200, config.minWidth);
        assertEquals(1600, config.maxWidth);
        assertEquals(600, config.height);
        assertEquals(300, config.minHeight);
        assertEquals(1200, config.maxHeight);
        assertTrue(config.vsync);
        assertEquals(150, config.targetFps);
        assertEquals(4, config.samples);
    }

    @Test
    void builderUsesDefaultConfigs() {
        WindowBuilder builder = createMockBuilder();

        WindowConfig builderConfig = builder.config;
        WindowConfig defaultConfig = new WindowConfig();

        // Assert that builderConfig has the same values as defaultConfig
        assertEquals(defaultConfig.windowId, builderConfig.windowId);
        assertEquals(defaultConfig.title, builderConfig.title);
        assertEquals(defaultConfig.mode, builderConfig.mode);
        assertEquals(defaultConfig.monitor, builderConfig.monitor);

        assertEquals(defaultConfig.width, builderConfig.width);
        assertEquals(defaultConfig.minWidth, builderConfig.minWidth);
        assertEquals(defaultConfig.maxWidth, builderConfig.maxWidth);

        assertEquals(defaultConfig.height, builderConfig.height);
        assertEquals(defaultConfig.minHeight, builderConfig.minHeight);
        assertEquals(defaultConfig.maxHeight, builderConfig.maxHeight);

        assertEquals(defaultConfig.vsync, builderConfig.vsync);
        assertEquals(defaultConfig.samples, builderConfig.samples);
        assertEquals(defaultConfig.targetFps, builderConfig.targetFps);
    }
}
