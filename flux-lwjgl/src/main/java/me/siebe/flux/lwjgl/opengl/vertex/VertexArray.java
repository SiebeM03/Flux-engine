package me.siebe.flux.lwjgl.opengl.vertex;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL45.glCreateVertexArrays;

public class VertexArray extends GLBuffer {
    private static final Logger logger = LoggerFactory.getLogger(VertexArray.class, LoggingCategories.RENDERER);

    private int vertexBufferIndex = 0;

    private final List<VertexBuffer> vertexBuffers;
    private IndexBuffer indexBuffer;

    public VertexArray() {
        super(glCreateVertexArrays());

        this.vertexBuffers = new ArrayList<>();
    }

    @Override
    protected int getBindTarget() {
        return 0;
    }

    public List<VertexBuffer> getVertexBuffers() {
        return vertexBuffers;
    }

    public void addVertexBuffer(final VertexBuffer vertexBuffer) {
        vertexBuffer.bind();
        BufferLayout layout = vertexBuffer.getLayout();
        if (layout == null) {
            throw new IllegalStateException("Vertex buffer layout is null");
        }
        for (BufferElement element : layout.getElements()) {
            switch (element.type) {
                case Float, Float2, Float3, Float4 -> {
                    glEnableVertexAttribArray(vertexBufferIndex);
                    glVertexAttribPointer(
                            vertexBufferIndex,
                            element.type.getComponentCount(),
                            GL_FLOAT,
                            element.normalized,
                            layout.getStride(),
                            element.offset
                    );
                    logger.info("Vertex attrib array {} has been added", element.name);
                    vertexBufferIndex++;
                }
                case Int, Int2, Int3, Int4 -> {
                    glEnableVertexAttribArray(vertexBufferIndex);
                    glVertexAttribIPointer(
                            vertexBufferIndex,
                            element.type.getComponentCount(),
                            GL_INT,
                            layout.getStride(),
                            element.offset
                    );
                    vertexBufferIndex++;
                }
                case Bool -> {
                    glEnableVertexAttribArray(vertexBufferIndex);
                    glVertexAttribIPointer(
                            vertexBufferIndex,
                            element.type.getComponentCount(),
                            GL_BOOL,
                            layout.getStride(),
                            element.offset
                    );
                    vertexBufferIndex++;
                }
                case Mat2, Mat3, Mat4 -> {
                    int count = element.type.getComponentCount();
                    for (int i = 0; i < count; i++) {
                        glEnableVertexAttribArray(vertexBufferIndex);
                        glVertexAttribPointer(
                                vertexBufferIndex,
                                count,
                                GL_FLOAT,
                                element.normalized,
                                layout.getStride(),
                                element.offset + (long) element.type.getTotalByteSize() * i
                        );
                        glVertexAttribDivisor(vertexBufferIndex, 1);
                        vertexBufferIndex++;
                    }
                }
                default -> throw new IllegalArgumentException("Unknown vertex buffer type: " + element.type);
            }
        }
        vertexBuffers.add(vertexBuffer);
    }

    public IndexBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public void setIndexBuffer(final IndexBuffer indexBuffer) {
        bind();
        indexBuffer.bind();
        this.indexBuffer = indexBuffer;
    }
}
