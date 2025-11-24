package me.siebe.flux.lwjgl.opengl.shader.builder;

import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class providing convenient factory methods for creating shader builders.
 * <p>
 * This class provides static methods to quickly create common shader components
 * and builders, making it easier to use the shader builder system.
 */
public final class ShaderBuilders {
    private ShaderBuilders() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a new vertex shader builder.
     *
     * @return a new vertex shader builder
     */
    @NotNull
    public static VertexShaderBuilder vertex() {
        return new VertexShaderBuilder();
    }

    /**
     * Creates a new fragment shader builder.
     *
     * @return a new fragment shader builder
     */
    @NotNull
    public static FragmentShaderBuilder fragment() {
        return new FragmentShaderBuilder();
    }

    /**
     * Creates a new shader program builder.
     *
     * @return a new shader program builder
     */
    @NotNull
    public static ShaderProgramBuilder program() {
        return new ShaderProgramBuilder();
    }

    /**
     * Creates a new attribute definition with automatic location assignment.
     *
     * @param name the attribute name
     * @param type the attribute data type
     * @return a new attribute definition
     */
    @NotNull
    public static ShaderAttributeDef attribute(@NotNull String name, @NotNull ShaderDataType type) {
        return new ShaderAttributeDef(name, type);
    }

    /**
     * Creates a new attribute definition with a specific location.
     *
     * @param name the attribute name
     * @param type the attribute data type
     * @param location the attribute location
     * @return a new attribute definition
     */
    @NotNull
    public static ShaderAttributeDef attribute(@NotNull String name, @NotNull ShaderDataType type, int location) {
        return new ShaderAttributeDef(name, type, location);
    }

    /**
     * Creates a new uniform definition (required by default).
     *
     * @param name the uniform name
     * @param type the uniform type
     * @return a new uniform definition
     */
    @NotNull
    public static ShaderUniformDef uniform(@NotNull String name, @NotNull ShaderUniformDef.UniformType type) {
        return new ShaderUniformDef(name, type);
    }

    /**
     * Creates a new uniform definition with optional requirement flag.
     *
     * @param name the uniform name
     * @param type the uniform type
     * @param required whether the uniform is required
     * @return a new uniform definition
     */
    @NotNull
    public static ShaderUniformDef uniform(@NotNull String name, @NotNull ShaderUniformDef.UniformType type, boolean required) {
        return new ShaderUniformDef(name, type, required);
    }

    /**
     * Creates a new varying definition.
     *
     * @param name the varying name
     * @param type the varying data type
     * @return a new varying definition
     */
    @NotNull
    public static ShaderVaryingDef varying(@NotNull String name, @NotNull ShaderDataType type) {
        return new ShaderVaryingDef(name, type);
    }
}



