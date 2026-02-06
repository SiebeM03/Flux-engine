# Flux Launcher

**FluxLauncher** is the main entry point for every Flux application. Its `main(String[])` discovers your
**FluxApplication** implementation, and runs the init → run → destroy lifecycle.

See also: [Flux Application](flux-application.md), [Implementing your application](implementing-your-app.md).

## Entry point

- **Package:** `me.siebe.flux.core`
- **Main class:** `me.siebe.flux.core.FluxLauncher`
- **Method:** `public static void main(String[] args)`

Always run the **engine** entry point, not your application class. The launcher is responsible for finding and running
your single **FluxApplication** subclass.

## Discovery

The launcher uses **SystemProvider** to find your application:

```java
List<FluxApplication> providers = SystemProvider.provideAll(FluxApplication.class, SystemProviderType.CUSTOM_ONLY);
```

- **CUSTOM_ONLY** means only implementations **outside** the engine package (`me.siebe.flux`) are considered. So your
  game’s subclass (e.g. in package `game.core`) is found; engine classes are ignored.
- Discovery is done via **classpath scanning** (ClassGraph), not `META-INF/services`. You do not need to register your
  application in a service file.
- The launcher then:
    - Throws **ApplicationException** if **zero** implementations are found (`noAppProviderImplementationFound`).
    - Throws **ApplicationException** if **more than one** implementation is found (
      `multipleAppProviderImplementationFound`).
    - Otherwise, takes the **first** (and only) implementation.

So you must have **exactly one** class that extends **FluxApplication** and is not in `me.siebe.flux`.

## Lifecycle sequence

The launcher then runs the full lifecycle:

1. **app.init()** — Initialises engine systems (timer, window, OpenGL, event bus, render pipeline), then calls your
   **initGameSystems()** and initialises the **SystemManager** (engine systems).
2. **app.run()** — Main loop: while the window is open, calls **gameUpdate(ctx)**, then **engineUpdate(ctx)**, then
   **systemManager.update()**, then **AppContext.get().getRenderer().render()**.
3. **app.destroy()** — Destroys engine systems (window, system manager), then calls your **destroyGameSystems()**.

Any exception thrown during init is propagated (and typically terminates the process). Exceptions during the run loop
are caught and logged; destroy is still executed when the loop exits (e.g. window closed).

## Summary

| Step | Action                                                                                             |
|------|----------------------------------------------------------------------------------------------------|
| 1    | Discover **FluxApplication** via **SystemProvider.provideAll(FluxApplication.class, CUSTOM_ONLY)** |
| 2    | Validate exactly one implementation; else throw **ApplicationException**                           |
| 3    | **app.init()**                                                                                     |
| 4    | **app.run()**                                                                                      |
| 5    | **app.destroy()**                                                                                  |
