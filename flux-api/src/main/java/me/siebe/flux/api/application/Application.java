package me.siebe.flux.api.application;

import me.siebe.flux.api.window.Window;
import me.siebe.flux.util.time.Timer;

public abstract class Application {
    public abstract void registerEngineSystem(EngineSystem engineSystem);

    public abstract void unregisterEngineSystem(Class<? extends EngineSystem> clazz);

    protected void setTimer(AppContext ctx, Timer timer) {
        ctx.timer = timer;
    }

    protected void setWindow(AppContext ctx, Window window) {
        ctx.window = window;
    }
}
