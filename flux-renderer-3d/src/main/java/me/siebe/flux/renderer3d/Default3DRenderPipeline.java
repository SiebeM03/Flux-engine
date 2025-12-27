package me.siebe.flux.renderer3d;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.GenericRenderPipeline;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.renderer3d.steps.ClearStep;

public class Default3DRenderPipeline extends GenericRenderPipeline {

    @Override
    public void init() {
        super.init();

        addStep(new ClearStep());
    }

    @Override
    public void render(BaseRenderContext context) {
        if (!initialized) {
            init();
        }

        for (RenderStep step : steps) {
            step.prepare(context);
            step.execute(context);
            step.finish(context);
        }
    }
}
