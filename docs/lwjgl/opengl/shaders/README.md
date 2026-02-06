# Shader System

The Flux engine’s shader system handles loading, compiling, linking, and using OpenGL shaders. It supports **file-based
shaders** (`.vert` / `.frag`), plus optional **hot-reload** during development.

## Overview

| Component                          | Purpose                                                                                                                                    |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| **ShaderLoader**                   | Singleton that loads and caches shader programs by base path (e.g. `shaders/gltf`). Extends `AssetPool<ShaderProgram>`.                    |
| **ShaderProgram**                  | A linked OpenGL program (vertex + fragment). Loads source from classpath or filesystem, compiles, links, and reflects attributes/uniforms. |
| **ShaderHotReloader**              | Optional engine system that watches shader files and triggers reloads on the OpenGL thread.                                                |
| **Shader uniform/attribute types** | `ShaderUniform`, `ShaderAttribute`, `ShaderDataType` — used for reflection and type-safe uploads.                                          |

## Documentation

- [Loading and caching](loading.md) — `ShaderLoader`, `AssetPool`, classpath vs filesystem, base path convention
- [Shader program](program.md) — `ShaderProgram` lifecycle, compilation, linking, reflection
- [Uniforms and attributes](uniforms-and-attributes.md) — `ShaderUniform`, `ShaderAttribute`, `ShaderDataType`, upload
  API
- [Hot reload](hot-reload.md) — Enabling and using shader hot-reload during development
- [Usage in the render pipeline](usage.md) — How render steps load and use shaders

## Quick start

**Load a file-based shader (typical in render steps):**

```java
ShaderProgram shader = ShaderLoader.get().load("shaders/gltf");
shader.bind();
shader.upload("uViewProj",viewProjMatrix);
shader.uploadTexture("uDiffuse",0);
shader.unbind();
```

**Shader files:** For base path `shaders/gltf`, the loader expects:

- `shaders/gltf.vert` — vertex shader
- `shaders/gltf.frag` — fragment shader

Place these under `src/main/resources` (or another resource root) so they are on the classpath, or use hot-reload with a
filesystem path.
