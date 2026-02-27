package me.siebe.flux.opengl.shader;

import me.siebe.flux.opengl.GLResource;
import me.siebe.flux.util.exceptions.ShaderException;
import me.siebe.flux.util.exceptions.Validator;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram extends GLResource {
    private static final Logger logger = LoggerFactory.getLogger(ShaderProgram.class, LoggingCategories.RENDERER);
    private static ShaderProgram ACTIVE_SHADER;

    private final String filename;

    private final Map<String, ShaderAttribute> attributes;
    private final Map<String, ShaderUniform> uniforms;

    /**
     * Creates a new shader program from vertex and fragment shader files.
     * <p>
     * When {@code resourceRoot} is null, shaders are loaded from the classpath
     * (e.g. basePath "shaders/basic" → "shaders/basic.vert" and "shaders/basic.frag").
     * When {@code resourceRoot} is non-null, shaders are read from the filesystem
     * at {@code resourceRoot.resolve(basePath + ".vert")} and {@code ".frag"} (for hot-reload).
     *
     * @param basePath     the base path to the shader files (without extension)
     * @param resourceRoot optional filesystem root for shader files; null to use classpath
     * @throws ShaderException if the shaders cannot be loaded, compiled, or linked
     */
    ShaderProgram(String basePath, Path resourceRoot) {
        super(ShaderFactory.createShader(basePath, resourceRoot));
        this.filename = basePath;
        this.attributes = new HashMap<>();
        this.uniforms = new HashMap<>();

        reflectAttributes();
        reflectUniforms();

        logger.debug("Created shader program '{}' with {} attributes and {} uniforms",
                filename, attributes.size(), uniforms.size());
    }

    @Override
    protected int getBindTarget() {
        return GL_PROGRAM;
    }

    @Override
    public void bind() {
        ShaderProgram.ACTIVE_SHADER = this;
        super.bind();
    }

    @Override
    public void unbind() {
        ShaderProgram.ACTIVE_SHADER = null;
        super.unbind();
    }

    private void reflectAttributes() {
        int count = glGetProgrami(getGlId(), GL_ACTIVE_ATTRIBUTES);

        IntBuffer sizeBuf = BufferUtils.createIntBuffer(1);
        IntBuffer typeBuf = BufferUtils.createIntBuffer(1);

        for (int i = 0; i < count; i++) {
            sizeBuf.clear();
            typeBuf.clear();

            String name = glGetActiveAttrib(getGlId(), i, sizeBuf, typeBuf);
            int location = glGetAttribLocation(getGlId(), name);
            int size = sizeBuf.get(0);
            int type = typeBuf.get(0);

            attributes.put(name, new ShaderAttribute(name, location, ShaderDataType.fromOpenGLType(type), size));
        }
    }

    private void reflectUniforms() {
        int count = glGetProgrami(getGlId(), GL_ACTIVE_UNIFORMS);

        IntBuffer sizeBuf = BufferUtils.createIntBuffer(1);
        IntBuffer typeBuf = BufferUtils.createIntBuffer(1);

        for (int i = 0; i < count; i++) {
            String name = glGetActiveUniform(getGlId(), i, sizeBuf, typeBuf);
            int location = glGetUniformLocation(getGlId(), name);
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


    public void upload(String name, Object value) {
        Validator.notNull(value);
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
