package me.siebe.flux.api.application;

import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.util.system.ProvidableSystem;
import me.siebe.flux.util.system.SystemProvider;
import me.siebe.flux.util.system.SystemProviderType;
import me.siebe.flux.util.time.Timer;

import java.util.function.Consumer;

public abstract class AppContext implements ProvidableSystem {
    protected static AppContext instance;

    protected AppContext() {
    }

    public static AppContext get() {
        if (instance == null) {
            instance = SystemProvider.provide(AppContext.class, SystemProviderType.ENGINE_ONLY);
        }
        return instance;
    }

    // =================================================================================================================
    // API methods
    // =================================================================================================================
    public abstract Window getWindow();

    public abstract Renderer getRenderer();

    public abstract Timer getTimer();

    public abstract EventBus getEventBus();

    public abstract SystemManager getSystemManager();


    // =================================================================================================================
    // Utility methods
    // =================================================================================================================
    @FunctionalInterface
    public interface ContextCallback<R> {
        R apply(AppContext ctx);
    }

    @FunctionalInterface
    public interface ContextCallbackNoReturn {
        void apply(AppContext ctx);
    }

    public static <R> R withContext(ContextCallback<R> callback) {
        return callback.apply(get());
    }

    public static void withContextNoReturn(Consumer<AppContext> ctxConsumer) {
        ctxConsumer.accept(get());
    }
}
