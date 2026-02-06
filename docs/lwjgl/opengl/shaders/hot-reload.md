# Shader hot reload

The Flux engine supports hot-reloading of vertex and fragment shaders during development. When you save a `.vert` or `.frag` file on disk, the corresponding shader program is recompiled and the cache is updated so the next frame uses the new shader. This document describes how the system works and how to enable it.

See also: [Loading and caching](loading.md), [Usage in the render pipeline](usage.md).

## Overview

- A background thread watches configured **resource root** directories for changes to `.vert` and `.frag` files.
- When a change is detected, a **pending reload** (base path + resource root) is queued.
- On the next frame, the main (OpenGL) thread runs **ShaderHotReloader.processPendingReloads()**: each pending reload calls **ShaderLoader.get().reload(basePath, resourceRoot)**. The old program is removed from the cache and destroyed; a new one is compiled from the files on disk and stored in the cache.
- Render steps that call **ShaderLoader.get().load(basePath)** each frame (e.g. in **prepare()**) automatically get the new program after a reload.

Hot reload is **optional**. If you do not start a **ShaderHotReloader**, shaders are loaded once from the classpath and cached as before (see [Loading and caching](loading.md)).

## Enabling hot reload

### Via VM argument

Set the system property **`flux.shader.hotreload.paths`** to a comma-separated list of **watch root** paths (relative to the process working directory). At startup, **ShaderLoader** checks this property; if set, it creates a **ShaderHotReloader** with those paths and registers it as an **EngineSystem**.

Example (run from project root):

```text
-Dflux.shader.hotreload.paths=demo-game/src/main/resources,flux-renderer-3d/src/main/resources
```

Each path is resolved against the current working directory. Only directories that exist are watched. The watcher then looks for changes under those directories; when a `.vert` or `.frag` file is modified (or created), the corresponding **base path** (e.g. `shaders/gltf` for `shaders/gltf.vert`) is queued for reload using that watch root as the **resource root** for **ShaderLoader.reload(basePath, resourceRoot)**.

### Via code

Create a **ShaderHotReloader** with one or more watch roots and register it with the application:

```java
// Paths relative to current working directory
ShaderHotReloader hotReloader = new ShaderHotReloader(
    "demo-game/src/main/resources",
    "flux-renderer-3d/src/main/resources"
);
AppContext.get().getSystemManager().registerEngineSystem(hotReloader);
```

Or with absolute **Path** instances:

```java
ShaderHotReloader hotReloader = new ShaderHotReloader(List.of(
    Paths.get("flux-renderer-3d/src/main/resources").toAbsolutePath()
));
AppContext.get().getSystemManager().registerEngineSystem(hotReloader);
```

**ShaderHotReloader** implements **EngineSystem**: **init()** starts the watch thread, **update()** runs **processPendingReloads()** on the main thread (OpenGL thread), and **destroy()** stops the watch thread.

## Watch roots and base path resolution

- **Watch roots** are directories that are recursively registered with a **WatchService** for `ENTRY_MODIFY` and `ENTRY_CREATE`. Only files are considered; directory events are ignored.
- When a file under a watch root changes, the path is relativized against that watch root. If the relative path ends with `.vert` or `.frag`, the **base path** is the path without the extension (e.g. `shaders/gltf.vert` → `shaders/gltf`). Forward slashes are used.
- The **resource root** for the reload is the watch root that contained the changed file. So shaders are recompiled from the **filesystem** at `resourceRoot.resolve(basePath + ".vert")` and `".frag"`, not from the classpath.
- If no watch roots are valid directories, **start()** logs an error and does not start the watcher.

## Threading and OpenGL

- **Watch loop** runs on a daemon background thread. It only enqueues **PendingReload** entries; it does not touch OpenGL or the shader cache.
- **processPendingReloads()** must run on the **OpenGL thread** (e.g. each frame in the game loop). It calls **ShaderLoader.get().reload(basePath, resourceRoot)**, which compiles and links on the current thread and updates the cache. If you use **ShaderHotReloader** as an **EngineSystem**, **update()** runs on the main thread, which is typically the OpenGL thread.
- After a failed reload (e.g. compile error), the cache has no entry for that base path. The next **load(basePath)** will create a new program from the **classpath** again (as if hot-reload were not used for that shader).
