package me.siebe.flux.api.window;


import me.siebe.flux.util.system.ProvidableSystem;

public abstract class WindowBuilder implements ProvidableSystem {
    protected final WindowPlatform platform;
    protected final WindowConfig config;
    protected boolean showWindow = true;

    protected WindowBuilder(WindowPlatform platform) {
        this.platform = platform;
        this.config = new WindowConfig();
    }

    public WindowBuilder title(String title) {
        config.title = title;
        return this;
    }

    public WindowBuilder mode(WindowMode mode) {
        config.mode = mode;
        return this;
    }

    public WindowBuilder width(int width, int minWidth, int maxWidth) {
        config.width = width;
        config.minWidth = minWidth;
        config.maxWidth = maxWidth;
        return this;
    }

    public WindowBuilder height(int height, int minHeight, int maxHeight) {
        config.height = height;
        config.minHeight = minHeight;
        config.maxHeight = maxHeight;
        return this;
    }

    public WindowBuilder vsync(boolean vsync) {
        config.vsync = vsync;
        return this;
    }

    public WindowBuilder samples(int samples) {
        config.samples = samples;
        return this;
    }

    public WindowBuilder targetFps(int targetFps) {
        config.targetFps = targetFps;
        return this;
    }

    public WindowBuilder hidden() {
        this.showWindow = false;
        return this;
    }

    public WindowPlatform getPlatform() {
        return this.platform;
    }

    public abstract Window build();
}
