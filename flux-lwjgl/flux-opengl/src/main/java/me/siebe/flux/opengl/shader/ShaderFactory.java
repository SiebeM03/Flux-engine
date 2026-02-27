package me.siebe.flux.opengl.shader;

import me.siebe.flux.util.exceptions.ShaderException;
import me.siebe.flux.util.memory.NativeTracker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

/**
 * Factory class for creating and managing shader programs.
 * This class is responsible for creating and compiling shader programs, linking them together, and deleting them.
 * It also provides methods for loading shader sources from the classpath or filesystem.
 */
class ShaderFactory {
    private ShaderFactory() {}

    static int createShader(String basePath, Path resourceRoot) {
        String vertexSource = getShaderSource(basePath + ".vert", resourceRoot);
        String fragmentSource = getShaderSource(basePath + ".frag", resourceRoot);

        int vertexShader = compile(vertexSource, GL_VERTEX_SHADER);
        int fragmentShader = compile(fragmentSource, GL_FRAGMENT_SHADER);

        return link(vertexShader, fragmentShader);
    }

    /**
     * Loads the shader source from the given resource path.
     * If the resource root is null, the shader source is loaded from the classpath.
     * Otherwise, the shader source is loaded from the filesystem at the given resource root.
     *
     * @param resourcePath the path to the shader file, relative to the resource root
     * @param resourceRoot the root path to the resources
     * @return the shader source
     * @throws ShaderException if the shader source cannot be read
     */
    private static String getShaderSource(String resourcePath, Path resourceRoot) {
        if (resourceRoot == null) {
            try (InputStream inputStream = ShaderProgram.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    throw new ShaderException("Shader file not found: " + resourcePath);
                }
                byte[] bytes = inputStream.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new ShaderException("Failed to read shader file: " + resourcePath, e);
            }
        }
        Path path = resourceRoot.resolve(resourcePath);
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ShaderException("Failed to read shader file: " + path, e);
        }
    }

    /**
     * Compiles the given shader source and returns the id of the compiled shader.
     * If the shader cannot be compiled, the shader is deleted and an exception is thrown.
     *
     * @param source the shader source
     * @param type   the shader type
     * @return the id of the compiled shader
     * @throws ShaderException if the shader cannot be compiled, or if the shader cannot be created
     */
    private static int compile(String source, int type) {
        int shader = createShader(source, type);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(shader);
            deleteShader(shader);
            throw new ShaderException("Failed to compile " + getShaderType(type) + " shader:\n" + infoLog);
        }

        return shader;
    }

    /**
     * Links the given vertex and fragment shaders and returns the id of the linked shader program.
     * If the shader program cannot be linked, the shader program is deleted and an exception is thrown.
     *
     * @param vertexShader   the id of the vertex shader
     * @param fragmentShader the id of the fragment shader
     * @return the id of the linked shader program
     * @throws ShaderException if the shader program cannot be linked, or if the shader program cannot be created
     */
    private static int link(int vertexShader, int fragmentShader) {
        int program = glCreateProgram();
        // Allocation and release for the final program are managed by GLResource
        if (program == 0) {
            throw ShaderException.failedToCreateProgram();
        }

        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(program);
            glDeleteProgram(program);

            deleteShader(vertexShader);
            deleteShader(fragmentShader);
            throw new ShaderException("Failed to link shader program:\n" + infoLog);
        }

        // Clean up shaders after linking
        glDetachShader(program, vertexShader);
        deleteShader(vertexShader);

        glDetachShader(program, fragmentShader);
        deleteShader(fragmentShader);

        return program;
    }

    /**
     * Creates and compiles a new shader object and returns the id of the created shader.
     * If the shader cannot be created, an exception is thrown.
     *
     * @param source the shader source
     * @param type   the shader type
     * @return the id of the created shader
     * @throws ShaderException if the shader cannot be created
     */
    private static int createShader(String source, int type) {
        int shader = glCreateShader(type);
        alloc("Shader");
        if (shader == 0) {
            release("Shader");
            throw ShaderException.failedToCreateProgram();
        }
        glShaderSource(shader, source);
        glCompileShader(shader);
        return shader;
    }

    /**
     * Deletes the given shader and releases the resources used by the shader.
     *
     * @param shader the id of the shader to delete
     */
    private static void deleteShader(int shader) {
        glDeleteShader(shader);
        release("Shader");
    }

    /**
     * Returns the string representation of the given shader type.
     *
     * @param type the shader type
     * @return the string representation of the shader type
     */
    private static String getShaderType(int type) {
        return switch (type) {
            case GL_VERTEX_SHADER -> "vertex";
            case GL_FRAGMENT_SHADER -> "fragment";
            default -> "unknown";
        };
    }


    private static void alloc(String tag) {
        NativeTracker.alloc(tag);
    }

    private static void release(String tag) {
        NativeTracker.free(tag);
    }

}
