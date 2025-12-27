package me.siebe.flux.renderer3d.steps;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import me.siebe.flux.renderer3d.model.data.Model;
import me.siebe.flux.util.time.Timer;
import org.joml.Vector3f;

public class GltfStep implements RenderStep {
    private ShaderProgram shader;
    private Model model;
    private Timer timer;

    @Override
    public void init() {
        this.shader = new ShaderProgram("shaders/gltf");
//        this.model = GltfLoader.loadModel("models/cube-ball/untitled.gltf");
//        this.model = GltfLoader.loadModel("models/duck/rubber_duck_toy_4k.gltf");
//        this.model = GltfLoader.loadModel("models/damaged-helmet/scene.gltf");

        this.timer = new Timer();
    }

    @Override
    public void execute(BaseRenderContext context) {
        this.timer.update();

        shader.bind();
        shader.upload("uViewProj", context.getCamera().getViewProjectionMatrix());

        float time = (float) this.timer.getTotalTime();
        float radius = 10.0f;
        float x = (float) Math.cos(time) * radius;
        float z = (float) Math.sin(time) * radius;

        Vector3f lightDir = new Vector3f(x, z, 0.0f).normalize();
        shader.upload("uLightDir", lightDir);


        model.render();

        shader.unbind();
    }


    @Override
    public void destroy() {
        RenderStep.super.destroy();
    }
}
