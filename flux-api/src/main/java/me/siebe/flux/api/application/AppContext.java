package me.siebe.flux.api.application;

import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.util.system.ProvidableSystem;
import me.siebe.flux.util.system.SystemProvider;
import me.siebe.flux.util.system.SystemProviderType;
import me.siebe.flux.util.time.Timer;

/**
 * Central application context providing access to core engine systems such as the window,
 * renderer, timer, event bus, and system manager. Implementations are provided by the engine
 * via {@link SystemProvider}; application code typically obtains the context through
 * {@link #get()} or the {@link #withContext(ContextCallback)} / {@link #withContextNoReturn(ContextCallbackNoReturn)}
 * utility methods.
 */
public abstract class AppContext implements ProvidableSystem {
    protected static AppContext instance;

    /** Protected constructor for use by engine-provided implementations. */
    protected AppContext() {
    }

    /**
     * Returns the current application context instance. The instance is lazily created by
     * {@link SystemProvider} when first requested (engine-only type).
     *
     * @return the singleton application context, never {@code null}
     */
    public static AppContext get() {
        if (instance == null) {
            instance = SystemProvider.provide(AppContext.class, SystemProviderType.ENGINE_ONLY);
        }
        return instance;
    }

    // =================================================================================================================
    // API methods
    // =================================================================================================================

    /** Returns the application window. */
    public abstract Window getWindow();

    /** Returns the renderer used for drawing. */
    public abstract Renderer getRenderer();

    /** Returns the application timer for frame timing and delta. */
    public abstract Timer getTimer();

    /** Returns the event bus for publishing and subscribing to application events. */
    public abstract EventBus getEventBus();

    /** Returns the system manager for registering and accessing application systems. */
    public abstract SystemManager getSystemManager();


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
