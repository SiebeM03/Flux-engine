package me.siebe.flux.lwjgl.opengl.shader.builder;

import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a shader varying definition for programmatic shader creation.
 * <p>
 * Varyings are values passed from the vertex shader to the fragment shader.
 * They are interpolated across the primitive.
 */
public class ShaderVaryingDef {
    private final String name;
    private final ShaderDataType type;

    /**
     * Creates a new shader varying definition.
     *
     * @param name the varying name (typically prefixed with 'v_' or 'f_')
     * @param type the data type of the varying
     */
    public ShaderVaryingDef(@NotNull String name, @NotNull ShaderDataType type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Varying name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Varying type cannot be null");
        }
        this.name = name;
        this.type = type;
    }

    /**
     * Gets the varying name.
     *
     * @return the varying name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets the varying data type.
     *
     * @return the data type
     */
    @NotNull
    public ShaderDataType getType() {
        return type;
    }

    /**
     * Converts this varying definition to GLSL code for vertex shader (out).
     *
     * @return the GLSL varying declaration for vertex shader
     */
    public String toGLSLVertex() {
        return String.format("out %s %s;", typeToGLSL(type), name);
    }

    /**
     * Converts this varying definition to GLSL code for fragment shader (in).
     *
     * @return the GLSL varying declaration for fragment shader
     */
    public String toGLSLFragment() {
        return String.format("in %s %s;", typeToGLSL(type), name);
    }

    private String typeToGLSL(ShaderDataType type) {
        return switch (type) {
            case Float -> "float";
            case Float2 -> "vec2";
            case Float3 -> "vec3";
            case Float4 -> "vec4";
            case Mat2 -> "mat2";
            case Mat3 -> "mat3";
            case Mat4 -> "mat4";
            case Int -> "int";
            case Int2 -> "ivec2";
            case Int3 -> "ivec3";
            case Int4 -> "ivec4";
            case Bool -> "bool";
        };
    }
}



