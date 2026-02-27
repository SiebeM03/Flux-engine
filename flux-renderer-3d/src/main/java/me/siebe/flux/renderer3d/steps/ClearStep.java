package me.siebe.flux.renderer3d.steps;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.opengl.OpenGLState;
import me.siebe.flux.util.FluxColor;

public class ClearStep implements RenderStep {

    private final FluxColor clearColor;

    public ClearStep() {
        this.clearColor = new FluxColor(0.1f, 0.1f, 0.1f);
    }

    public ClearStep(FluxColor clearColor) {
        this.clearColor = clearColor;
    }


    @Override
    public void init() {
        OpenGLState.setClearColor(clearColor);
    }

    @Override
    public void execute(BaseRenderContext context) {
        OpenGLState.clearColorAndDepth();
    }
}
