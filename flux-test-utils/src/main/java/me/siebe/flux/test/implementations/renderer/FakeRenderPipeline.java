package me.siebe.flux.test.implementations.renderer;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.RenderPipeline;
import me.siebe.flux.api.renderer.pipeline.RenderStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * No-op render pipeline for headless tests. Optionally records how many times {@link #render} was called.
 */
public class FakeRenderPipeline implements RenderPipeline {
    private final List<RenderStep> steps = new ArrayList<>();
    private int renderCallCount;

    @Override
    public void addStep(RenderStep step) {
        steps.add(step);
    }

    @Override
    public void addStep(RenderStep step, int index) {
        steps.add(index, step);
    }

    @Override
    public void removeStep(RenderStep step) {
        steps.remove(step);
    }

    @Override
    public void init() {
        for (RenderStep step : steps) {
            step.init();
        }
    }

    @Override
    public void render(BaseRenderContext context) {
        renderCallCount++;
        for (RenderStep step : steps) {
            step.prepare(context);
            step.execute(context);
            step.finish(context);
        }
    }

    @Override
    public void destroy() {
        for (RenderStep step : steps) {
            step.destroy();
        }
        steps.clear();
    }

    /** Returns how many times {@link #render} has been invoked. */
    public int getRenderCallCount() {
        return renderCallCount;
    }

    /** Returns an unmodifiable view of the steps. */
    public List<RenderStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }
}
