package me.siebe.flux.renderer3d.steps;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.opengl.OpenGLState;
import me.siebe.flux.opengl.shader.ShaderDataType;
import me.siebe.flux.opengl.shader.ShaderLoader;
import me.siebe.flux.opengl.shader.ShaderProgram;
import me.siebe.flux.opengl.vertex.BufferElement;
import me.siebe.flux.opengl.vertex.BufferLayout;
import me.siebe.flux.opengl.vertex.VertexArray;
import me.siebe.flux.opengl.vertex.VertexBuffer;

public class OriginStep implements RenderStep {
    private ShaderProgram shader;
    private VertexArray vertexArray;

    protected ShaderProgram getShader() {
        return ShaderLoader.get().load("shaders/color_pos_3D");
    }

    @Override
    public void init() {
        shader = getShader();

        vertexArray = new VertexArray();
        vertexArray.bind();

        BufferLayout bufferLayout = new BufferLayout(
                new BufferElement("aPos", ShaderDataType.Float3, false),
                new BufferElement("aColor", ShaderDataType.Float4, false)
        );

        float[] data = new float[]{
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
        };
        VertexBuffer vertexBuffer = new VertexBuffer(data);
        vertexBuffer.setLayout(bufferLayout);
        vertexArray.addVertexBuffer(vertexBuffer);
    }

    @Override
    public void prepare(BaseRenderContext context) {
        this.shader = getShader();
        OpenGLState.disableCullFace();
    }

    @Override
    public void execute(BaseRenderContext context) {
        shader.bind();
        shader.upload("uViewProj", context.getCamera().getViewProjectionMatrix());

        OpenGLState.drawElements(vertexArray);

        shader.unbind();
    }

    @Override
    public void destroy() {
        shader.delete();
        vertexArray.delete();
    }
}
