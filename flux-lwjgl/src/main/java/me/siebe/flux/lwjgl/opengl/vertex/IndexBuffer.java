package me.siebe.flux.lwjgl.opengl.vertex;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public class IndexBuffer extends GLBuffer {
    private static final Logger logger = LoggerFactory.getLogger(IndexBuffer.class, LoggingCategories.RENDERER);

    private int count;

    /**
     * Creates a new index buffer with the specified indices.
     *
     * @param indices the index data
     */
    public IndexBuffer(int[] indices) {
        super(glCreateBuffers());
        this.count = indices.length;

        // GL_ELEMENT_ARRAY_BUFFER is not valid without an actively bound VAO
        // Binding with GL_ARRAY_BUFFER allows the data to be loaded regardless of VAO state.
        bind(GL_ARRAY_BUFFER);
        glBufferData(GL_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    @Override
    protected int getBindTarget() {
        return GL_ELEMENT_ARRAY_BUFFER;
    }

    public int getCount() {
        return count;
    }
}
