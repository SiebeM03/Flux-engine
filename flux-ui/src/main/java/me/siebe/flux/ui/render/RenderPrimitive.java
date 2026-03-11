package me.siebe.flux.ui.render;

import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public enum RenderPrimitive {
    QUAD(4, 6, GL_TRIANGLES, (elements, index) -> {
        int vertexStartIndex = 6 * index;
        int offset = 4 * index;

        //  i3 ---------- i0
        //  |              |
        //  i2 ---------- i1
        elements[vertexStartIndex++] = offset + 0;
        elements[vertexStartIndex++] = offset + 2;
        elements[vertexStartIndex++] = offset + 1;

        elements[vertexStartIndex++] = offset + 0;
        elements[vertexStartIndex++] = offset + 3;
        elements[vertexStartIndex++] = offset + 2;
    }),
    ;

    /**
     * Number of vertices in the primitive
     */
    public final int vertexCount;
    /**
     * Number of elements in the primitive
     */
    public final int elementCount;
    /**
     * Primitive ID that opengl expects
     */
    public final int openglPrimitive;
    /**
     * Puts index data in the provided int buffer
     */
    public final BiConsumer<int[], Integer> elementCreation;

    RenderPrimitive(int vertexCount, int elementCount, int openglPrimitive, BiConsumer<int[], Integer> elementCreation) {
        this.vertexCount = vertexCount;
        this.elementCount = elementCount;
        this.openglPrimitive = openglPrimitive;
        this.elementCreation = elementCreation;
    }
}
