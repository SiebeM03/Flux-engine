package me.siebe;

import me.siebe.flux.opengl.shader.ShaderDataType;
import me.siebe.flux.opengl.texture.Texture;
import me.siebe.flux.opengl.vertex.*;
import me.siebe.texture.TextureBinder;
import me.siebe.texture.TextureUnitAllocator;

import java.util.ArrayList;
import java.util.List;

public class DataHandler {
    private static final int maxBatchSize = 20;

    private final BufferLayout bufferLayout = new BufferLayout(
            new BufferElement("aPos", ShaderDataType.Float2),
            new BufferElement("aTexCoords", ShaderDataType.Float2),
            new BufferElement("aTexId", ShaderDataType.Float)
    );

    private VertexArray vertexArray;
    private VertexBuffer vertexBuffer;
    private IndexBuffer indexBuffer;

    private List<Texture> boundTextures = new ArrayList<>();


    private final TextureUnitAllocator allocator = new TextureUnitAllocator(16);
    private final TextureBinder binder = new TextureBinder(allocator);


    public DataHandler() {
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
        vertexArray.unbind();
        for (int i = 0; i < boundTextures.size(); i++) {
            boundTextures.get(i).unbindToSlot(i + 1);
        }
    }

    public void pushVertexData(float[] data) {
        vertexBuffer.setData(data);
    }

    public int addTexture(Texture texture) {
        if (texture == null) return 0;
        if (!boundTextures.contains(texture)) {
            boundTextures.add(texture);
        }
        return boundTextures.indexOf(texture) + 1;
    }

    public VertexArray getVertexArray() {
        return vertexArray;
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
