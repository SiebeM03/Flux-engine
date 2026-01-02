package me.siebe.flux.lwjgl.opengl.vertex;

import me.siebe.flux.util.exceptions.Validator;

public class VaoBuilder {
    private VertexArray vertexArray;
    private BufferLayout bufferLayout;
    private float[] vertices;
    private int[] indices;

    private VaoBuilder() {}

    public static VaoBuilder create() {
        return new VaoBuilder();
    }

    public VaoBuilder setBufferElements(BufferElement... bufferElements) {
        this.bufferLayout = new BufferLayout(bufferElements);
        return this;
    }

    public VaoBuilder setBufferLayout(BufferLayout bufferLayout) {
        this.bufferLayout = bufferLayout;
        return this;
    }

    public VaoBuilder addVertices(float[] data) {
        this.vertices = data;
        return this;
    }

    public VaoBuilder addIndices(int[] data) {
        this.indices = data;
        return this;
    }

    public VertexArray build() {
        Validator.notNull(bufferLayout);
        Validator.notNull(vertices);
        Validator.notNull(indices);

        this.vertexArray = new VertexArray();
        vertexArray.bind();

        VertexBuffer vertexBuffer = new VertexBuffer(vertices);
        vertexBuffer.setLayout(bufferLayout);
        vertexArray.addVertexBuffer(vertexBuffer);

        IndexBuffer indexBuffer = new IndexBuffer(indices);
        vertexArray.setIndexBuffer(indexBuffer);

        return vertexArray;
    }
}
