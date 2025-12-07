package me.siebe.flux.api.renderer.context;

import org.joml.Matrix4f;

public class BaseRenderContext {
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}
