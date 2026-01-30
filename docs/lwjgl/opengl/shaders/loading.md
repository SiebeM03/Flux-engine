# Shader loading and caching

Shaders are loaded and cached by **ShaderLoader**, which extends **AssetPool&lt;ShaderProgram&gt;**. You identify a
shader by a **base path** (no extension); the loader resolves `.vert` and `.frag` from that base.

## ShaderLoader

- **Package:** `me.siebe.flux.lwjgl.opengl.shader`
- **Singleton:** `ShaderLoader.get()`
- **Extends:** `AssetPool<ShaderProgram>` (from `flux-util`)

### Loading a shader

```java
ShaderProgram shader = ShaderLoader.get().load("shaders/gltf");
```

- **Parameter:** Base path without extension (e.g. `shaders/gltf`, `shaders/test`).
- **Resolution:** The loader looks for:
    - `{basePath}.vert` — vertex shader
    - `{basePath}.frag` — fragment shader
- **Caching:** The first `load(basePath)` compiles and links the program and stores it in the pool. Subsequent
  `load(basePath)` calls return the same cached instance.
- **Thread:** Shader creation (compile/link) must happen on the thread that has the OpenGL context. Typically, you call
  `load()` from the main/render thread.

### Where shader files are read from

1. **Normal load (no hot-reload):**  
   When `load(basePath)` creates a new program, it uses `resourceRoot == null`. Source is read from the **classpath**
   via `ClassLoader.getResourceAsStream(resourcePath)`. So for `shaders/gltf` the classpath must provide
   `shaders/gltf.vert` and `shaders/gltf.frag` (e.g. under `src/main/resources`).

2. **Hot-reload:**  
   When **ShaderHotReloader** triggers a reload, it calls `ShaderLoader.get().reload(basePath, resourceRoot)` with a
   filesystem `resourceRoot`. Source is then read from the **filesystem** at `resourceRoot.resolve(resourcePath)`.
   See [Hot reload](hot-reload.md).

### Reload (hot-reload path)

```java
boolean ok = ShaderLoader.get().reload("shaders/gltf", resourceRootPath);
```

- **Must be called on the OpenGL thread.**
- Compiles and links a new program from the files at `resourceRoot`, replaces the old program in the cache with this new
  program and destroys the old program.
- Returns `true` if reload succeeded, `false` if the shader was not cached or compilation/linking failed. On failure,
  the cache has no entry for that base path; the next `load()` will load again from the classpath (with
  `resourceRoot == null`).

You can not call `reload()` yourself; **ShaderHotReloader** does it when it detects file changes.
See [Hot reload](hot-reload.md).

## AssetPool (flux-util)

`ShaderLoader` delegates storage to **AssetPool**:

- **`load(identifier)`** — If the identifier is already in the pool, returns the cached asset. Otherwise, calls
  `create(identifier)`, puts the result in the pool, and returns it.
- **`create(identifier)`** — Implemented by `ShaderLoader` as `new ShaderProgram(filepath, null)` for normal classpath
  loading.
- **`removeAsset(identifier)`** / **`putAsset(identifier, asset)`** — Used internally by `reload()` to replace a cached
  shader.

Other asset types (e.g. textures, models) can use the same pattern by extending `AssetPool<T>` in their own loaders.

## Base path convention

- Use a path **without** file extension: `shaders/gltf`, not `shaders/gltf.vert`.
- Use forward slashes; they work for both classpath resources and typical filesystem layouts.
- The loader appends `.vert` and `.frag`; other extensions (e.g. `.glsl`) are not used by this loader.
