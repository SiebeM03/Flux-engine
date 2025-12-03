package me.siebe.flux.api.renderer;

import java.util.List;

public abstract class GenericRenderPipeline implements RenderPipeline {
    protected final List<RenderStep> steps;

    protected GenericRenderPipeline(List<RenderStep> steps) {
        this.steps = steps;
    }

    @Override
    public RenderPipeline addStep(RenderStep step) {
        steps.add(step);
        return this;
    }

    @Override
    public RenderPipeline addStep(int index, RenderStep step) {
        steps.add(index, step);
        return this;
    }

    @Override
    public RenderPipeline removeStep(RenderStep step) {
        steps.remove(step);
        return this;
    }

    @Override
    public RenderPipeline removeStep(int index) {
        steps.remove(index);
        return this;
    }

    @Override
    public List<RenderStep> getSteps() {
        return steps;
    }
}
