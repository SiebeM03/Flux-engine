package me.siebe.flux.api.renderer;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.util.system.ProvidableSystem;
import me.siebe.flux.util.system.SystemProvider;
import me.siebe.flux.util.system.SystemProviderType;

import java.util.List;

/**
 * Represents a rendering pipeline that executes a sequence of rendering steps.
 * <p>
 * The pipeline processes rendering steps in order, allowing for extensible and
 * customizable rendering workflows. Games can add custom steps to extend the
 * default rendering behavior.
 */
public interface RenderPipeline extends ProvidableSystem {
    /**
     * Creates a new default rendering pipeline instance.
     * <p>
     * The returned pipeline is ready to use and can have steps added to it.
     *
     * @return a new default rendering pipeline
     */
    static RenderPipeline create() {
        return SystemProvider.provide(RenderPipeline.class, SystemProviderType.ALL);
    }

    /**
     * Executes all steps in the pipeline in order.
     * <p>
     * This method should be called once per frame to render the scene.
     *
     * @param context the rendering context for this frame
     */
    void render(BaseRenderContext context);

    /**
     * Adds a rendering step to the end of the pipeline.
     * <p>
     * The step will be executed after all previously added steps.
     *
     * @param step the step to add
     * @return this pipeline instance for method chaining
     */
    RenderPipeline addStep(RenderStep step);

    /**
     * Adds a rendering step at a specific position in the pipeline.
     * <p>
     * The step will be inserted at the specified index, shifting subsequent
     * steps to the right.
     *
     * @param index the position to insert the step (0-based)
     * @param step  the step to add
     * @return this pipeline instance for method chaining
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    RenderPipeline addStep(int index, RenderStep step);

    /**
     * Removes a rendering step from the pipeline.
     * <p>
     * If the step appears multiple times, only the first occurrence is removed.
     *
     * @param step the step to remove
     * @return this pipeline instance for method chaining
     */
    RenderPipeline removeStep(RenderStep step);

    /**
     * Removes a rendering step at a specific position in the pipeline.
     *
     * @param index the position of the step to remove (0-based)
     * @return this pipeline instance for method chaining
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    RenderPipeline removeStep(int index);

    /**
     * Gets all steps in the pipeline.
     * <p>
     * The returned list is a copy and modifications to it will not affect the pipeline.
     *
     * @return an immutable list of all steps in execution order
     */
    List<RenderStep> getSteps();

    /**
     * Initializes all steps in the pipeline.
     * <p>
     * This should be called once after the pipeline is constructed and before
     * the first frame is rendered.
     *
     * @param context the initial rendering context
     */
    void init(BaseRenderContext context);

    /**
     * Destroys all steps in the pipeline.
     * <p>
     * This should be called when the pipeline is no longer needed to clean up
     * resources allocated by the steps.
     */
    void destroy();
}