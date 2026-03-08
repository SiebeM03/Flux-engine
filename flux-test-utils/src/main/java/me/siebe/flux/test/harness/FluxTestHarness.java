package me.siebe.flux.test.harness;

import me.siebe.flux.api.ecs.Entity;
import me.siebe.flux.api.ecs.World;
import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.systems.EngineSystem;
import me.siebe.flux.api.systems.SystemManager;
import me.siebe.flux.core.HeadlessContextInjector;
import me.siebe.flux.test.implementations.event.RecordingEventBus;
import me.siebe.flux.test.implementations.event.TestEventBus;
import me.siebe.flux.test.implementations.input.FakeController;
import me.siebe.flux.test.implementations.input.FakeKeyboard;
import me.siebe.flux.test.implementations.input.FakeMouse;
import me.siebe.flux.test.implementations.renderer.FakeRenderPipeline;
import me.siebe.flux.test.implementations.time.FixedStepTimeProvider;
import me.siebe.flux.test.implementations.window.FakeWindow;
import me.siebe.flux.util.time.Timer;

import java.util.List;

/**
 * Headless test harness for deterministic engine simulation. Supports fixed timestep,
 * fake input/window/renderer, and system injection.
 * <p>
 * Example:
 * <pre>{@code
 * World world = World.create("test", 100);
 * EngineTestHarness h = EngineTestHarness.builder()
 *     .world(world)
 *     .fixedTimestep(1.0 / 60.0)
 *     .withFakeInput()
 *     .withFakeWindow()
 *     .withFakeRenderer()
 *     .build();
 * h.registerSystem(new MySystem());
 * h.initSystems();
 * world.createEntity(new Position(0, 0), new Velocity(1, 0));
 * h.runFrames(60);
 * // assert on world state
 * h.teardown();
 * }</pre>
 */
public final class FluxTestHarness {
    private final World world;
    private final SystemManager systemManager;
    private final FixedStepTimeProvider timeProvider;
    private final Timer timer;
    private final FakeWindow window;
    private final TestEventBus testEventBus;
    private final RecordingEventBus eventBus;
    private final Renderer renderer;
    private final FakeRenderPipeline fakePipeline;
    private final boolean withFakeInput;
    private FakeKeyboard fakeKeyboard;
    private FakeMouse fakeMouse;
    private FakeController fakeController;
    private boolean systemsInitialized;
    private boolean renderEachFrame;

    private FluxTestHarness(Builder builder) {
        this.world = builder.world;
        double step = builder.fixedTimestepSeconds > 0 ? builder.fixedTimestepSeconds : 1.0 / 60.0;
        this.timeProvider = new FixedStepTimeProvider(step);
        this.timer = new Timer(timeProvider);
        this.systemManager = new SystemManager();
        this.window = builder.withFakeWindow ? new FakeWindow() : null;
        this.testEventBus = new TestEventBus();
        this.eventBus = new RecordingEventBus(testEventBus);
        this.fakePipeline = builder.withFakeRenderer ? new FakeRenderPipeline() : null;
        this.renderer = fakePipeline != null ? new Renderer(fakePipeline) : null;
        this.withFakeInput = builder.withFakeInput;
        this.systemsInitialized = false;
        this.renderEachFrame = builder.renderEachFrame;

        HeadlessContextInjector.resetAppContext();
        HeadlessContextInjector.inject(
                window,
                timer,
                renderer,
                eventBus,
                systemManager
        );

        if (renderer != null) {
            renderer.setRenderContext(
                    new BaseRenderContext.Builder<>()
                            .camera(null)
                            .emptyRenderables()
                            .build()
            );
        }
    }

    /**
     * Creates an entity in the harness world with the given components. Convenience for {@code getWorld().createEntity(components)}.
     *
     * @param components initial components (nulls ignored)
     * @return the created entity
     */
    public Entity spawnEntity(Object... components) {
        return world.createEntity(components);
    }

    /**
     * Registers an engine system. Call before {@link #initSystems()} and {@link #runFrames(int)}.
     */
    public void registerSystem(EngineSystem system) {
        systemManager.registerEngineSystem(system);
    }

    /**
     * Initializes all registered systems. Call once after registering systems and before {@link #runFrames(int)}.
     */
    public void initSystems() {
        systemManager.init();
        systemsInitialized = true;
    }

    /**
     * Runs the given number of simulation frames with fixed timestep. Each frame: advances time,
     * updates timer, input frame boundaries, window, event bus flush, and system manager update.
     * If a fake renderer was configured and {@link Builder#renderEachFrame(boolean)} was true, renders each frame.
     *
     * @param frameCount number of frames to run
     */
    public void runFrames(int frameCount) {
        runFrames(frameCount, null);
    }

    /**
     * Like {@link #runFrames(int)} but invokes the callback each frame after {@code beginFrame()}
     * and before event flush and system update. Use to script input per frame (e.g. press W at frame 0, release at 30)
     * or to assert state during the run.
     *
     * @param frameCount number of frames to run
     * @param callback   optional; if non-null, called each frame with (frameIndex, this)
     */
    public void runFrames(int frameCount, PerFrameCallback callback) {
        if (!systemsInitialized) {
            initSystems();
        }
        for (int i = 0; i < frameCount; i++) {
            timeProvider.tick();
            timer.update();
            if (withFakeInput) {
                Input.endFrame();
            }
            if (window != null) {
                window.update();
            }
            if (withFakeInput) {
                Input.beginFrame();
            }
            if (callback != null) {
                callback.onFrame(i, this);
            }
            eventBus.flush();
            systemManager.update();
            if (renderEachFrame && renderer != null) {
                renderer.render();
            }
        }
    }

    public World getWorld() {
        return world;
    }

    public SystemManager getSystemManager() {
        return systemManager;
    }

    public Timer getTimer() {
        return timer;
    }

    public FixedStepTimeProvider getTimeProvider() {
        return timeProvider;
    }

    public FakeWindow getWindow() {
        return window;
    }

    /**
     * Returns the event bus (a {@link RecordingEventBus} so events are recorded for assertions).
     */
    public RecordingEventBus getEventBus() {
        return eventBus;
    }

    /**
     * Returns an immutable list of all events posted since the last {@link #clearRecordedEvents()} (or since build).
     */
    public List<RecordingEventBus.RecordedEvent> getRecordedEvents() {
        return eventBus.getRecordedEvents();
    }

    /**
     * Clears the list of recorded events. Use between runs or after setup to isolate assertions.
     */
    public void clearRecordedEvents() {
        eventBus.clearRecordedEvents();
    }

    public FakeRenderPipeline getFakeRenderPipeline() {
        return fakePipeline;
    }

    public FakeKeyboard getFakeKeyboard() {
        return fakeKeyboard;
    }

    public FakeMouse getFakeMouse() {
        return fakeMouse;
    }

    public FakeController getFakeController() {
        return fakeController;
    }

    // =================================================================================================================
    // Builder
    // =================================================================================================================
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private World world;
        private double fixedTimestepSeconds = 1.0 / 60.0;
        private boolean withFakeInput;
        private boolean withFakeWindow = true;
        private boolean withFakeRenderer = true;
        private boolean renderEachFrame = false;

        /** Sets the world to use. Required. */
        public Builder world(World world) {
            this.world = world;
            return this;
        }

        /** Sets the fixed timestep in seconds (e.g. 1.0/60.0 for 60 fps). */
        public Builder fixedTimestep(double seconds) {
            this.fixedTimestepSeconds = seconds;
            return this;
        }

        /** Installs fake keyboard, mouse, and controller and calls {@link Input#init}. */
        public Builder withFakeInput() {
            this.withFakeInput = true;
            return this;
        }

        /** Installs a fake window in the context (default: true). */
        public Builder withFakeWindow(boolean with) {
            this.withFakeWindow = with;
            return this;
        }

        /** Installs a fake render pipeline and renderer (default: true). */
        public Builder withFakeRenderer(boolean with) {
            this.withFakeRenderer = with;
            return this;
        }

        /** If true, {@link #runFrames} will call render each frame (default: false). */
        public Builder renderEachFrame(boolean render) {
            this.renderEachFrame = render;
            return this;
        }

        public FluxTestHarness build() {
            return new FluxTestHarness(this);
        }
    }
}
