package me.siebe.flux.ui.render;

import me.siebe.flux.api.renderer.data.Renderable;
import me.siebe.flux.opengl.texture.Texture;
import me.siebe.flux.opengl.vertex.BufferLayout;
import me.siebe.flux.opengl.vertex.IndexBuffer;
import me.siebe.flux.opengl.vertex.VertexArray;
import me.siebe.flux.opengl.vertex.VertexBuffer;
import me.siebe.flux.util.data.buffer.FloatBuffer;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;

public class RenderBatch implements Renderable {
    public static final int MAX_BATCH_SIZE = 2000;

    protected final int maxBatchSize;
    protected final BufferLayout bufferLayout;
    protected final RenderPrimitive renderPrimitive;

    protected VertexArray vertexArray;
    protected VertexBuffer vertexBuffer;
    protected IndexBuffer indexBuffer;

    protected FloatBuffer vertexData;
    protected List<Texture> boundTextures;

    public RenderBatch(int maxBatchSize, RenderPrimitive renderPrimitive, BufferLayout bufferLayout) {
        this.maxBatchSize = maxBatchSize;
        this.bufferLayout = bufferLayout;
        this.renderPrimitive = renderPrimitive;

        this.vertexData = new FloatBuffer(maxBatchSize * renderPrimitive.vertexCount * bufferLayout.getComponentCount());
        this.boundTextures = new ArrayList<>();
    }

    @Override
    public void init() {
        this.vertexArray = new VertexArray();
        this.vertexArray.bind();

        this.vertexBuffer = new VertexBuffer(maxBatchSize * renderPrimitive.vertexCount * bufferLayout.getStride());
        this.vertexBuffer.bind();
        this.vertexBuffer.setLayout(bufferLayout);

        this.indexBuffer = new IndexBuffer(generateIndices());
        this.indexBuffer.bind();

        this.vertexArray.addVertexBuffer(vertexBuffer);
        this.vertexArray.setIndexBuffer(indexBuffer);
    }

    public void bind() {
        vertexArray.bind();
        for (int i = 0; i < boundTextures.size(); i++) {
            boundTextures.get(i).bindToSlot(i + 1);
        }
    }

    public void unbind() {
        for (int i = 0; i < boundTextures.size(); i++) {
            boundTextures.get(i).bindToSlot(i + 1);
            boundTextures.get(i).unbind();
        }
        vertexArray.unbind();
    }

    public void clearBuffer() {
        vertexData.clear();
        boundTextures.clear();
    }

    public void submitBuffer() {
        vertexBuffer.setData(vertexData.toArray());
    }

    public int addTexture(Texture texture) {
        if (texture == null) return 0;  // TODO make -1
//        if (boundTextures.stream().noneMatch(b -> b.getGlId() == texture.getGlId())) {
        if (!boundTextures.contains(texture)) {
            boundTextures.add(texture);
        }
        return boundTextures.indexOf(texture) + 1;  // TODO remove +1
    }

    public int getVertexCount() {
        if (vertexData.size() % bufferLayout.getComponentCount() != 0) {
            throw new IllegalStateException("The vertex buffer has an incomplete vertex. The buffer has " + vertexData.size() + " components but expects a multiple of " + bufferLayout.getComponentCount());
        }
        return (vertexData.size() * renderPrimitive.elementCount) / (bufferLayout.getComponentCount() * renderPrimitive.vertexCount);
    }

    @Override
    public void render() {
        bind();
        glDrawElements(renderPrimitive.openglPrimitive, getVertexCount(), GL_UNSIGNED_INT, 0);
        unbind();
    }

    @Override
    public void destroy() {

    }

    private int[] generateIndices() {
        int[] indices = new int[maxBatchSize * 6];
        for (int i = 0; i < maxBatchSize; i++) {
            renderPrimitive.elementCreation.accept(indices, i);
        }
        return indices;
    }
}
