package me.siebe.render;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.opengl.shader.ShaderDataType;
import me.siebe.flux.opengl.shader.ShaderLoader;
import me.siebe.flux.opengl.shader.ShaderProgram;
import me.siebe.flux.opengl.texture.Texture;
import me.siebe.flux.opengl.texture.TextureLoader;
import me.siebe.flux.opengl.vertex.BufferElement;
import me.siebe.flux.opengl.vertex.BufferLayout;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1iv;

public class DefaultStep extends BatchedRenderStep {
    private ShaderProgram shader = ShaderLoader.get().load("shaders/default");


    public DefaultStep() {
        addWhiteQuad();
        addTextureQuad();
    }

    private void addTextureQuad() {
        Texture texture = TextureLoader.get().load("assets/test.png");
        RenderBatch batch = getAvailableBatch(texture);
        float texId = batch.addTexture(texture);
        batch.addVertexData(new float[]{
                0, 0, 1, 1, texId,
                0, -1, 1, 0, texId,
                -1, -1, 0, 0, texId,
                -1, 0, 0, 1, texId
        });
    }

    private void addWhiteQuad() {
        Texture texture = TextureLoader.get().load("assets/models/damaged-helmet/textures/Material_MR_metallicRoughness.png");
//        texture = null;
        RenderBatch batch = getAvailableBatch(texture);
        float texId = batch.addTexture(texture);
        batch.addVertexData(new float[]{
                1, 1, 1, 0, texId,
                1, 0, 1, 1, texId,
                0, 0, 0, 1, texId,
                0, 1, 0, 0, texId,
        });
    }

    @Override
    protected RenderBatch createBatch() {
        return new RenderBatch(new BufferLayout(
                new BufferElement("aPos", ShaderDataType.Float2),
                new BufferElement("aTexCoords", ShaderDataType.Float2),
                new BufferElement("aTexId", ShaderDataType.Float)
        ));
    }

    @Override
    public void execute(BaseRenderContext context) {
        shader.bind();

        int varLocation = glGetUniformLocation(shader.getGlId(), "uTextures");
        glUniform1iv(varLocation, new int[]{0, 1, 2, 3, 4, 5, 6, 7});


        renderBatches();

        shader.unbind();
    }
}
