package me.siebe.flux.core;

import me.siebe.flux.api.application.AppContext;
import me.siebe.flux.api.application.SystemManager;
import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.util.exceptions.ApplicationException;
import me.siebe.flux.util.time.Timer;

/**
 * The FluxContext is a singleton that provides access to the various subsystems of the Flux engine.
 */
public class FluxContext extends AppContext {
    Window window;
    Timer timer;
    Renderer renderer;
    EventBus eventBus;
    SystemManager systemManager;

    public FluxContext() {
        if (AppContext.instance != null) {
            throw new ApplicationException("Flux Context already initialized");
        }
    }

    public static FluxContext get() {
        return (FluxContext) AppContext.get();
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public SystemManager getSystemManager() {
        return systemManager;
    }
}
