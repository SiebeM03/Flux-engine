package me.siebe.flux.api.window;

import me.siebe.flux.util.system.SystemProvider;
import me.siebe.flux.util.system.SystemProviderType;

public interface Window {
    // =================================================================================================================
    // Lifecycle methods
    // =================================================================================================================
    void init();

    void update();

    void destroy();

    boolean shouldClose();


    // =================================================================================================================
    // Window properties
    // =================================================================================================================
    int getWidth();

    int getHeight();

    default float getAspectRatio() {
        return (float) getWidth() / (float) getHeight();
    }

    long getId();

    static WindowBuilder builder(WindowPlatform platform) {
        return SystemProvider.provide(WindowBuilder.class, SystemProviderType.ALL, wb -> wb.getPlatform() == platform);
    }
}
