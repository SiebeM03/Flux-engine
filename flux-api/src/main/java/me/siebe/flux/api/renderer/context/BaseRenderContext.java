package me.siebe.flux.api.renderer.context;

import me.siebe.flux.api.camera.Camera;
import me.siebe.flux.api.renderer.data.Renderable;

import java.util.ArrayList;
import java.util.List;

public class BaseRenderContext {
    private Camera camera;
    // TODO this should not be stored here, this should just retrieve a copy/reference of the actual list to be used
    //  in the render pipeline
    private List<Renderable> renderables = new ArrayList<>();

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public List<Renderable> getRenderables() {
        return renderables;
    }

    public void setRenderables(List<Renderable> renderables) {
        this.renderables = renderables;
    }
}
