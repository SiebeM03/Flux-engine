package me.siebe.flux.api.renderer;

import me.siebe.flux.api.renderer.context.BaseRenderContext;

/**
 * Represents a single step in the rendering pipeline.
 * <p>
 * Each step is executed in order during the rendering process. Steps can perform
 * operations such as clearing buffers, rendering geometry, applying post-processing
 * effects, or any other rendering-related task.
 * <p>
 * Implementations should be stateless where possible, as the same step instance
 * may be reused across multiple frames.
 */
public interface RenderStep {
    /**
     * Executes this rendering step.
     * <p>
     * This method is called once per frame for each step in the pipeline, in the
     * order they were added to the pipeline.
     *
     * @param context the rendering context containing frame information
     */
    void execute(BaseRenderContext context);

    /**
     * Gets the name of this rendering step.
     * <p>
     * Used for debugging and logging purposes.
     *
     * @return the step name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Called when the rendering pipeline is initialized.
     * <p>
     * Override this method to perform any one-time setup required by this step,
     * such as loading shaders, creating buffers, or initializing resources.
     *
     * @param context the initial rendering context
     */
    default void init(BaseRenderContext context) {
        // Default implementation does nothing
    }

    /**
     * Called when the rendering pipeline is destroyed.
     * <p>
     * Override this method to clean up any resources allocated by this step,
     * such as releasing buffers, deleting shaders, or freeing memory.
     */
    default void destroy() {
        // Default implementation does nothing
    }
}

