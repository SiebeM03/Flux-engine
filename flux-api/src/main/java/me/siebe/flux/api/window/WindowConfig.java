package me.siebe.flux.api.window;

import me.siebe.flux.util.config.Flux;
import me.siebe.flux.util.string.ToStringIgnore;

/**
 * Mutable window configuration populated by {@link me.siebe.flux.api.window.WindowBuilder}.
 * <p>
 * The no-args constructor provides sensible defaults so builders can be used fluently without
 * requiring every attribute to be set explicitly. Defaults include:
 * <ul>
 *     <li>title: {@code "Flux Engine"}</li>
 *     <li>mode: {@link WindowMode#WINDOWED}</li>
 *     <li>monitor: {@link org.lwjgl.system.MemoryUtil#NULL}</li>
 *     <li>width/height: {@code 1280x720}</li>
 *     <li>minimum dimensions: {@code 120x120}</li>
 *     <li>maximum dimensions: {@link me.siebe.flux.util.config.Flux#FLUX_DONT_CARE}</li>
 *     <li>vsync: {@code true}</li>
 *     <li>samples: {@code 0}</li>
 *     <li>target FPS: {@link me.siebe.flux.util.config.Flux#FLUX_DONT_CARE}</li>
 * </ul>
 * </p>
 */
public class WindowConfig {
    @ToStringIgnore
    public long windowId;
    public String title;
    public WindowMode mode;
    public long monitor;

    public int width;
    public int minWidth;
    public int maxWidth;

    public int height;
    public int minHeight;
    public int maxHeight;

    public boolean vsync;
    public int samples;
    public int targetFps;


    public WindowConfig() {
        this.windowId = -1L;
        this.title = "Flux Engine";
        this.mode = WindowMode.WINDOWED;
        this.monitor = Flux.NULL;

        this.width = 1280;
        this.minWidth = 120;
        this.maxWidth = Flux.FLUX_DONT_CARE;

        this.height = 720;
        this.minHeight = 120;
        this.maxHeight = Flux.FLUX_DONT_CARE;

        this.vsync = true;
        this.samples = 0;
        this.targetFps = Flux.FLUX_DONT_CARE;
    }
}
