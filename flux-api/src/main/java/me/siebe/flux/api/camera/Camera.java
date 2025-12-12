package me.siebe.flux.api.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface Camera {
    Vector3f getPosition();

    void setPosition(Vector3f position);

    Vector3f getDirection();

    Vector3f getUp();

    void lookAt(Vector3f target);

    Matrix4f getViewMatrix();

    Matrix4f getProjectionMatrix();

    Matrix4f getViewProjectionMatrix();

    void update();
}
