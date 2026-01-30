package me.siebe.flux.api.camera;

import me.siebe.flux.util.DirtyValue;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class GenericCamera implements Camera {
    protected final Vector3f position;
    protected final Vector3f direction;
    protected final Vector3f up;
    protected final Vector3f target;

    protected final DirtyValue<Matrix4f> viewMatrix;
    protected final DirtyValue<Matrix4f> projectionMatrix;
    protected final DirtyValue<Matrix4f> viewProjectionMatrix;

    public GenericCamera() {
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.direction = new Vector3f(0.0f, 0.0f, -1.0f);
        this.up = new Vector3f(0.0f, 1.0f, 0.0f);
        this.target = new Vector3f(0.0f, 0.0f, 0.0f);


        this.viewMatrix = new DirtyValue<>(new Matrix4f(), this::updateViewMatrix);
        this.projectionMatrix = new DirtyValue<>(new Matrix4f(), this::updateProjectionMatrix);
        this.viewProjectionMatrix = new DirtyValue<>(new Matrix4f(), this::updateViewProjectionMatrix);
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f(this.position);
    }

    @Override
    public void setPosition(Vector3f position) {
        this.position.set(position);
        this.viewMatrix.markDirty();
        this.viewProjectionMatrix.markDirty();
    }

    @Override
    public Vector3f getDirection() {
        return new Vector3f(this.direction);
    }

    @Override
    public Vector3f getUp() {
        return new Vector3f(this.up);
    }

    @Override
    public void lookAt(Vector3f target) {
        this.target.set(target);
        this.direction.set(this.target).sub(this.position).normalize();
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
        m.identity().lookAt(position, target, up);
    }

    protected abstract void updateProjectionMatrix(Matrix4f m);

    protected void updateViewProjectionMatrix(Matrix4f m) {
        Matrix4f proj = projectionMatrix.get();
        Matrix4f view = viewMatrix.get();
        m.set(proj).mul(view);
    }
}