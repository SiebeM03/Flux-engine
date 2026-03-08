package me.siebe.flux.core;

import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.systems.SystemManager;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.util.time.Timer;

/**
 * Injects headless test doubles into {@link AppContext}. Placed in package {@code me.siebe.flux.core}
 * so that context fields can be set. For use only from test code
 */
public final class HeadlessContextInjector {
    private HeadlessContextInjector() {}

    /**
     * Injects the given components into the current AppContext. Null values are stored as-is;
     * the engine may require non-null window/timer/systemManager for some code paths.
     *
     * @param window        optional fake window
     * @param timer         optional timer (e.g. using FixedStepTimeProvider)
     * @param renderer      optional renderer (e.g. using FakeRenderPipeline)
     * @param eventBus      optional event bus (e.g. TestEventBus)
     * @param systemManager optional system manager
     */
    public static void inject(
            Window window,
            Timer timer,
            Renderer renderer,
            EventBus eventBus,
            SystemManager systemManager
    ) {
        AppContext ctx = AppContext.get();
        if (window != null) ctx.window = window;
        if (timer != null) ctx.timer = timer;
        if (renderer != null) ctx.renderer = renderer;
        if (eventBus != null) ctx.eventBus = eventBus;
        if (systemManager != null) ctx.systemManager = systemManager;
    }

    public static void resetAppContext() {
        AppContext.resetForTests();
    }
}
