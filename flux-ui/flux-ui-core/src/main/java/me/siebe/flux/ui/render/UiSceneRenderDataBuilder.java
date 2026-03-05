package me.siebe.flux.ui.render;

import me.siebe.flux.opengl.shader.ShaderDataType;
import me.siebe.flux.opengl.vertex.*;
import me.siebe.flux.ui.UIElement;
import me.siebe.flux.ui.UIScene;
import me.siebe.flux.util.data.buffer.FloatBuffer;
import me.siebe.flux.util.data.buffer.IntBuffer;

class UiSceneRenderDataBuilder {
    private FloatBuffer vertexData;
    private IntBuffer indexData;

    private VertexArray vertexArray;
    private VertexBuffer vertexBuffer;
    private IndexBuffer indexBuffer;

    UiSceneRenderDataBuilder() {
        vertexData = new FloatBuffer(128);
        indexData = new IntBuffer(128);

        // VAO
        vertexArray = new VertexArray();
        vertexArray.bind();
        // VBO
        BufferLayout bufferLayout = new BufferLayout(
                new BufferElement("aPos", ShaderDataType.Float2, false),
                new BufferElement("aColor", ShaderDataType.Float4, false)
        );
        vertexBuffer = new VertexBuffer(128);
        vertexBuffer.setLayout(bufferLayout);
        vertexArray.addVertexBuffer(vertexBuffer);
        // EBO
        indexBuffer = new IndexBuffer(new int[0]);
        vertexArray.setIndexBuffer(indexBuffer);
    }

    void clear() {
        vertexData.clear();
        indexData.clear();
    }

    void addScene(UIScene scene) {
        addElementToBuffer(scene.getRoot());

        vertexArray.bind();
        vertexBuffer.setData(vertexData.toArray());
        indexBuffer.setData(indexData.toArray());
    }

    VertexArray getVertexArray() {
        return vertexArray;
    }

    private void addElementToBuffer(UIElement element) {
        int lastIndex = vertexData.size() / vertexBuffer.getLayout().getComponentCount();
        for (int i = 0; i < 4; i++) {
            //  i3 ---------- i0
            //  |              |
            //  i2 ---------- i1
            float xAdd = (i == 0 || i == 1) ? element.getWidth() : 0;
            float yAdd = (i == 0 || i == 3) ? element.getHeight() : 0;

            // aPos
            vertexData.add(element.getX() + xAdd);
            vertexData.add(element.getY() + yAdd);
            // aColor
            vertexData.add(element.getBackground().redFloat());
            vertexData.add(element.getBackground().greenFloat());
            vertexData.add(element.getBackground().blueFloat());
            vertexData.add(element.getBackground().alphaFloat());
        }

        indexData.add(lastIndex + 0);
        indexData.add(lastIndex + 2);
        indexData.add(lastIndex + 1);
        indexData.add(lastIndex + 0);
        indexData.add(lastIndex + 3);
        indexData.add(lastIndex + 2);

        for (UIElement child : element.getChildren()) {
            addElementToBuffer(child);
        }
    }

    void delete() {
        vertexArray.delete();
        // VertexBuffer and IndexBuffer are deleted as dependencies of VertexArray
    }
}
