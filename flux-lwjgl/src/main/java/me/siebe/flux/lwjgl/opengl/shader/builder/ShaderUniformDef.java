package me.siebe.flux.lwjgl.opengl.shader.builder;

import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a shader uniform definition for programmatic shader creation.
 * <p>
 * Uniforms are values that are constant across all vertices/fragments in a draw call.
 * They can be set from the application code.
 */
public class ShaderUniformDef {
    private final String name;
    private final UniformType type;
    private final boolean required;

    /**
     * Creates a new shader uniform definition.
     *
     * @param name the uniform name (typically prefixed with 'u_')
     * @param type the uniform type
     * @param required whether this uniform is required (for validation)
     */
    public ShaderUniformDef(@NotNull String name, @NotNull UniformType type, boolean required) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Uniform name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Uniform type cannot be null");
        }
        this.name = name;
        this.type = type;
        this.required = required;
    }

    /**
     * Creates a new required shader uniform definition.
     *
     * @param name the uniform name
     * @param type the uniform type
     */
    public ShaderUniformDef(@NotNull String name, @NotNull UniformType type) {
        this(name, type, true);
    }

    /**
     * Gets the uniform name.
     *
     * @return the uniform name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets the uniform type.
     *
     * @return the uniform type
     */
    @NotNull
    public UniformType getType() {
        return type;
    }

    /**
     * Checks if this uniform is required.
     *
     * @return true if required, false otherwise
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Converts this uniform definition to GLSL code.
     *
     * @return the GLSL uniform declaration
     */
    public String toGLSL() {
        return String.format("uniform %s %s;", type.toGLSL(), name);
    }

    /**
     * Represents the type of a uniform variable.
     */
    public enum UniformType {
        Float("float"),
        Float2("vec2"),
        Float3("vec3"),
        Float4("vec4"),
        Int("int"),
        Int2("ivec2"),
        Int3("ivec3"),
        Int4("ivec4"),
        Bool("bool"),
        Mat2("mat2"),
        Mat3("mat3"),
        Mat4("mat4"),
        Sampler2D("sampler2D"),
        SamplerCube("samplerCube");

        private final String glslType;

        UniformType(String glslType) {
            this.glslType = glslType;
        }

        /**
         * Gets the GLSL type string for this uniform type.
         *
         * @return the GLSL type string
         */
        public String toGLSL() {
            return glslType;
        }

        /**
         * Converts a ShaderDataType to a UniformType where applicable.
         *
         * @param dataType the shader data type
         * @return the corresponding uniform type
         * @throws IllegalArgumentException if the data type cannot be converted
         */
        public static UniformType fromShaderDataType(ShaderDataType dataType) {
            return switch (dataType) {
                case Float -> Float;
                case Float2 -> Float2;
                case Float3 -> Float3;
                case Float4 -> Float4;
                case Int -> Int;
                case Int2 -> Int2;
                case Int3 -> Int3;
                case Int4 -> Int4;
                case Bool -> Bool;
                case Mat2 -> Mat2;
                case Mat3 -> Mat3;
                case Mat4 -> Mat4;
                default -> throw new IllegalArgumentException("Cannot convert ShaderDataType " + dataType + " to UniformType");
            };
        }
    }
}



