package me.siebe.flux.core;

import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.lwjgl.opengl.OpenGLState;
import me.siebe.flux.util.FluxColor;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import me.siebe.flux.util.system.ProvidableSystem;
import me.siebe.flux.util.time.Timer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;

public abstract class FluxApplication implements ProvidableSystem {
    private static final Logger logger = LoggerFactory.getLogger(FluxApplication.class, LoggingCategories.APPLICATION);

    final void init() {
        logger.info("Initializing Application");

        try {
            initEngineSystems();
            initGameSystems();
        } catch (Exception e) {
            logger.error("Something went wrong while initializing the application", e);
        }
    }

    private void initEngineSystems() {
        logger.info("Initializing Engine Systems");

        AppContext.withContextNoReturn(ctx -> {
            ctx.timer = new Timer();

            // Window initialization
            WindowBuilder windowBuilder = createWindowBuilder();
            ctx.window = windowBuilder.build();
            ctx.window.init();
        });
    }

    protected abstract void initGameSystems();

    final void run() {
        try {
            AppContext.withContextNoReturn(ctx -> {
                while (!ctx.window.shouldClose()) {
                    glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                    glEnable(GL_DEPTH_TEST);

                    gameUpdate(ctx);
                    engineUpdate(ctx);
                }
            });
        } catch (Exception e) {
            logger.error("Error during runtime loop", e);
        } finally {
            destroy();
        }
    }

    private void engineUpdate(final AppContext ctx) {
        logger.trace("Updating Engine");

        ctx.timer.update();
        ctx.timer.print();

        ctx.window.update();
    }

    protected abstract void gameUpdate(final AppContext ctx);


    // =================================================================================================================
    // Shutdown related methods
    // =================================================================================================================
    final void destroy() {
        logger.info("Application is shutting down, cleaning up resources");
        AppContext.withContextNoReturn(ctx -> {
            destroyEngineSystems(ctx);
            destroyGameSystems();
        });
    }

    private void destroyEngineSystems(final AppContext ctx) {
        ctx.window.destroy();
    }

    protected abstract void destroyGameSystems();

    // =================================================================================================================
    // Instance lifecycle methods
    // =================================================================================================================
    protected FluxApplication() {
    }

    protected abstract WindowBuilder createWindowBuilder();
}

