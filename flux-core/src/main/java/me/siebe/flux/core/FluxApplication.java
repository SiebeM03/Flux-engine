package me.siebe.flux.core;

import me.siebe.flux.api.event.common.FramebufferResizeEvent;
import me.siebe.flux.api.event.common.WindowResizeEvent;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.renderer.pipeline.RenderPipeline;
import me.siebe.flux.api.systems.SystemManager;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.core.system.StartupBanner;
import me.siebe.flux.core.system.SystemInfoService;
import me.siebe.flux.event.DefaultEventBus;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import me.siebe.flux.util.memory.NativeTracker;
import me.siebe.flux.util.system.ProvidableSystem;
import me.siebe.flux.util.time.Timer;

/**
 * The base class for all Flux applications.
 * <p>
 * A {@code FluxApplication} is the entry point for all Flux applications. It defines the lifecycle of a running engine instance, including initialization, game-loop execution, engine updates, game updates, and shutdown.
 * Game developers should extend this class and implement the abstract methods to hook into the engine lifecycle.
 * <p>
 * This class coordinates all high-level engine functionality, such as window management, OpenGL context creation, and resource initialization.
 * It ensures a consistent and predictable execution flow across all applications.
 *
 * <h3>Application Lifecycle</h3>
 * <ol>
 *   <li>Initialization: {@link #init()}</li>
 *   <li>Runtime Loop: {@link #run()}</li>
 *   <li>Shutdown: {@link #destroy()}</li>
 * </ol>
 */
public abstract class FluxApplication implements ProvidableSystem {
    private static final Logger logger = LoggerFactory.getLogger(FluxApplication.class, LoggingCategories.APPLICATION);

    // =================================================================================================================
    // Initialization related methods
    // =================================================================================================================
    /**
     * Initializes the application and all required engine and game systems.
     * <p>
     * This method is called by {@link FluxLauncher#main(String[])} to initialize the application.
     * It provides a consistent initialization sequence for all applications:
     * <ol>
     *   <li>Engine-level systems are initialized first: {@link #initEngineSystems()}</li>
     *   <li>Game-level systems are initialized afterward: {@link #initGameSystems()}</li>
     * </ol>
     * <p>
     * Any exceptions thrown during initialization will be caught and logged.
     */
    final void init() {
        logger.info("Initializing Application");

        try {
            SystemInfoService.populateStartupBanner();
            initEngineSystems();
            logger.info("Engine successfully initialized");
            StartupBanner.render();

            initGameSystems();
            logger.info("Game successfully initialized");

            // These engine systems are typically registered inside initGameSystems()
            AppContext.get().getSystemManager().init();
        } catch (Exception e) {
            logger.error("Something went wrong while initializing the application");
            throw e;
        }
    }

    /**
     * Initializes all engine-provided systems which must be ready before the game loop starts.
     * <p>
     * Systems initialized here include:
     * <ul>
     *   <li>Global timer</li>
     *   <li>Window creation (via {@link #createWindowBuilder()}) and initialization</li>
     *   <li>OpenGL context initialization</li>
     *   <li>Render pipeline creation and initialization</li>
     * </ul>
     */
    private void initEngineSystems() {
        logger.info("Initializing Engine Systems");

        AppContext.withContextNoReturn(ctx -> {
            // Event bus initialization
            ctx.eventBus = new DefaultEventBus();
            ctx.eventBus.getEventPoolRegistry().register(WindowResizeEvent.class, WindowResizeEvent::new);
            ctx.eventBus.getEventPoolRegistry().register(FramebufferResizeEvent.class, FramebufferResizeEvent::new);

            // Window initialization
            WindowBuilder windowBuilder = createWindowBuilder();
            ctx.window = windowBuilder.build();
            ctx.getWindow().init();

            // Timer initialization (can only be called after windowBuilder.build() is called (glfwInit())
            ctx.timer = new Timer(windowBuilder.getTimeProvider());

            // System manager initialization
            ctx.systemManager = new SystemManager();

            // Render pipeline initialization
            ctx.renderer = new Renderer(RenderPipeline.create());
        });
    }

    /**
     * Initializes game-specific systems which can depend on engine-provided systems.
     * <p>
     * This method is called immediately after all engine systems have been successfully initialized.
     * Game developers should initialize their own gameplay systems, resources, custom implementations, etc. here.
     * <p>
     * This method is invoked exactly once in the application lifecycle.
     */
    protected abstract void initGameSystems();


    // =================================================================================================================
    // Game loop related methods
    // =================================================================================================================
    /**
     * Starts the main application loop.
     * <p>
     * This method:
     * <ol>
     *   <li>Continuously loops until the window signals closure.</li>
     *   <li>Executes the game update logic via {@link #gameUpdate(AppContext)}</li>
     *   <li>Executes the engine update logic via {@link #engineUpdate(AppContext)}</li>
     * </ol>
     */
    final void run() {
        try {
            AppContext.withContextNoReturn(ctx -> {
                while (!ctx.getWindow().shouldClose()) {
                    gameUpdate(ctx);
                    engineUpdate(ctx);

                    ctx.getRenderer().render();
                }
            });
        } catch (Exception e) {
            logger.error("Error during runtime loop", e);
        }
    }

    /**
     * Performs engine-side per-frame updates.
     * <p>
     * This includes:
     * <ul>
     *   <li>Updating the global timer</li>
     *   <li>Updating the window</li>
     * </ul>
     * <p>
     * This method is called once per frame after the game update logic has been executed, this way systems such as rendering are guaranteed to have access to the latest game state.
     *
     * @param ctx the current application context containing engine state
     */
    private void engineUpdate(final AppContext ctx) {
        logger.trace("Updating Engine");

        ctx.getTimer().update();
        ctx.getTimer().print();

        ctx.getWindow().update();

        ctx.getEventBus().flush();

        // MUST BE LAST UPDATE!!
        ctx.getSystemManager().update();
    }

    /**
     * Performs game-side per-frame updates.
     * <p>
     * This method is called once per frame before the engine systems update.
     * Game developers should implement their own update logic such as game logic, ECS updates, etc. here.
     *
     * @param ctx the current application context containing engine state
     */
    protected abstract void gameUpdate(final AppContext ctx);


    // =================================================================================================================
    // Shutdown related methods
    // =================================================================================================================
    /**
     * Shuts down the application and all associated systems and resources.
     * <p>
     * This method ensures a clean shutdown sequence:
     * <ol>
     *   <li>Engine systems are destroyed first: {@link #destroyEngineSystems(AppContext)}</li>
     *   <li>Game systems are destroyed afterward: {@link #destroyGameSystems()}</li>
     * </ol>
     * <p>
     * This method is called exactly once, regardless of how the application loop exits (normal closure, exception, etc.).
     */
    final void destroy() {
        logger.info("Application is shutting down, cleaning up resources");
        AppContext.withContextNoReturn(ctx -> {
            destroyEngineSystems(ctx);
            destroyGameSystems();
        });

        NativeTracker.report();
    }

    /**
     * Cleans up engine systems such as the window and any other engine-provided systems.
     * <p>
     * This method cannot be overridden, as all engine-level cleanup must occur
     * in a consistent and predictable order.
     *
     * @param ctx the current application context containing engine state
     */
    private void destroyEngineSystems(final AppContext ctx) {
        ctx.getWindow().destroy();
        ctx.getRenderer().destroy();

        ctx.getSystemManager().destroy();
    }

    /**
     * Cleans up game-specific systems.
     * <p>
     * This method is called after all engine systems have been cleaned up.
     * Game developers should release their custom resources, destroy gameplay
     * systems, and free engine-dependent resources here.
     */
    protected abstract void destroyGameSystems();

    // =================================================================================================================
    // Instance lifecycle methods
    // =================================================================================================================
    /**
     * Creates a new Flux application instance.
     * <p>
     * Subclasses should typically not perform any initialization here; instead, they should use {@link #initGameSystems()} to prepare any game logic or assets.
     */
    protected FluxApplication() {
    }

    /**
     * Creates a {@link WindowBuilder} used to construct the main application window.
     * <p>
     * Game developers must implement this method to customize the configuration of the application window (such as title, size, vsync, etc.).
     * <p>
     * The returned builder is used during engine initialization and must produce a valid window instance.
     * <p>
     * Example implementation:
     * <pre>{@code
     * protected WindowBuilder createWindowBuilder() {
     *     return Window.builder(WindowPlatform.GLFW)
     *             .title("My Game")
     *             .width(1920, FLUX_DONT_CARE, FLUX_DONT_CARE)
     *             .height(1080, FLUX_DONT_CARE, FLUX_DONT_CARE)
     *             .mode(WindowMode.WINDOWED)
     *             .vsync(false);
     * }
     * }</pre>
     *
     * @return a {@link WindowBuilder} configured by the game developer
     */
    protected abstract WindowBuilder createWindowBuilder();
}

