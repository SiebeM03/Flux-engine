package me.siebe.flux.lwjgl.opengl.shader.builder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating vertex shaders programmatically.
 * <p>
 * This class allows you to define vertex shaders using Java code, which enables
 * compile-time checks for required attributes and better type safety.
 */
public class VertexShaderBuilder {
    private String version = "#version 330 core";
    private final List<ShaderAttributeDef> attributes = new ArrayList<>();
    private final List<ShaderUniformDef> uniforms = new ArrayList<>();
    private final List<ShaderVaryingDef> varyings = new ArrayList<>();
    private String mainBody = "";

    /**
     * Sets the GLSL version string.
     * Default is "#version 330 core".
     *
     * @param version the version string
     * @return this builder for method chaining
     */
    @NotNull
    public VertexShaderBuilder version(@NotNull String version) {
        this.version = version;
        return this;
    }

    /**
     * Adds an attribute to the vertex shader.
     *
     * @param attribute the attribute definition
     * @return this builder for method chaining
     */
    @NotNull
    public VertexShaderBuilder attribute(@NotNull ShaderAttributeDef attribute) {
        attributes.add(attribute);
        return this;
    }

    /**
     * Adds a uniform to the vertex shader.
     *
     * @param uniform the uniform definition
     * @return this builder for method chaining
     */
    @NotNull
    public VertexShaderBuilder uniform(@NotNull ShaderUniformDef uniform) {
        uniforms.add(uniform);
        return this;
    }

    /**
     * Adds a varying (output) to the vertex shader.
     *
     * @param varying the varying definition
     * @return this builder for method chaining
     */
    @NotNull
    public VertexShaderBuilder varying(@NotNull ShaderVaryingDef varying) {
        varyings.add(varying);
        return this;
    }

    /**
     * Sets the main function body.
     * The body should contain the code inside the main() function, without the function signature.
     *
     * @param body the main function body
     * @return this builder for method chaining
     */
    @NotNull
    public VertexShaderBuilder mainBody(@NotNull String body) {
        this.mainBody = body;
        return this;
    }

    /**
     * Builds the GLSL vertex shader source code.
     *
     * @return the complete GLSL vertex shader source
     */
    @NotNull
    public String build() {
        StringBuilder sb = new StringBuilder();
        
        // Version
        sb.append(version).append("\n\n");
        
        // Attributes
        for (ShaderAttributeDef attr : attributes) {
            sb.append(attr.toGLSL()).append("\n");
        }
        if (!attributes.isEmpty()) {
            sb.append("\n");
        }
        
        // Uniforms
        for (ShaderUniformDef uniform : uniforms) {
            sb.append(uniform.toGLSL()).append("\n");
        }
        if (!uniforms.isEmpty()) {
            sb.append("\n");
        }
        
        // Varyings (out)
        for (ShaderVaryingDef varying : varyings) {
            sb.append(varying.toGLSLVertex()).append("\n");
        }
        if (!varyings.isEmpty()) {
            sb.append("\n");
        }
        
        // Main function
        sb.append("void main()\n");
        sb.append("{\n");
        if (!mainBody.isEmpty()) {
            // Indent the body
            String[] lines = mainBody.split("\n");
            for (String line : lines) {
                sb.append("    ").append(line).append("\n");
            }
        }
        sb.append("}\n");
        
        return sb.toString();
    }

    /**
     * Gets all attribute definitions.
     *
     * @return a list of attribute definitions
     */
    @NotNull
    public List<ShaderAttributeDef> getAttributes() {
        return new ArrayList<>(attributes);
    }

    /**
     * Gets all uniform definitions.
     *
     * @return a list of uniform definitions
     */
    @NotNull
    public List<ShaderUniformDef> getUniforms() {
        return new ArrayList<>(uniforms);
    }

    /**
     * Gets all varying definitions.
     *
     * @return a list of varying definitions
     */
    @NotNull
    public List<ShaderVaryingDef> getVaryings() {
        return new ArrayList<>(varyings);
    }
}



