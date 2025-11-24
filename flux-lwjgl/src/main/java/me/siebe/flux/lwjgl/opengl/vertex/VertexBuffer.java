package me.siebe.flux.lwjgl.opengl.vertex;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;

public class VertexBuffer extends GLBuffer {
    private static final Logger logger = LoggerFactory.getLogger(VertexBuffer.class, LoggingCategories.RENDERER);

    private BufferLayout bufferLayout;

    public VertexBuffer(int size) {
        super(glGenBuffers());
        bind();
        glBufferData(GL_ARRAY_BUFFER, size, GL_DYNAMIC_DRAW);
    }

    public VertexBuffer(float[] vertices) {
        super(glGenBuffers());
        bind();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();

        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
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
    }

}
