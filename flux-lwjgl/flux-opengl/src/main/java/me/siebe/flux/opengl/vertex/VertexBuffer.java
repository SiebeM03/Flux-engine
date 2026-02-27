package me.siebe.flux.opengl.vertex;

import me.siebe.flux.opengl.GLResource;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;

public class VertexBuffer extends GLResource {
    private static final Logger logger = LoggerFactory.getLogger(VertexBuffer.class, LoggingCategories.RENDERER);

    private BufferLayout bufferLayout;
    private int size;

    public VertexBuffer(int size) {
        super(glGenBuffers());
        bind();
        glBufferData(GL_ARRAY_BUFFER, size, GL_DYNAMIC_DRAW);
        this.size = size;
    }

    public VertexBuffer(float[] vertices) {
        super(glGenBuffers());
        bind();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();

        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        this.size = vertices.length;
    }

    @Override
    protected int getBindTarget() {
        return GL_ARRAY_BUFFER;
    }

    public BufferLayout getLayout() {
        return bufferLayout;
    }

    public void setLayout(BufferLayout bufferLayout) {
        this.bufferLayout = bufferLayout;
    }

    public void setData(float[] data) {
        bind();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data).flip();

        glBufferSubData(GL_ARRAY_BUFFER, 0, data);
        this.size = data.length;
    }

    public int getSize() {
        return size;
    }
}
