package me.siebe.flux.opengl.shader;

import me.siebe.flux.util.exceptions.ShaderException;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public record ShaderUniform(
        String name,
        int location,
        int glType,
        int size
) {
    private static void checkLocation(int location) {
        if (location < 0) throw new ShaderException("Uniform location is invalid (inactive or optimized out).");
    }

    private void validateType(int expectedGLType) {
        if (glType != expectedGLType) {
            throw new IllegalArgumentException(
                    "Type mismatch when uploading to uniform '" + name +
                            "'. Shader type=" + glType + ", attempted upload type=" + expectedGLType
            );
        }
    }


    // =================================================================================================================
    // Uniform upload methods
    // =================================================================================================================
    //region
    // --- Matrix uploads ---
    public void upload(Matrix4f mat4) {
        checkLocation(location);
        validateType(GL_FLOAT_MAT4);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        mat4.get(buffer);
        glUniformMatrix4fv(location, false, buffer);
    }

    public void upload(Matrix3f mat3) {
        checkLocation(location);
        validateType(GL_FLOAT_MAT3);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
        mat3.get(buffer);
        glUniformMatrix3fv(location, false, buffer);
    }

    // --- Vector uploads ---
    public void upload(Vector4f vec) {
        checkLocation(location);
        validateType(GL_FLOAT_VEC4);
        glUniform4f(location, vec.x, vec.y, vec.z, vec.w);
    }

    public void upload(Vector3f vec) {
        checkLocation(location);
        validateType(GL_FLOAT_VEC3);
        glUniform3f(location, vec.x, vec.y, vec.z);
    }

    public void upload(Vector2f vec) {
        checkLocation(location);
        validateType(GL_FLOAT_VEC2);
        glUniform2f(location, vec.x, vec.y);
    }

    // --- Scalars ---
    public void upload(float val) {
        checkLocation(location);
        validateType(GL_FLOAT);
        glUniform1f(location, val);
    }

    public void upload(int val) {
        checkLocation(location);
        validateType(GL_INT);
        glUniform1i(location, val);
    }

    public void upload(int[] array) {
        checkLocation(location);
        validateType(GL_INT);
        glUniform1iv(location, array);
    }

    // --- Texture slots ---
    public void uploadTexture(int slot) {
        checkLocation(location);
        validateType(GL_SAMPLER_2D); // or GL_SAMPLER_CUBE etc. depending on shader
        glUniform1i(location, slot);
    }
    //endregion
}