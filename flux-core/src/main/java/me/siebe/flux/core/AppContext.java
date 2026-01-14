package me.siebe.flux.core;

import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.event.EventBusProvider;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.renderer.RendererProvider;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.util.exceptions.ApplicationException;
import me.siebe.flux.util.time.Timer;

import java.util.function.Consumer;

@Deprecated()
public class AppContext {

    private static AppContext instance;

    private FluxApplication application;
    Window window;
    Timer timer;
    Renderer renderer;
    EventBus eventBus;

    private AppContext() {
    }


    @FunctionalInterface
    public interface ContextCallback<R> {
        R apply(AppContext ctx);
    }

    @FunctionalInterface
    public interface ContextCallbackNoReturn {
        void apply(AppContext ctx);
    }

    public static AppContext get() {
        if (instance == null) instance = new AppContext();
        return instance;
    }

    public static <R> R withContext(ContextCallback<R> callback) {
        return callback.apply(get());
    }

    public static void withContextNoReturn(Consumer<AppContext> ctxConsumer) {
        ctxConsumer.accept(get());
    }

    public FluxApplication getApplication() {
        if (this.application == null) throw ApplicationException.notInitialized();
        return this.application;
    }

    public void setApplication(FluxApplication application) {
        if (this.application != null) throw ApplicationException.alreadyInitialized();
        this.application = application;
    }
    public Window getWindow() {
        return window;
    }

    public Timer getTimer() {
        return timer;
    }

    @Deprecated
    public Renderer getRenderer() {
        return RendererProvider.get();
    }

    @Deprecated
    public EventBus getEventBus() {
        return EventBusProvider.get();
    }
}
