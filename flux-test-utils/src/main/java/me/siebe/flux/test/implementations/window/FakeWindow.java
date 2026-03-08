package me.siebe.flux.test.implementations.window;

import me.siebe.flux.api.window.Window;

/**
 * Fake window for headless tests. Never closes unless requested; no-op init/update/destroy.
 */
public final class FakeWindow implements Window {
    private boolean shouldClose;
    private int width;
    private int height;
    private int targetFps;
    private long idCounter = 1;

    public FakeWindow() {
        this(800, 600, 60);
    }

    public FakeWindow(int width, int height, int targetFps) {
        this.width = width;
        this.height = height;
        this.targetFps = targetFps;
    }

    @Override
    public void init() {
        // No-op
    }

    @Override
    public void update() {
        // No-op
    }

    @Override
    public void destroy() {
        // No-op
    }

    @Override
    public boolean shouldClose() {
        return shouldClose;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getTargetFps() {
        return targetFps;
    }

    @Override
    public long getId() {
        return idCounter++;
    }

    /** Makes the window report that it should close (e.g. to exit a loop in tests). */
    public void requestClose() {
        shouldClose = true;
    }
}
