# Shader program

A **ShaderProgram** represents a linked OpenGL program (vertex shader + fragment shader). It is created by
**ShaderLoader** from a base path. This page describes the file-based **ShaderProgram** in `flux-lwjgl`.

## Creation

- **By ShaderLoader:**  
  `ShaderLoader.get().load("shaders/gltf")` creates a `ShaderProgram` with:
    - `basePath` → resolves to `shaders/gltf.vert` and `shaders/gltf.frag`
    - `resourceRoot == null` → source read from classpath; non-null → read from filesystem (used by hot-reload).

- **Constructor (internal):**  
  `ShaderProgram(String basePath, Path resourceRoot)`:
    1. Loads vertex and fragment source (classpath or `resourceRoot`).
    2. Compiles each shader with OpenGL.
    3. Links the program.
    4. Reflects attributes and uniforms and stores them in maps.

If any step fails, a **ShaderException** is thrown (e.g. file not found, compile error, link error).

## Lifecycle

| Method        | Description                                                                                                                         |
|---------------|-------------------------------------------------------------------------------------------------------------------------------------|
| **bind()**    | Makes this program current: `glUseProgram(programId)`. Also sets the static “active shader” used by `getActiveShader()`.            |
| **unbind()**  | Clears current program: `glUseProgram(0)`.                                                                                          |
| **destroy()** | Unbinds (if this program is active) and deletes the OpenGL program. Called by the loader when replacing a shader during hot-reload. |

After creation, you typically **bind()** before drawing, upload uniforms, issue draw calls, then **unbind()** (or bind
another shader).

## Compilation and linking

- **Compile:** Each of the vertex and fragment sources is compiled with `glCompileShader`. On failure, the shader is
  deleted and a **ShaderException** is thrown with the OpenGL compile log.
- **Link:** The two shaders are attached to a new program and linked with `glLinkProgram`. On failure, program and
  shaders are deleted and a **ShaderException** is thrown with the link log.
- **Cleanup:** After a successful link, the individual shaders are detached and deleted; only the program ID is kept.

So a **ShaderProgram** instance always holds a single linked program; there is no “partial” or unlinked state exposed.

## Reflection (attributes and uniforms)

After linking, the program **reflects** active attributes and uniforms from OpenGL:

- **Attributes:** `glGetProgrami(GL_ACTIVE_ATTRIBUTES)` and `glGetActiveAttrib` / `glGetAttribLocation` for each. Stored
  as **ShaderAttribute** (name, location, ShaderDataType, size).
- **Uniforms:** `glGetProgrami(GL_ACTIVE_UNIFORMS)` and `glGetActiveUniform` / `glGetUniformLocation` for each. Stored
  as **ShaderUniform** (name, location, glType, size).

These maps are used by:

- **getUniform(name)** — Returns the **ShaderUniform** for that name, or `null` if not found (e.g. optimized out).
- **upload(name, value)** — Looks up the uniform and dispatches to the appropriate **ShaderUniform** upload method (
  matrix, vector, scalar, int array). See [Uniforms and attributes](uniforms-and-attributes.md).
- **uploadTexture(name, slot)** — Uploads a texture slot (e.g. `glUniform1i`) for a sampler uniform.

Attributes are used mainly by the vertex pipeline (e.g. **BufferLayout** / **BufferElement** with **ShaderDataType**) to
match vertex buffer layout to shader inputs; the program does not upload attributes directly — that is done via vertex
buffers and vertex attribute pointers.

## Active shader

`ShaderProgram.getActiveShader()` returns the **ShaderProgram** that was last passed to **bind()**, or `null` if none.
This is a static hook for code that needs to know the currently bound program without holding a reference.
