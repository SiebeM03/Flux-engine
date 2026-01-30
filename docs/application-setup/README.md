# Application setup

The Flux engine uses a single entry point (**FluxLauncher**) and an abstract application class (**FluxApplication**)
that your game extends. The launcher discovers your implementation via classpath scanning and runs the init → run →
destroy lifecycle. This section describes how to set up and run a Flux-based game.

## Overview

| Component             | Purpose                                                                                                                                                                                                |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **FluxLauncher**      | Entry point with `main(String[])`. Discovers your `FluxApplication` subclass, stores it in **AppContext**, and runs init → run → destroy.                                                              |
| **FluxApplication**   | Abstract base class for your game. Defines engine lifecycle (engine systems, window, render pipeline) and hooks: `initGameSystems()`, `gameUpdate()`, `destroyGameSystems()`, `createWindowBuilder()`. |
| **AppContext**        | Holds the current application instance, window, timer, and provides access to **Renderer** and **EventBus**. Set by the launcher before init.                                                          |
| **Run configuration** | IntelliJ Application run config (e.g. `.idea/runConfigurations/Flux_Launcher.xml`) or Maven: main class **me.siebe.flux.core.FluxLauncher**, module/artifact = your game.                              |

## Documentation

- [Flux Launcher](flux-launcher.md) — Entry point, discovery via **SystemProvider**, lifecycle sequence
- [Flux Application](flux-application.md) — **FluxApplication** lifecycle, engine vs game init/update/destroy, abstract
  methods
- [Implementing your application](implementing-your-app.md) — Extending **FluxApplication**, package and constructor
  requirements, example from demo-game
- [Run configurations](run-configurations.md) — Maven main class and shade plugin, IntelliJ run config and
  **Flux_Launcher.xml**, VM parameters, dependencies

## Quick start

**1. Create a single class that extends FluxApplication** (package must not start with `me.siebe.flux`):

```java
package game.core;

import me.siebe.flux.api.application.AppContext;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.api.window.WindowMode;
import me.siebe.flux.api.window.WindowPlatform;
import me.siebe.flux.core.FluxApplication;

public class GameApplication extends FluxApplication {

    @Override
    protected void initGameSystems() {
        // Configure renderer, load assets, register pipeline steps
    }

    @Override
    protected void gameUpdate(final AppContext ctx) {
        // Per-frame game logic
    }

    @Override
    protected void destroyGameSystems() {
        // Release resources
    }

    @Override
    protected WindowBuilder createWindowBuilder() {
        return Window.builder(WindowPlatform.GLFW)
                .title("My Game")
                .mode(WindowMode.WINDOWED)
                .vsync(false);
    }
}
```

**2. Run with main class `me.siebe.flux.core.FluxLauncher`** (not your application class). In IntelliJ: Application run
configuration, main class **me.siebe.flux.core.FluxLauncher**, module = your game module. From Maven: set
`<mainClass>me.siebe.flux.core.FluxLauncher</mainClass>` in the shade plugin (or exec plugin) and run the JAR or
`mvn exec:java`.

**3. Dependencies:** Your game module must depend on **flux-core** (and any other Flux modules you use, e.g.
**flux-renderer-3d**).

You must have **exactly one** subclass of **FluxApplication** on the classpath (and it must not be in `me.siebe.flux`).
No `META-INF/services` file is required for the application.
