package me.siebe.flux.renderer2d;

import me.siebe.flux.api.renderer.GenericRenderPipeline;
import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.RenderStep;

import java.util.ArrayList;
import java.util.List;

public class Basic2DRenderPipeline extends GenericRenderPipeline {
    private boolean initialized = false;

    public Basic2DRenderPipeline() {
        this(new ArrayList<>());
    }

    public Basic2DRenderPipeline(List<RenderStep> steps) {
        super(steps);
    }

    @Override
    public void render(BaseRenderContext context) {
        if (!initialized) {
            init(context);
        }

        steps.forEach(step -> step.execute(context));
    }

    @Override
    public void init(BaseRenderContext context) {
        steps.forEach(step -> step.init(context));

        this.initialized = true;
    }

    @Override
    public void destroy() {
        steps.forEach(RenderStep::destroy);
    }
}
