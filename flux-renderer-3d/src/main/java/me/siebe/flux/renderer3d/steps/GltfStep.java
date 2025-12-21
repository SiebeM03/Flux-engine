package me.siebe.flux.renderer3d.steps;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import me.siebe.flux.renderer3d.model.data.Model;
import me.siebe.flux.renderer3d.model.gltf.loading.GltfLoader;
import org.joml.Matrix4f;

public class GltfStep implements RenderStep {
    private ShaderProgram shader;
    private Model model;

    @Override
    public void init() {
        this.shader = new ShaderProgram("shaders/gltf");
        this.model = GltfLoader.loadModel("models/cube-ball/untitled.gltf");
    }

    @Override
    public void execute(BaseRenderContext context) {
        shader.bind();
        shader.upload("uViewProj", context.getCamera().getViewProjectionMatrix());
        shader.upload("uModelMatrix", new Matrix4f());

        model.render();

        shader.unbind();
    }

    @Override
    public void destroy() {
        RenderStep.super.destroy();
    }
}
