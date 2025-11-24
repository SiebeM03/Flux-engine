package me.siebe.flux.lwjgl.opengl.shader.builder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating fragment shaders programmatically.
 * <p>
 * This class allows you to define fragment shaders using Java code, which enables
 * compile-time checks for required uniforms and better type safety.
 */
public class FragmentShaderBuilder {
    private String version = "#version 330 core";
    private final List<ShaderUniformDef> uniforms = new ArrayList<>();
    private final List<ShaderVaryingDef> varyings = new ArrayList<>();
    private String outputName = "FragColor";
    private String mainBody = "";

    /**
     * Sets the GLSL version string.
     * Default is "#version 330 core".
     *
     * @param version the version string
     * @return this builder for method chaining
     */
    @NotNull
    public FragmentShaderBuilder version(@NotNull String version) {
        this.version = version;
        return this;
    }

    /**
     * Adds a uniform to the fragment shader.
     *
     * @param uniform the uniform definition
     * @return this builder for method chaining
     */
    @NotNull
    public FragmentShaderBuilder uniform(@NotNull ShaderUniformDef uniform) {
        uniforms.add(uniform);
        return this;
    }

    /**
     * Adds a varying (input) to the fragment shader.
     *
     * @param varying the varying definition
     * @return this builder for method chaining
     */
    @NotNull
    public FragmentShaderBuilder varying(@NotNull ShaderVaryingDef varying) {
        varyings.add(varying);
        return this;
    }

    /**
     * Sets the output variable name.
     * Default is "FragColor".
     *
     * @param outputName the output variable name
     * @return this builder for method chaining
     */
    @NotNull
    public FragmentShaderBuilder outputName(@NotNull String outputName) {
        this.outputName = outputName;
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
    public FragmentShaderBuilder mainBody(@NotNull String body) {
        this.mainBody = body;
        return this;
    }

    /**
     * Builds the GLSL fragment shader source code.
     *
     * @return the complete GLSL fragment shader source
     */
    @NotNull
    public String build() {
        StringBuilder sb = new StringBuilder();
        
        // Version
        sb.append(version).append("\n\n");
        
        // Varyings (in)
        for (ShaderVaryingDef varying : varyings) {
            sb.append(varying.toGLSLFragment()).append("\n");
        }
        if (!varyings.isEmpty()) {
            sb.append("\n");
        }
        
        // Uniforms
        for (ShaderUniformDef uniform : uniforms) {
            sb.append(uniform.toGLSL()).append("\n");
        }
        if (!uniforms.isEmpty()) {
            sb.append("\n");
        }
        
        // Output
        sb.append("out vec4 ").append(outputName).append(";\n\n");
        
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



