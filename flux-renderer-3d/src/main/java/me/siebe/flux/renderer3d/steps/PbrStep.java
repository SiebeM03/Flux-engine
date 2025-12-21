package me.siebe.flux.renderer3d.steps;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.data.Renderable;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;

public class PbrStep implements RenderStep {
    private ShaderProgram shader;

    @Override
    public void init() {
        this.shader = new ShaderProgram("shaders/pbr");
    }

    @Override
    public void execute(BaseRenderContext context) {
        shader.bind();
        shader.upload("uViewProj", context.getCamera().getViewProjectionMatrix());


        for (Renderable renderable : context.getRenderables()) {
            renderable.render();
        }
        shader.unbind();
    }

    @Override
    public void destroy() {
        RenderStep.super.destroy();
    }
}
