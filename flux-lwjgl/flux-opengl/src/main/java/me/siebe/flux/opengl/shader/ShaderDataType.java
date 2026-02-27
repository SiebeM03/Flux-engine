package me.siebe.flux.opengl.shader;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.*;

public enum ShaderDataType {
    Float(java.lang.Float.BYTES, 1, GL_FLOAT),
    Float2(java.lang.Float.BYTES, 2, GL_FLOAT_VEC2),
    Float3(java.lang.Float.BYTES, 3, GL_FLOAT_VEC3),
    Float4(java.lang.Float.BYTES, 4, GL_FLOAT_VEC4),
    Mat2(java.lang.Float.BYTES, 2 * 2, GL_FLOAT_MAT2),
    Mat3(java.lang.Float.BYTES, 3 * 3, GL_FLOAT_MAT3),
    Mat4(java.lang.Float.BYTES, 4 * 4, GL_FLOAT_MAT4),
    Int(java.lang.Integer.BYTES, 1, GL_INT),
    Int2(java.lang.Integer.BYTES, 2, GL_INT_VEC2),
    Int3(java.lang.Integer.BYTES, 3, GL_INT_VEC3),
    Int4(java.lang.Integer.BYTES, 4, GL_INT_VEC4),
    Bool(1, 1, GL_BOOL),
    ;

    /**
     * The size in bytes of a single component of this shader data type.
     * For example, Float types have a component size of 4 bytes, while Bool has 1 byte.
     */
    private final int componentSize;

    /**
     * The number of components in this shader data type.
     * For example, Float2 has 2 components, Float3 has 3, Mat4 has 16 (4x4) components.
     */
    private final int componentCount;

    /**
     * The OpenGL type constant for this shader data type.
     */
    private final int openGLType;

    ShaderDataType(int componentSize, int componentCount, int openGLType) {
        this.componentSize = componentSize;
        this.componentCount = componentCount;
        this.openGLType = openGLType;
    }

    /**
     * Gets the size in bytes of a single component of this shader data type.
     * For example, Float types have a component size of 4 bytes, while Bool has 1 byte.
     *
     * @return the size in bytes of a single component
     */
    public int getComponentSize() {
        return componentSize;
    }

    /**
     * Gets the number of components in this shader data type.
     * For example, Float2 has 2 components, Float3 has 3, Mat4 has 16 (4x4) components.
     *
     * @return the number of components
     */
    public int getComponentCount() {
        return this.componentCount;
    }

    /**
     * Calculates the total size in bytes for this shader data type.
     * This is the product of component size and component count.
     *
     * @return the total byte size
     */
    public int getTotalByteSize() {
        return componentSize * componentCount;
    }

    /**
     * Gets the OpenGL type constant for this shader data type.
     *
     * @return the OpenGL type constant (e.g., GL_FLOAT, GL_FLOAT_VEC2, etc.)
     */
    public int getOpenGLType() {
        return openGLType;
    }

    public static ShaderDataType fromOpenGLType(int openGLType) {
        for (ShaderDataType dataType : ShaderDataType.values()) {
            if (dataType.getOpenGLType() == openGLType) {
                return dataType;
            }
        }
        throw new IllegalArgumentException("No ShaderDataType found for OpenGL type: " + openGLType);
    }
}
