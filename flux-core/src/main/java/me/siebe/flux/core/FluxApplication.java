package me.siebe.flux.core;

import me.siebe.flux.api.renderer.RenderContext;
import me.siebe.flux.api.renderer.RenderPipeline;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.lwjgl.opengl.OpenGLState;
import me.siebe.flux.renderer.pipeline.steps.ClearStep;
import me.siebe.flux.util.FluxColor;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import me.siebe.flux.util.system.ProvidableSystem;
import me.siebe.flux.util.time.Timer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class FluxApplication implements ProvidableSystem {
    private static final Logger logger = LoggerFactory.getLogger(FluxApplication.class, LoggingCategories.APPLICATION);

    final void init() {
        logger.info("Initializing Application");

        try {
            initEngineSystems();
            initGameSystems();
            // Initialize render pipeline after all steps have been added
            initializeRenderPipeline();
        } catch (Exception e) {
            logger.error("Something went wrong while initializing the application", e);
        }
    }

    private void initializeRenderPipeline() {
        AppContext.withContextNoReturn(ctx -> {
            RenderContext initContext = new RenderContext(
                    ctx.window.getWidth(),
                    ctx.window.getHeight(),
                    0.0f,
                    0.0
            );
            ctx.renderPipeline.init(initContext);
        });
    }

    private void initEngineSystems() {
        logger.info("Initializing Engine Systems");

        AppContext.withContextNoReturn(ctx -> {
            ctx.timer = new Timer();

            // Window initialization
            WindowBuilder windowBuilder = createWindowBuilder();
            ctx.window = windowBuilder.build();
            ctx.window.init();

            // OpenGL initialization
            OpenGLState.init();
            OpenGLState.setViewport(0, 0, ctx.window.getWidth(), ctx.window.getHeight());

            // Render pipeline initialization (basic steps only)
            // Note: Pipeline will be fully initialized after game systems add their steps
            ctx.renderPipeline = RenderPipeline.create();
            ctx.renderPipeline.addStep(new ClearStep(new FluxColor(0.1f, 0.1f, 0.1f)));
        });
    }

    protected abstract void initGameSystems();

    final void run() {
        try {
            AppContext.withContextNoReturn(ctx -> {
                while (!ctx.window.shouldClose()) {
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

        Vector3f viewPosition = new Vector3f(0.0f, 0.0f, -3.0f);
        Vector3f targetPosition = new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3f upPosition = new Vector3f(0.0f, 0.0f, 0.0f);
        Matrix4f viewMatrix = new Matrix4f().lookAt(
                viewPosition,
                targetPosition,
                upPosition
        );

        float aspect = (float) ctx.getWindow().getWidth() / (float) ctx.getWindow().getHeight();
        Matrix4f projectionMatrix = new Matrix4f().perspective(
                (float) Math.toRadians(90.0f),  // FOV
                aspect,                         // Aspect ratio
                0.1f,
                100.0f
        );

        // Render the frame
        RenderContext renderContext = new RenderContext(
                ctx.window.getWidth(),
                ctx.window.getHeight(),
                (float) ctx.timer.getDeltaTime(),
                ctx.timer.getTotalTime()
        );
        renderContext.set3DContext(viewMatrix, projectionMatrix, new Vector3f(-3.0f), new Vector3f(1.0f), viewPosition);
        ctx.renderPipeline.render(renderContext);
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
        if (ctx.renderPipeline != null) {
            ctx.renderPipeline.destroy();
        }
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

