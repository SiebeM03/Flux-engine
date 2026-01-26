package me.siebe.flux.api.camera;

import me.siebe.flux.util.DirtyValue;
import me.siebe.flux.util.Transform;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class GenericCamera implements Camera {
    /**
     * The transform for this camera (position and rotation).
     * Scale is not used for cameras.
     */
    protected final Transform transform;

    /**
     * The up vector used for lookAt calculations.
     * Defaults to (0, 1, 0).
     */
    protected final Vector3f up;

    protected final DirtyValue<Matrix4f> viewMatrix;
    protected final DirtyValue<Matrix4f> projectionMatrix;
    protected final DirtyValue<Matrix4f> viewProjectionMatrix;

    public GenericCamera() {
        this.transform = new Transform();
        this.up = new Vector3f(0.0f, 1.0f, 0.0f);

        this.viewMatrix = new DirtyValue<>(new Matrix4f(), this::updateViewMatrix);
        this.projectionMatrix = new DirtyValue<>(new Matrix4f(), this::updateProjectionMatrix);
        this.viewProjectionMatrix = new DirtyValue<>(new Matrix4f(), this::updateViewProjectionMatrix);
    }

    public Transform getTransform() {
        return transform;
    }

    @Override
    public Vector3f getPosition() {
        return transform.getPosition();
    }

    @Override
    public void setPosition(Vector3f position) {
        transform.setPosition(position);
        this.viewMatrix.markDirty();
        this.viewProjectionMatrix.markDirty();
    }

    @Override
    public Vector3f getDirection() {
        return transform.getForward();
    }

    @Override
    public Vector3f getUp() {
        return new Vector3f(this.up);
    }

    /**
     * Sets the up vector used for lookAt calculations.
     *
     * @param up The up vector (typically (0, 1, 0))
     */
    public void setUp(Vector3f up) {
        if (up != null) {
            this.up.set(up);
            this.viewMatrix.markDirty();
            this.viewProjectionMatrix.markDirty();
        }
    }

    @Override
    public void lookAt(Vector3f target) {
        transform.lookAt(target, this.up);
        this.viewMatrix.markDirty();
        this.viewProjectionMatrix.markDirty();
    }

    @Override
    public Matrix4f getViewMatrix() {
        return new Matrix4f(this.viewMatrix.get());
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return new Matrix4f(this.projectionMatrix.get());
    }

    @Override
    public Matrix4f getViewProjectionMatrix() {
        return new Matrix4f(this.viewProjectionMatrix.get());
    }


    @Override
    public void update() {

    }

    protected void updateViewMatrix(Matrix4f m) {
        // Compute view matrix from transform
        // View matrix is the inverse of the model matrix
        Vector3f position = transform.getPosition();
        Vector3f forward = transform.getForward();
        Vector3f target = new Vector3f(position).add(forward);
        m.identity().lookAt(position, target, up);
    }

    protected abstract void updateProjectionMatrix(Matrix4f m);

    protected void updateViewProjectionMatrix(Matrix4f m) {
        Matrix4f proj = projectionMatrix.get();
        Matrix4f view = viewMatrix.get();
        m.set(proj).mul(view);
    }
}
