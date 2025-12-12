package me.siebe.flux.api.camera;

import org.joml.Matrix4f;

public class PerspectiveCamera extends GenericCamera {
    private float fieldOfView;
    private float aspectRatio;
    private float near;
    private float far;

    public PerspectiveCamera(float fieldOfView, float aspectRatio, float near, float far) {
        this.fieldOfView = fieldOfView;
        // TODO auto update with events
        this.aspectRatio = aspectRatio;
        this.near = near;
        this.far = far;
    }

    public PerspectiveCamera(float aspectRatio, float near, float far) {
        this((float) Math.toRadians(60.0), aspectRatio, near, far);
    }

    public void setFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
        this.projectionMatrix.markDirty();
        this.viewProjectionMatrix.markDirty();
    }

    public void setFieldOfViewDegrees(float fieldOfViewDegrees) {
        setFieldOfView((float) Math.toRadians(fieldOfViewDegrees));
    }

    public float getFieldOfView() {
        return this.fieldOfView;
    }

    public float getFieldOfViewDegrees() {
        return (float) Math.toDegrees(fieldOfView);
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        this.projectionMatrix.markDirty();
        this.viewProjectionMatrix.markDirty();
    }

    public void setAspectRatio(int width, int height) {
        setAspectRatio((float) width / height);
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setPlanes(float near, float far) {
        this.near = near;
        this.far = far;
        this.projectionMatrix.markDirty();
        this.viewProjectionMatrix.markDirty();
    }

    public float getNear() {
        return this.near;
    }

    public float getFar() {
        return this.far;
    }

    @Override
    protected void updateProjectionMatrix(Matrix4f m) {
        m.identity().perspective(fieldOfView, aspectRatio, near, far);
    }
}
