package me.siebe.flux.core;

import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.systems.SystemManager;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.util.time.Timer;

/**
 * Central application context providing access to core engine systems such as window, renderer,
 * timer, event bus, and system manager. The instance is lazily created when first requested
 * and is populated by the engine during FluxApplication init.
 * Application code typically obtains the context through {@link #get()} or the
 * {@link #withContext(ContextCallback)} / {@link #withContextNoReturn(ContextCallbackNoReturn)}
 * utility methods.
 */
public final class AppContext {
    private static AppContext instance;

    Window window;
    Timer timer;
    Renderer renderer;
    EventBus eventBus;
    SystemManager systemManager;

    private AppContext() {
    }

    public static AppContext get() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    // =================================================================================================================
    // API methods
    // =================================================================================================================

    /** Returns the application window. */
    public Window getWindow() {return window;}

    /** Returns the renderer used for drawing. */
    public Renderer getRenderer() {return renderer;}

    /** Returns the application timer for frame timing and delta. */
    public Timer getTimer() {return timer;}

    /** Returns the event bus for publishing and subscribing to application events. */
    public EventBus getEventBus() {return eventBus;}

    /** Returns the system manager for registering and accessing application systems. */
    public SystemManager getSystemManager() {return systemManager;}


    // =================================================================================================================
    // Utility methods
    // =================================================================================================================

    /**
     * Callback that receives the current {@link AppContext} and returns a value.
     *
     * @param <R> the type of the result
     */
    @FunctionalInterface
    public interface ContextCallback<R> {
        /**
         * Applies this callback to the given context.
         *
         * @param ctx the application context
         * @return the result of the callback
         */
        R apply(AppContext ctx);
    }

    /**
     * Callback that receives the current {@link AppContext} and performs a side effect (no return value).
     */
    @FunctionalInterface
    public interface ContextCallbackNoReturn {
        /**
         * Applies this callback to the given context.
         *
         * @param ctx the application context
         */
        void apply(AppContext ctx);
    }

    /**
     * Runs the given callback with the current application context and returns its result.
     * Convenient when a method only needs temporary access to the context.
     *
     * @param callback the callback to run with the context
     * @param <R>      the return type of the callback
     * @return the value returned by {@code callback.apply(get())}
     */
    public static <R> R withContext(ContextCallback<R> callback) {
        return callback.apply(get());
    }

    /**
     * Runs the given callback with the current application context. Use when no return value is needed.
     *
     * @param callback the callback to run with the context
     */
    public static void withContextNoReturn(ContextCallbackNoReturn callback) {
        callback.apply(get());
    }
}
