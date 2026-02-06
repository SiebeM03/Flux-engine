package me.siebe.flux.renderer3d.steps;

import me.siebe.flux.api.application.AppContext;
import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.data.Renderable;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.lwjgl.opengl.shader.ShaderLoader;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import org.joml.Vector3f;

// TODO rename to a more generic name as it will support more than just GLTF models in the future
public class GltfStep implements RenderStep {
    private ShaderProgram shader;

    protected ShaderProgram getShader() {
        return ShaderLoader.get().load("shaders/gltf");
    }

    @Override
    public void init() {
        this.shader = getShader();
    }

    @Override
    public void prepare(BaseRenderContext context) {
        // TODO add a check (e.g. if (this.shader.isDeleted()) and only then reload from the ShaderLoader)
        //  see GLResource class for the isDeleted() logic
        this.shader = getShader();
        shader.upload("uViewProj", context.getCamera().getViewProjectionMatrix());

        float time = (float) AppContext.get().getTimer().getTotalTime();
        float radius = 10.0f;
        float x = (float) Math.cos(time) * radius;
        float z = (float) Math.sin(time) * radius;

        Vector3f lightDir = new Vector3f(x, 0.0f, z).normalize();
        shader.upload("uLightDir", lightDir);
    }

    @Override
    public void execute(BaseRenderContext context) {
        shader.bind();

        context.getRenderables().forEach(Renderable::render);

        shader.unbind();
    }

    @Override
    public void destroy() {
        shader.delete();
    }
}
