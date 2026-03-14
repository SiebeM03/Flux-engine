package me.siebe.render;

import me.siebe.flux.opengl.texture.Texture;
import me.siebe.flux.opengl.vertex.BufferLayout;
import me.siebe.flux.opengl.vertex.IndexBuffer;
import me.siebe.flux.opengl.vertex.VertexArray;
import me.siebe.flux.opengl.vertex.VertexBuffer;
import me.siebe.texture.TextureBinder;
import me.siebe.texture.TextureUnitAllocator;

import java.util.ArrayList;
import java.util.List;

public class RenderBatch {
    private static final int maxBatchSize = 20;

    private VertexArray vertexArray;
    private VertexBuffer vertexBuffer;
    private IndexBuffer indexBuffer;

    private float[] vertexData;
    private int vertexDataPointer = 0;
    private List<Texture> boundTextures = new ArrayList<>();


    private final TextureUnitAllocator allocator = new TextureUnitAllocator(16);
    private final TextureBinder binder = new TextureBinder(allocator);


    public RenderBatch(BufferLayout bufferLayout) {
        vertexData = new float[maxBatchSize * 4 * bufferLayout.getComponentCount()];

        vertexArray = new VertexArray();
        vertexArray.bind();

        vertexBuffer = new VertexBuffer(maxBatchSize * 4 * bufferLayout.getStride());
        vertexBuffer.bind();
        vertexBuffer.setLayout(bufferLayout);

        indexBuffer = new IndexBuffer(generateIndices());
        indexBuffer.bind();

        vertexArray.addVertexBuffer(vertexBuffer);
        vertexArray.setIndexBuffer(indexBuffer);
    }

    public void bind() {
        vertexArray.bind();
        for (int i = 0; i < boundTextures.size(); i++) {
            boundTextures.get(i).bindToSlot(i + 1);
        }
    }

    public void unbind() {
        for (int i = 0; i < boundTextures.size(); i++) {
            boundTextures.get(i).unbindToSlot(i + 1);
        }
        vertexArray.unbind();
    }

    public void addVertexData(float[] data) {
        for (int i = 0; i < data.length; i++) {
            vertexData[vertexDataPointer++] = data[i];
        }
    }

    public void submitBuffer() {
        vertexBuffer.setData(vertexData);
    }

    public VertexArray getVertexArray() {
        return vertexArray;
    }


    // Textures ========================================================================================================
    public int addTexture(Texture texture) {
        if (texture == null) return 0;
        if (!hasTexture(texture)) {
            boundTextures.add(texture);
        }
        return boundTextures.indexOf(texture) + 1;
    }

    public boolean hasTexture(Texture texture) {
        return boundTextures.contains(texture);
    }

    public boolean hasTextureRoom() {
        return boundTextures.size() < 16;
    }

    private int[] generateIndices() {
        int[] indices = new int[maxBatchSize * 6];
        for (int i = 0; i < maxBatchSize; i++) {
            int vertexStartIndex = 6 * i;
            int offset = 4 * i;

            indices[vertexStartIndex++] = offset + 0;
            indices[vertexStartIndex++] = offset + 2;
            indices[vertexStartIndex++] = offset + 1;

            indices[vertexStartIndex++] = offset + 0;
            indices[vertexStartIndex++] = offset + 3;
            indices[vertexStartIndex++] = offset + 2;
        }
        return indices;
    }

}
