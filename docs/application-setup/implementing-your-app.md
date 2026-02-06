# Implementing your application

To build a Flux game, create a single class that extends **FluxApplication**, implement all abstract methods, and
ensure it is discoverable by **FluxLauncher**. This page describes the requirements and gives an example from the
demo-game module.

See
also: [Flux Launcher](flux-launcher.md), [Flux Application](flux-application.md), [Run configurations](run-configurations.md).

## Requirements

1. **Exactly one subclass** of **FluxApplication** on the classpath at runtime. If there are zero, **FluxLauncher**
   throws **ApplicationException** (no implementation found). If there are two or more, it throws (multiple
   implementations).

2. **Package:** Your class must **not** be in a package that starts with **me.siebe.flux**. The launcher uses
   **SystemProvider.provideAll(FluxApplication.class, SystemProviderType.CUSTOM_ONLY)**, which only considers
   implementations outside the engine package. Put your application in a game package.

3. **Constructor:** The provider instantiates your class via reflection with a **no-arg constructor**. A default
   constructor is sufficient; do not require arguments.

4. **No META-INF/services:** Discovery is done by **classpath scanning** (ClassGraph), not Java’s **ServiceLoader**. You
   do **not** need a `META-INF/services/me.siebe.flux.core.FluxApplication` file.

## Example: GameApplication (demo-game)

The demo-game module provides a minimal but complete implementation:

```java
package game.core;

public class GameApplication extends FluxApplication {
    private static final Logger logger = LoggerFactory.getLogger(GameApplication.class, GameCategories.APPLICATION);

    private CameraSetup cameraSetup;

    @Override
    protected void initGameSystems() {
        logger.info("Initializing Game Systems");

        // Get the Renderer instance
        Renderer renderer = AppContext.get().getRenderer();
        // Set a custom RenderContext (extends `me.siebe.flux.api.renderer.context`)
        renderer.setRenderContext(new CustomRenderContext());
        // Add steps to the RenderPipeline
        renderer.getPipeline().addStep(new GltfStep());

        // Set the renderables list in the render context to an empty list, load a GLTF model and add it to this list
        renderer.getRenderContext().setRenderables(new ArrayList<>());
        renderer.getRenderContext().getRenderables().add(GltfLoader.get().load("models/damaged-helmet/scene.gltf"));

        // Initialize game-specific systems
        this.cameraSetup = new CameraSetup();
        this.cameraSetup.init();
    }

    @Override
    protected void gameUpdate(final AppContext ctx) {
        logger.trace("Updating Game");
        this.cameraSetup.update(ctx);
    }

    @Override
    protected void destroyGameSystems() {
        logger.info("Destroying Game Systems");
        this.cameraSetup.destroy();
    }

    @Override
    protected WindowBuilder createWindowBuilder() {
        return Window.builder(WindowPlatform.GLFW)
                .title("Demo Game")
                .mode(WindowMode.WINDOWED)
                .vsync(false);
    }
}
```

- **createWindowBuilder()** — Returns a GLFW window builder with title, windowed mode, and vsync off.
- **initGameSystems()** — Sets a custom **RenderContext**, adds **GltfStep** to the pipeline, loads a GLTF model, and
  initialises a temporary camera setup.
- **gameUpdate()** — Delegates to the camera setup for per-frame updates.
- **destroyGameSystems()** — Logs shutdown; in a real game you would release GPU resources and other handles here.

## Checklist

| Item                                                         | Check                                                      |
|--------------------------------------------------------------|------------------------------------------------------------|
| One class extends **FluxApplication**                        | Only one such class in your game (and its dependencies).   |
| Package is not under **me.siebe.flux**                       | e.g. **game.core**.                                        |
| No-arg constructor available                                 | Default constructor or explicit public no-arg.             |
| **createWindowBuilder()** implemented                        | Return a configured **WindowBuilder**.                     |
| **initGameSystems()** implemented                            | Set up render context, pipeline, assets, and game systems. |
| **gameUpdate(AppContext)** implemented                       | Per-frame logic.                                           |
| **destroyGameSystems()** implemented                         | Release resources.                                         |
| Main class at runtime is **me.siebe.flux.core.FluxLauncher** | See [Run configurations](run-configurations.md).           |
