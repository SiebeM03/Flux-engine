package me.siebe.flux.lwjgl.opengl.shader;

import me.siebe.flux.util.exceptions.ShaderException;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private static final Logger logger = LoggerFactory.getLogger(ShaderProgram.class, LoggingCategories.RENDERER);
    private static ShaderProgram ACTIVE_SHADER;

    private final int programId;
    private final String filename;

    private final Map<String, ShaderAttribute> attributes;
    private final Map<String, ShaderUniform> uniforms;

    /**
     * Creates a new shader program from vertex and fragment shader files.
     * <p>
     * The shader files should be located in the resources directory.
     * For example, if basePath is "shaders/basic", it will look for
     * "shaders/basic.vert" and "shaders/basic.frag".
     *
     * @param basePath the base path to the shader files (without extension)
     * @throws ShaderException if the shaders cannot be loaded, compiled, or linked
     */
    ShaderProgram(String basePath) {
        this.filename = basePath;
        this.attributes = new HashMap<>();
        this.uniforms = new HashMap<>();

        String vertexSource = getFileContent(basePath + ".vert");
        String fragmentSource = getFileContent(basePath + ".frag");

        int vertexShader = compile(vertexSource, GL_VERTEX_SHADER);
        int fragmentShader = compile(fragmentSource, GL_FRAGMENT_SHADER);

        this.programId = link(vertexShader, fragmentShader);

        reflectAttributes();
        reflectUniforms();

        logger.debug("Created shader program '{}' with {} attributes and {} uniforms",
                filename, attributes.size(), uniforms.size());
    }


    public void bind() {
        ShaderProgram.ACTIVE_SHADER = this;
        glUseProgram(programId);
    }

    public void unbind() {
        ShaderProgram.ACTIVE_SHADER = null;
        glUseProgram(0);
    }

    public void destroy() {
        unbind();
        glDeleteProgram(programId);
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

    private String getFileContent(String path) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new ShaderException("Shader file not found: " + path);
            }
            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ShaderException("Failed to read shader file: " + path, e);
        }
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

    /**
     * Gets the uniform with the specified name.
     *
     * @param name the uniform name
     * @return the uniform, or null if not found
     */
    public ShaderUniform getUniform(String name) {
        return uniforms.get(name);
    }


    public void upload(String name, @NotNull Object value) {
        ShaderUniform u = getUniform(name);
        if (u == null) {
            logger.warn("Uniform {} not found or unused in {}", name, filename);
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

    public void uploadTexture(String name, int slot) {
        ShaderUniform u = getUniform(name);
        if (u == null) {
            logger.warn("Uniform {} not found or unused in {}", name, filename);
            return;
        }
        bind();
        u.uploadTexture(slot);
    }

    public static ShaderProgram getActiveShader() {
        return ACTIVE_SHADER;
    }
}
