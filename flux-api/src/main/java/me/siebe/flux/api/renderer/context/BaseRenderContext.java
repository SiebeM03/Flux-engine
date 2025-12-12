package me.siebe.flux.api.renderer.context;

import me.siebe.flux.api.camera.Camera;

public class BaseRenderContext {
    private Camera camera;

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }
}
