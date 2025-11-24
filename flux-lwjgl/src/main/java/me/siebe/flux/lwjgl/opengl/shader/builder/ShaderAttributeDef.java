package me.siebe.flux.lwjgl.opengl.shader.builder;

import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a shader attribute definition for programmatic shader creation.
 * <p>
 * Attributes are inputs to the vertex shader that come from vertex buffers.
 * Each attribute has a name, data type, and optional location.
 */
public class ShaderAttributeDef {
    private final String name;
    private final ShaderDataType type;
    private final Integer location;

    /**
     * Creates a new shader attribute definition.
     *
     * @param name the attribute name (must start with a letter, typically prefixed with 'a_')
     * @param type the data type of the attribute
     * @param location the location index (optional, null for automatic assignment)
     */
    public ShaderAttributeDef(@NotNull String name, @NotNull ShaderDataType type, Integer location) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Attribute type cannot be null");
        }
        this.name = name;
        this.type = type;
        this.location = location;
    }

    /**
     * Creates a new shader attribute definition with automatic location assignment.
     *
     * @param name the attribute name
     * @param type the data type of the attribute
     */
    public ShaderAttributeDef(@NotNull String name, @NotNull ShaderDataType type) {
        this(name, type, null);
    }

    /**
     * Gets the attribute name.
     *
     * @return the attribute name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets the attribute data type.
     *
     * @return the data type
     */
    @NotNull
    public ShaderDataType getType() {
        return type;
    }

    /**
     * Gets the attribute location, or null if not specified.
     *
     * @return the location index, or null
     */
    public Integer getLocation() {
        return location;
    }

    /**
     * Converts this attribute definition to GLSL code.
     *
     * @return the GLSL attribute declaration
     */
    public String toGLSL() {
        String typeStr = typeToGLSL(type);
        if (location != null) {
            return String.format("layout (location = %d) in %s %s;", location, typeStr, name);
        } else {
            return String.format("in %s %s;", typeStr, name);
        }
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



