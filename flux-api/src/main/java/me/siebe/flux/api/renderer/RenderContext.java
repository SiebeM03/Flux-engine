package me.siebe.flux.api.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Contains frame-specific rendering information passed through the render pipeline.
 * <p>
 * This context is created each frame and contains information that render steps
 * may need, such as viewport dimensions, timing information, and 3D rendering data
 * (camera matrices, lighting, etc.).
 */
public class RenderContext {
    // Viewport information
    private final int viewportWidth;
    private final int viewportHeight;

    // Timing information
    private final float deltaTime;
    private final double totalTime;

    // 3D rendering context (camera, lighting, etc.)
    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;
    private Vector3f lightPosition;
    private Vector3f lightColor;
    private Vector3f viewPosition;

    /**
     * Creates a new render context with viewport and timing information.
     *
     * @param viewportWidth  the viewport width in pixels
     * @param viewportHeight the viewport height in pixels
     * @param deltaTime      the time since last frame in seconds
     * @param totalTime      the total time since application start in seconds
     */
    public RenderContext(int viewportWidth, int viewportHeight, float deltaTime, double totalTime) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.deltaTime = deltaTime;
        this.totalTime = totalTime;
    }

    /**
     * Sets the 3D rendering context (camera matrices and lighting).
     *
     * @param viewMatrix       the view matrix (camera transform)
     * @param projectionMatrix the projection matrix
     * @param lightPosition    the light position in world space
     * @param lightColor       the light color
     * @param viewPosition     the camera/view position in world space
     */
    public void set3DContext(Matrix4f viewMatrix, Matrix4f projectionMatrix,
                             Vector3f lightPosition, Vector3f lightColor, Vector3f viewPosition) {
        this.viewMatrix = new Matrix4f(viewMatrix);
        this.projectionMatrix = new Matrix4f(projectionMatrix);
        this.lightPosition = new Vector3f(lightPosition);
        this.lightColor = new Vector3f(lightColor);
        this.viewPosition = new Vector3f(viewPosition);
    }

    /**
     * Checks if 3D rendering context is available.
     *
     * @return true if 3D context is set, false otherwise
     */
    public boolean has3DContext() {
        return viewMatrix != null && projectionMatrix != null;
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

    // 3D context getters

    /**
     * Gets the view matrix.
     *
     * @return the view matrix, or null if 3D context is not set
     */
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    /**
     * Gets the projection matrix.
     *
     * @return the projection matrix, or null if 3D context is not set
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * Gets the light position.
     *
     * @return the light position, or null if 3D context is not set
     */
    public Vector3f getLightPosition() {
        return lightPosition;
    }

    /**
     * Gets the light color.
     *
     * @return the light color, or null if 3D context is not set
     */
    public Vector3f getLightColor() {
        return lightColor;
    }

    /**
     * Gets the view position.
     *
     * @return the view position, or null if 3D context is not set
     */
    public Vector3f getViewPosition() {
        return viewPosition;
    }
}
