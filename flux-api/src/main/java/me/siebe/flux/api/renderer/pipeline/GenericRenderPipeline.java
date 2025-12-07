package me.siebe.flux.api.renderer.pipeline;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericRenderPipeline implements RenderPipeline {
    protected final List<RenderStep> steps = new ArrayList<>();
    protected boolean initialized = false;

    @Override
    public void addStep(RenderStep step) {
        addStep(step, steps.size());
    }

    @Override
    public void addStep(RenderStep step, int index) {
        steps.add(index, step);

        if (initialized) {
            step.init();
        }
    }

    @Override
    public void removeStep(RenderStep step) {
        steps.remove(step);
        step.destroy();
    }

    @Override
    public void init() {
        steps.forEach(RenderStep::init);
        initialized = true;
    }

    @Override
    public void destroy() {
        steps.forEach(RenderStep::destroy);
        steps.clear();
    }
}
