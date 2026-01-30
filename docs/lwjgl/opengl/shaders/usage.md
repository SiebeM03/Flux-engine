# Shader usage in the render pipeline

Render steps typically obtain a shader from **ShaderLoader**, bind the shader, upload per-frame uniforms, issue draws,
then unbind. With **ShaderHotReloader** enabled, they should re-query the loader each frame so that a reloaded shader is
used on the next frame.

## Obtaining the shader

Use the same base path every time so the loader can cache (and hot-reload) one program per step:

```java
ShaderProgram shader = ShaderLoader.get().load("shaders/gltf");
```

Do **not** cache the **ShaderProgram** reference across frames if you use hot-reload; the old program may be destroyed
when a reload happens. Instead, call **load()** each frame (e.g. in **prepare()**) so you always get the current cached
program (which may have been replaced by a reload). The cost of **load()** when the shader is already cached is just a
map lookup.

## Typical flow in a RenderStep

1. **init()**  
   Optionally load the shader once and create any static resources (e.g. **VertexArray**). With hot-reload you will
   still re-query the shader in **prepare()** so the initial load is for convenience only.

2. **prepare(BaseRenderContext context)**
    - Re-obtain the shader: `shader = ShaderLoader.get().load("shaders/gltf");`
    - Upload per-frame uniforms (e.g. view-projection, time, light direction):
        - `shader.upload("uViewProj", context.getCamera().getViewProjectionMatrix());`
        - `shader.upload("uLightDir", lightDir);`
        - `shader.uploadTexture("uDiffuse", 0);` if the shader uses a sampler.

3. **execute(BaseRenderContext context)**
    - `shader.bind();`
    - Submit draws (e.g. `context.getRenderables().forEach(Renderable::render);` or **OpenGLState.drawElements(
      vertexArray)**).
    - `shader.unbind();`

4. **destroy()**  
   Release step-owned resources. Do **not** destroy the shader; **ShaderLoader** owns it and will destroy it on reload
   or when the loader is no longer used.

## Example: GltfStep

- **getShader()** returns `ShaderLoader.get().load("shaders/gltf")`.
- **init()** stores that in a field (optional).
- **prepare()** sets `this.shader = getShader()` (so hot-reload is picked up), then uploads `uViewProj` and `uLightDir`.
- **execute()** binds the shader, renders all renderables, unbinds.

## Example: OriginStep

- Uses `ShaderLoader.get().load("shaders/test")`.
- **prepare()** sets `this.shader = getShader()` and uploads `uViewProj` in **execute()**.
- **execute()** binds, draws the origin **VertexArray**, unbinds.
- Vertex layout uses **BufferElement** with **ShaderDataType.Float3** / **Float4** to match the test shader’s
  attributes.

## Uniform names and types

Match the names and types to what the shader declares. If a uniform is missing or optimized out, **getUniform(name)**
returns `null` and **upload()** logs a warning and does nothing. Use the same names as in your `.vert` / `.frag` (e.g.
`uViewProj`, `uModelMatrix`, `uLightDir`, `uDiffuse`).

## Hot-reload and the main loop

**ShaderHotReloader** is an **EngineSystem** that:

- **init()** — Starts the file watcher (if not already started).
- **update()** — Calls **processPendingReloads()** on the OpenGL thread.

So as long as the application runs the engine systems each frame, pending shader reloads are applied at the start of the
frame. Render steps that call **ShaderLoader.get().load(basePath)** in **prepare()** will then use the new program for
that frame. See [Hot reload](hot-reload.md).
