package me.siebe.flux.api.camera;

import me.siebe.flux.api.application.AppContext;
import me.siebe.flux.api.event.common.FramebufferResizeEvent;
import org.joml.Matrix4f;

public class OrthographicCamera extends GenericCamera {
    private float left;
    private float right;
    private float bottom;
    private float top;
    private float near;
    private float far;

    public OrthographicCamera(float left, float right, float bottom, float top, float near, float far) {
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.near = near;
        this.far = far;

        AppContext.get().getEventBus().getListenerRegistry().register(FramebufferResizeEvent.class, e -> {
            // TODO add event listener for framebuffer resize
        });
    }

    public void setProjection(float left, float right, float bottom, float top, float near, float far) {
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.near = near;
        this.far = far;

        this.projectionMatrix.markDirty();
        this.viewProjectionMatrix.markDirty();
    }

    public void setProjection(float width, float height, float near, float far) {
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        setProjection(-halfWidth, halfWidth, -halfHeight, halfHeight, near, far);
    }

    public float getLeft() {
        return this.left;
    }

    public float getRight() {
        return this.right;
    }

    public float getBottom() {
        return this.bottom;
    }

    public float getTop() {
        return this.top;
    }

    public float getNear() {
        return this.near;
    }

    public float getFar() {
        return this.far;
    }

    @Override
    protected void updateProjectionMatrix(Matrix4f m) {
        m.identity().ortho(left, right, bottom, top, near, far);
    }
}
