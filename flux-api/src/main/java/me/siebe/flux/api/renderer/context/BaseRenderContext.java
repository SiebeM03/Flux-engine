package me.siebe.flux.api.renderer.context;

import org.joml.Matrix4f;

/**
 * Contains frame-specific rendering information passed through the render pipeline.
 * <p>
 * This context is created each frame and contains information that render steps
 * may need, such as viewport dimensions, timing information, and 3D rendering data
 * (camera matrices, lighting, etc.).
 */
public class BaseRenderContext {
    // Viewport information
    protected final int viewportWidth;
    protected final int viewportHeight;

    // Timing information
    protected float deltaTime;
    protected double totalTime;

    //
    protected Matrix4f projectionMatrix;
    protected Matrix4f viewMatrix;
    protected Matrix4f modelMatrix;


    /**
     * Creates a new render context with viewport and timing information.
     *
     * @param viewportWidth  the viewport width in pixels
     * @param viewportHeight the viewport height in pixels
     * @param deltaTime      the time since last frame in seconds
     * @param totalTime      the total time since application start in seconds
     */
    public BaseRenderContext(int viewportWidth, int viewportHeight, float deltaTime, double totalTime) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.deltaTime = deltaTime;
        this.totalTime = totalTime;
    }

    // Viewport getters
    /**
     * Gets the viewport width.
     *
     * @return the viewport width in pixels
     */
    public int getViewportWidth() {
        return viewportWidth;
    }

    /**
     * Gets the viewport height.
     *
     * @return the viewport height in pixels
     */
    public int getViewportHeight() {
        return viewportHeight;
    }

    /**
     * Gets the aspect ratio of the viewport.
     *
     * @return the aspect ratio (width / height)
     */
    public float getAspectRatio() {
        return viewportHeight > 0 ? (float) viewportWidth / viewportHeight : 1.0f;
    }

    // Timing getters
    /**
     * Gets the time since the last frame.
     *
     * @return the delta time in seconds
     */
    public float getDeltaTime() {
        return deltaTime;
    }

    /**
     * Gets the total time since application start.
     *
     * @return the total time in seconds
     */
    public double getTotalTime() {
        return totalTime;
    }



    public void setProjectionMatrix(Matrix4f projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setViewMatrix(Matrix4f viewMatrix) {
        this.viewMatrix = viewMatrix;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public void setModelMatrix(Matrix4f modelMatrix) {
        this.modelMatrix = modelMatrix;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }
}
