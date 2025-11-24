package me.siebe.flux.lwjgl.opengl.shader.builder;

import me.siebe.flux.lwjgl.opengl.shader.ShaderAttribute;
import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import me.siebe.flux.lwjgl.opengl.shader.ShaderUniform;
import me.siebe.flux.util.exceptions.ShaderException;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

/**
 * Builder for creating shader programs from programmatically defined shaders.
 * <p>
 * This class allows you to build shader programs using the builder pattern,
 * compile them, and validate that required uniforms and attributes are present.
 */
public class ShaderProgramBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ShaderProgramBuilder.class, LoggingCategories.RENDERER);

    private VertexShaderBuilder vertexBuilder;
    private FragmentShaderBuilder fragmentBuilder;
    private String programName;

    /**
     * Creates a new shader program builder.
     */
    public ShaderProgramBuilder() {
    }

    /**
     * Sets the vertex shader builder.
     *
     * @param builder the vertex shader builder
     * @return this builder for method chaining
     */
    @NotNull
    public ShaderProgramBuilder vertex(@NotNull VertexShaderBuilder builder) {
        this.vertexBuilder = builder;
        return this;
    }

    /**
     * Sets the fragment shader builder.
     *
     * @param builder the fragment shader builder
     * @return this builder for method chaining
     */
    @NotNull
    public ShaderProgramBuilder fragment(@NotNull FragmentShaderBuilder builder) {
        this.fragmentBuilder = builder;
        return this;
    }

    /**
     * Sets the program name (for logging and debugging).
     *
     * @param name the program name
     * @return this builder for method chaining
     */
    @NotNull
    public ShaderProgramBuilder name(@NotNull String name) {
        this.programName = name;
        return this;
    }

    /**
     * Validates that all required uniforms and attributes are present.
     * This checks that varyings match between vertex and fragment shaders.
     *
     * @throws ShaderException if validation fails
     */
    public void validate() {
        if (vertexBuilder == null) {
            throw new ShaderException("Vertex shader builder is not set");
        }
        if (fragmentBuilder == null) {
            throw new ShaderException("Fragment shader builder is not set");
        }

        // Check that varyings match
        List<ShaderVaryingDef> vertexVaryings = vertexBuilder.getVaryings();
        List<ShaderVaryingDef> fragmentVaryings = fragmentBuilder.getVaryings();

        Set<String> vertexVaryingNames = new HashSet<>();
        for (ShaderVaryingDef varying : vertexVaryings) {
            vertexVaryingNames.add(varying.getName());
        }

        Set<String> fragmentVaryingNames = new HashSet<>();
        for (ShaderVaryingDef varying : fragmentVaryings) {
            fragmentVaryingNames.add(varying.getName());
        }

        // Check for mismatches
        for (ShaderVaryingDef vertexVarying : vertexVaryings) {
            if (!fragmentVaryingNames.contains(vertexVarying.getName())) {
                logger.warn("Vertex shader outputs varying '{}' but fragment shader does not declare it", 
                        vertexVarying.getName());
            }
        }

        for (ShaderVaryingDef fragmentVarying : fragmentVaryings) {
            if (!vertexVaryingNames.contains(fragmentVarying.getName())) {
                throw new ShaderException("Fragment shader declares varying '" + fragmentVarying.getName() + 
                        "' but vertex shader does not output it");
            }
        }

        // Check required uniforms
        List<ShaderUniformDef> allUniforms = new ArrayList<>(vertexBuilder.getUniforms());
        allUniforms.addAll(fragmentBuilder.getUniforms());

        for (ShaderUniformDef uniform : allUniforms) {
            if (uniform.isRequired()) {
                logger.debug("Required uniform '{}' is declared", uniform.getName());
            }
        }
    }

    /**
     * Builds and compiles the shader program.
     * This method validates the shaders, compiles them, and creates a BuiltShaderProgram.
     *
     * @return the compiled shader program
     * @throws ShaderException if compilation or linking fails
     */
    @NotNull
    public BuiltShaderProgram build() {
        validate();

        String vertexSource = vertexBuilder.build();
        String fragmentSource = fragmentBuilder.build();

        // Log the generated shaders if in debug mode
        if (logger.isDebugEnabled()) {
            logger.debug("Generated vertex shader for '{}':\n{}", programName != null ? programName : "unnamed", vertexSource);
            logger.debug("Generated fragment shader for '{}':\n{}", programName != null ? programName : "unnamed", fragmentSource);
        }

        int vertexShader = compile(vertexSource, GL_VERTEX_SHADER);
        int fragmentShader = compile(fragmentSource, GL_FRAGMENT_SHADER);

        int programId = link(vertexShader, fragmentShader);

        // Create a built shader program
        return new BuiltShaderProgram(programId, programName != null ? programName : "built_shader");
    }

    /**
     * Saves the generated shader source code to files.
     * Useful for debugging or inspection.
     *
     * @param vertexPath the path to save the vertex shader
     * @param fragmentPath the path to save the fragment shader
     * @throws IOException if file writing fails
     */
    public void saveToFiles(@NotNull Path vertexPath, @NotNull Path fragmentPath) throws IOException {
        if (vertexBuilder == null || fragmentBuilder == null) {
            throw new IllegalStateException("Both vertex and fragment shaders must be set before saving");
        }

        String vertexSource = vertexBuilder.build();
        String fragmentSource = fragmentBuilder.build();

        Files.writeString(vertexPath, vertexSource, StandardCharsets.UTF_8);
        Files.writeString(fragmentPath, fragmentSource, StandardCharsets.UTF_8);

        logger.info("Saved shader sources to {} and {}", vertexPath, fragmentPath);
    }

    private int compile(String source, int type) {
        int shader = glCreateShader(type);
        if (shader == 0) {
            throw ShaderException.failedToCreateProgram();
        }

        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            String shaderType = type == GL_VERTEX_SHADER ? "vertex" : "fragment";
            throw new ShaderException("Failed to compile " + shaderType + " shader:\n" + infoLog);
        }

        return shader;
    }

    private int link(int vertexShader, int fragmentShader) {
        int program = glCreateProgram();
        if (program == 0) {
            throw ShaderException.failedToCreateProgram();
        }

        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(program);
            glDeleteProgram(program);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            throw new ShaderException("Failed to link shader program:\n" + infoLog);
        }

        // Clean up shaders after linking
        glDetachShader(program, vertexShader);
        glDetachShader(program, fragmentShader);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return program;
    }

    /**
     * A shader program created from programmatically defined shaders.
     * This class provides the same interface as ShaderProgram but is created from source code.
     */
    public static class BuiltShaderProgram {
        private final int programId;
        private final String name;
        private final Map<String, ShaderAttribute> attributes;
        private final Map<String, ShaderUniform> uniforms;

        private BuiltShaderProgram(int programId, String name) {
            this.programId = programId;
            this.name = name;
            this.attributes = new HashMap<>();
            this.uniforms = new HashMap<>();
            
            reflectAttributes();
            reflectUniforms();
            
            logger.debug("Created built shader program '{}' with {} attributes and {} uniforms",
                    name, attributes.size(), uniforms.size());
        }

        /**
         * Binds this shader program for use.
         */
        public void bind() {
            glUseProgram(programId);
        }

        /**
         * Unbinds this shader program.
         */
        public void unbind() {
            glUseProgram(0);
        }

        /**
         * Destroys this shader program and frees its resources.
         */
        public void destroy() {
            unbind();
            glDeleteProgram(programId);
        }

        /**
         * Gets the uniform with the specified name.
         *
         * @param name the uniform name
         * @return the uniform, or null if not found
         */
        public ShaderUniform getUniform(String name) {
            return uniforms.get(name);
        }

        /**
         * Uploads a value to a uniform.
         * This method provides the same interface as ShaderProgram.upload().
         *
         * @param name the uniform name
         * @param value the value to upload
         */
        public void upload(String name, @NotNull Object value) {
            ShaderUniform u = getUniform(name);
            if (u == null) {
                logger.error("Uniform {} not found or unused in {}", name, this.name);
                return;
            }
            bind();
            switch (value) {
                case Matrix4f mat4 -> u.upload(mat4);
                case Matrix3f mat3 -> u.upload(mat3);
                case Vector4f vec4 -> u.upload(vec4);
                case Vector3f vec3 -> u.upload(vec3);
                case Vector2f vec2 -> u.upload(vec2);
                case Float f -> u.upload(f);
                case Integer i -> u.upload(i);
                case int[] arr -> u.upload(arr);
                default -> throw new ShaderException("Tried to upload unsupported uniform type: " + value.getClass());
            }
        }

        /**
         * Uploads a texture slot to a uniform.
         *
         * @param name the uniform name
         * @param slot the texture slot
         */
        public void uploadTexture(String name, int slot) {
            ShaderUniform u = getUniform(name);
            if (u == null) {
                logger.error("Uniform {} not found or unused in {}", name, this.name);
                return;
            }
            bind();
            u.uploadTexture(slot);
        }

        private void reflectAttributes() {
            int count = glGetProgrami(programId, GL_ACTIVE_ATTRIBUTES);

            IntBuffer sizeBuf = BufferUtils.createIntBuffer(1);
            IntBuffer typeBuf = BufferUtils.createIntBuffer(1);

            for (int i = 0; i < count; i++) {
                sizeBuf.clear();
                typeBuf.clear();

                String name = glGetActiveAttrib(programId, i, sizeBuf, typeBuf);
                int location = glGetAttribLocation(programId, name);
                int size = sizeBuf.get(0);
                int type = typeBuf.get(0);

                attributes.put(name, new ShaderAttribute(name, location, ShaderDataType.fromOpenGLType(type), size));
            }
        }

        private void reflectUniforms() {
            int count = glGetProgrami(programId, GL_ACTIVE_UNIFORMS);

            IntBuffer sizeBuf = BufferUtils.createIntBuffer(1);
            IntBuffer typeBuf = BufferUtils.createIntBuffer(1);

            for (int i = 0; i < count; i++) {
                String name = glGetActiveUniform(programId, i, sizeBuf, typeBuf);
                int location = glGetUniformLocation(programId, name);
                int size = sizeBuf.get(0);
                int type = typeBuf.get(0);

                uniforms.put(name, new ShaderUniform(name, location, type, size));
            }
        }
    }
}

