package me.siebe.flux.renderer3d.steps;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.api.ui.api.Ui;
import me.siebe.flux.api.ui.api.UiComponent;
import me.siebe.flux.opengl.OpenGLState;
import me.siebe.flux.opengl.shader.ShaderDataType;
import me.siebe.flux.opengl.shader.ShaderLoader;
import me.siebe.flux.opengl.shader.ShaderProgram;
import me.siebe.flux.opengl.vertex.*;
import me.siebe.flux.util.data.FloatBuffer;
import me.siebe.flux.util.data.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class UiRenderStep implements RenderStep {
    private ShaderProgram shader;
    private FloatBuffer vertexData;
    private IntBuffer indexData;

    private VertexArray vertexArray;
    private VertexBuffer vertexBuffer;
    private BufferLayout bufferLayout;
    private IndexBuffer indexBuffer;

    protected ShaderProgram getShader() {
        return ShaderLoader.get().load("shaders/ui");
    }

    @Override
    public void init() {
        vertexData = new FloatBuffer(128);
        indexData = new IntBuffer(128);
        // VAO
        vertexArray = new VertexArray();
        vertexArray.bind();
        // VBO
        bufferLayout = new BufferLayout(
                new BufferElement("aPos", ShaderDataType.Float2, false),
                new BufferElement("aColor", ShaderDataType.Float4, false)
        );
        vertexBuffer = new VertexBuffer(128);
        vertexBuffer.setLayout(bufferLayout);
        vertexArray.addVertexBuffer(vertexBuffer);
        // EBO
        indexBuffer = new IndexBuffer(new int[]{
                0, 2, 1,
                0, 3, 2
        });
        vertexArray.setIndexBuffer(indexBuffer);
    }

    @Override
    public void prepare(BaseRenderContext context) {
        shader = getShader();

        populateBufferData();
        validateBufferData();

        vertexArray.bind();
        vertexBuffer.setData(vertexData.toArray());
        indexBuffer.setData(indexData.toArray());
    }

    @Override
    public void execute(BaseRenderContext context) {
        OpenGLState.disableDepthTest();
        OpenGLState.enableBlend();
        OpenGLState.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.upload("uProjection", Ui.CONTAINER.getProjectionMatrix());

        OpenGLState.drawElements(vertexArray);

        shader.unbind();

        OpenGLState.enableDepthTest();
    }

    @Override
    public void destroy() {
        shader.delete();
        vertexArray.delete();
        vertexBuffer.delete();
        indexBuffer.delete();
    }

    private void populateBufferData() {
        vertexData.clear();
        indexData.clear();
        addComponentToBuffer(Ui.CONTAINER);
    }

    private void addComponentToBuffer(UiComponent component) {
        int lastIndex = vertexData.size() / bufferLayout.getComponentCount();
        for (int i = 0; i < 4; i++) {
            //  i3 ---------- i0
            //  |              |
            //  i2 ---------- i1
            float xAdd = (i == 0 || i == 1) ? component.getSize().x : 0;
            float yAdd = (i == 0 || i == 3) ? component.getSize().y : 0;

            // aPos
            vertexData.add(component.getPosition().x + xAdd);
            vertexData.add(component.getPosition().y + yAdd);
            // aColor
            vertexData.add(component.getColor().redFloat());
            vertexData.add(component.getColor().greenFloat());
            vertexData.add(component.getColor().blueFloat());
            vertexData.add(component.getColor().alphaFloat());
        }

        indexData.add(lastIndex + 0);
        indexData.add(lastIndex + 2);
        indexData.add(lastIndex + 1);
        indexData.add(lastIndex + 0);
        indexData.add(lastIndex + 3);
        indexData.add(lastIndex + 2);

        //        0, 2, 1,
        //        0, 3, 2
    }

    private void validateBufferData() {
        if (vertexData.size() % bufferLayout.getComponentCount() != 0) {
            throw new IllegalStateException("The vertex buffer has an incomplete vertex. The buffer has " + vertexData.size() + " components but expects a multiple of " + bufferLayout.getComponentCount());
        }
    }
}
