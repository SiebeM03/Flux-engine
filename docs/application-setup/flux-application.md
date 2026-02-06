# Flux Application

**FluxApplication** is the abstract base class for all Flux applications. It defines the engine lifecycle (engine
systems, window, render pipeline) and provides hooks for game-level initialisation, per-frame updates, and shutdown.

See also: [Flux Launcher](flux-launcher.md), [Implementing your application](implementing-your-app.md).

## Type and package

- **Package:** `me.siebe.flux.core`
- **Implements:** **ProvidableSystem** (flux-util), so it can be discovered by **SystemProvider** when used as a custom
  implementation

Your game subclass must **not** be in package `me.siebe.flux` so that **FluxLauncher** treats it as a custom
implementation.

## Lifecycle overview

| Phase       | Engine (FluxApplication)                                                                                    | Game (your override)                                                             |
|-------------|-------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| **Init**    | **initEngineSystems()**: timer, event bus, window (from **createWindowBuilder()**), OpenGL, renderer.       | **initGameSystems()** — called once after engine systems are ready.              |
| **Run**     | Each frame: **engineUpdate(ctx)** (timer, window update, event flush, system manager), **Renderer::render** | **gameUpdate(ctx)** — called once per frame **before** engine update and render. |
| **Destroy** | **destroyEngineSystems(ctx)** (window destroy, **systemManager.destroy()**).                                | **destroyGameSystems()** — called once after engine systems are destroyed.       |

Engine methods (**initEngineSystems**, **engineUpdate**, **destroyEngineSystems**) are **final** or **private**; you
only implement the game hooks and **createWindowBuilder()**.

## Abstract methods you must implement

| Method                         | When it runs                                 | Purpose                                                                                                                                |
|--------------------------------|----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| **createWindowBuilder()**      | During **initEngineSystems()**               | Return a configured **WindowBuilder** (title, size, mode, vsync, platform). The engine calls **build()** and **init()** on the window. |
| **initGameSystems()**          | Once, after engine systems are initialised   | Set up your render context, pipeline steps, assets, and game logic.                                                                    |
| **gameUpdate(AppContext ctx)** | Every frame, before engine update and render | Per-frame game logic (camera, input, physics, etc.).                                                                                   |
| **destroyGameSystems()**       | Once, after engine systems are destroyed     | Release your resources and clean up.                                                                                                   |

## createWindowBuilder()

Called during **initEngineSystems()**. The engine uses the returned builder to create and initialise the main window.
Example:

```java

@Override
protected WindowBuilder createWindowBuilder() {
    return Window.builder(WindowPlatform.GLFW)
            .title("Demo Game")
            .mode(WindowMode.WINDOWED)
            .vsync(false);
}
```

- **Window.builder(WindowPlatform)** is provided by **SystemProvider** (e.g. GLFW implementation from flux-lwjgl).
- Configure title, dimensions, mode (windowed/fullscreen), vsync, etc. on the builder before returning.

## initGameSystems()

Called **once** after **initEngineSystems()** and **StartupBanner**, and before **systemManager.init()**. Use it to:

- Obtain **Renderer** from **AppContext** and set your **RenderContext**, add **RenderPipeline** steps.
- Load models, textures, and other assets.
- Register custom **EngineSystem** instances via **AppContext.get().getSystemManager().registerEngineSystem(...)** if needed.
- Initialise game-specific subsystems (e.g. camera, input handlers).

Engine systems you register here are initialised when **systemManager.init()** runs immediately after
**initGameSystems()**.

## gameUpdate(AppContext ctx)

Called **every frame** before **engineUpdate(ctx)**, **systemManager.update()**, and rendering. Use it for:

- Camera updates, input handling, physics, gameplay logic.
- **ctx** gives access to **Window**, **Timer**, **Renderer**, **EventBus**, and **SystemManager** (e.g. for registering engine systems).

Rendering uses the latest state because it runs after **gameUpdate** and **engineUpdate**.

## destroyGameSystems()

Called **once** after **destroyEngineSystems(ctx)** (window destroyed, **systemManager.destroy()**). Use it to:

- Release textures, buffers, and other GPU or native resources you created.
- Close files, stop threads, and clean up game subsystems.

Do not destroy engine-owned resources (e.g. the window); the engine has already destroyed them.

## Optional: engine systems

The **SystemManager** (obtained via **AppContext.get().getSystemManager()**) exposes:

- **registerEngineSystem(EngineSystem)**
- **unregisterEngineSystem(Class&lt;? extends EngineSystem&gt;)**

You can register custom **EngineSystem** implementations (e.g. **ShaderHotReloader**) in **initGameSystems()**. Their
**init()** runs in **systemManager.init()**, **update()** in **systemManager.update()**, and **destroy()** in
**systemManager.destroy()**.
