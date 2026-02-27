# Uniforms and attributes

The shader system uses **ShaderUniform**, **ShaderAttribute**, and **ShaderDataType** to represent reflected inputs and
to upload uniform values with type checking.

## ShaderDataType

**Package:** `me.siebe.flux.opengl.shader`

Enum of GLSL-like types used for both attributes and (where applicable) uniforms:

| Type                  | Components | OpenGL type   | Typical use             |
|-----------------------|------------|---------------|-------------------------|
| Float                 | 1          | GL_FLOAT      | Scalars                 |
| Float2                | 2          | GL_FLOAT_VEC2 | vec2                    |
| Float3                | 3          | GL_FLOAT_VEC3 | vec3 (position, normal) |
| Float4                | 4          | GL_FLOAT_VEC4 | vec4 (color, tangent)   |
| Mat2                  | 4          | GL_FLOAT_MAT2 | mat2                    |
| Mat3                  | 9          | GL_FLOAT_MAT3 | mat3 (normal matrix)    |
| Mat4                  | 16         | GL_FLOAT_MAT4 | mat4 (MVP, model)       |
| Int, Int2, Int3, Int4 | 1–4        | GL_INT_*      | ivec*                   |
| Bool                  | 1          | GL_BOOL       | bool                    |

Methods:

- **getComponentSize()** — Bytes per component (e.g. 4 for float).
- **getComponentCount()** — Number of components (e.g. 3 for Float3).
- **getTotalByteSize()** — `componentSize * componentCount` (used by **BufferElement** for vertex layout).
- **getOpenGLType()** — OpenGL type constant.
- **fromOpenGLType(int)** — Maps an OpenGL type constant back to a **ShaderDataType** (used after reflection).

## ShaderUniform

**Package:** `me.siebe.flux.opengl.shader`

Record: `ShaderUniform(String name, int location, int glType, int size)`.

- **name** — Uniform name as in the shader.
- **location** — From `glGetUniformLocation`.
- **glType** — OpenGL type (e.g. `GL_FLOAT_VEC3`, `GL_FLOAT_MAT4`, `GL_SAMPLER_2D`).
- **size** — Array length for uniform arrays; otherwise 1.

You usually do not construct **ShaderUniform** yourself; they are created during **ShaderProgram** reflection. You use
them indirectly via **ShaderProgram**:

- **getUniform(name)** — Returns the **ShaderUniform** or `null`.
- **upload(name, value)** — Dispatches to the correct upload method (see below).
- **uploadTexture(name, slot)** — For sampler uniforms; uploads the texture slot index.

### Upload methods (on ShaderUniform)

Each method checks that the uniform is active (location ≥ 0) and that the OpenGL type matches:

- **upload(Matrix4f)** / **upload(Matrix3f)** — mat4 / mat3 (JOML).
- **upload(Vector4f)** / **upload(Vector3f)** / **upload(Vector2f)** — vec4 / vec3 / vec2 (JOML).
- **upload(float)** / **upload(int)** — Scalars.
- **upload(int[])** — Integer array (e.g. `glUniform1iv`).
- **uploadTexture(int slot)** — For `sampler2D` / `samplerCube`; uploads the texture unit index.

Type mismatch or invalid location throws **ShaderException** or **IllegalArgumentException**.

### Upload from ShaderProgram

**ShaderProgram.upload(String name, Object value)** accepts:

- `Matrix4f`, `Matrix3f`
- `Vector4f`, `Vector3f`, `Vector2f`
- `Float`, `Integer`
- `int[]`

and dispatch to the corresponding **ShaderUniform** method. Any other type throws **ShaderException**. For samplers use
**uploadTexture(name, slot)**.

## ShaderAttribute

**Package:** `me.siebe.flux.opengl.shader`

Record: `ShaderAttribute(String name, int location, ShaderDataType type, int size)`.

- **name** — Attribute name as in the shader (e.g. `aPos`, `aNormal`).
- **location** — From `glGetAttribLocation`.
- **type** — **ShaderDataType** (from OpenGL type via **ShaderDataType.fromOpenGLType**).
- **size** — Component count for arrays; usually 1.

Attributes are created during **ShaderProgram** reflection. They are not “uploaded” by the shader API; they are fed by
**vertex buffers** and **VertexArray** / **BufferLayout**. The **BufferElement** type uses **ShaderDataType** so that
vertex layout (offset, stride, component count) matches the shader’s `in` declarations. See the vertex/OpenGL docs for
**BufferLayout** and **VertexArray**.

## Vertex layout and BufferElement

In **flux-lwjgl**, **BufferElement** takes a **ShaderDataType** so that the CPU-side layout matches the shader:

```java
BufferLayout layout = new BufferLayout(
        new BufferElement("aPos", ShaderDataType.Float3, false),
        new BufferElement("aColor", ShaderDataType.Float4, false)
);
```

The same names and types should appear in the vertex shader (e.g. `in vec3 aPos; in vec4 aColor;`). The program’s
reflected **ShaderAttribute** list can be used to validate or debug that mapping.
