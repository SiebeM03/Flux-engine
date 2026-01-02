package me.siebe.flux.renderer3d.steps;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.data.Renderable;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import me.siebe.flux.lwjgl.opengl.shader.ShaderType;
import me.siebe.flux.util.time.Timer;
import org.joml.Vector3f;

// TODO rename to a more generic name as it will support more than just GLTF models in the future
public class GltfStep implements RenderStep {
    private ShaderProgram shader;
    private Timer timer;

    @Override
    public void init() {
        this.shader = ShaderType.PBR.getShader();
        this.timer = new Timer();
    }

    @Override
    public void prepare(BaseRenderContext context) {
        shader.upload("uViewProj", context.getCamera().getViewProjectionMatrix());

        float time = (float) this.timer.getTotalTime();
        float radius = 10.0f;
        float x = (float) Math.cos(time) * radius;
        float z = (float) Math.sin(time) * radius;

        Vector3f lightDir = new Vector3f(x, 0.0f, z).normalize();
        shader.upload("uLightDir", lightDir);
    }

    @Override
    public void execute(BaseRenderContext context) {
        this.timer.update();
        shader.bind();

        context.getRenderables().forEach(Renderable::render);

        shader.unbind();
    }
}
