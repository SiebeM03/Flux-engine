package game.core.render;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.data.Renderable;

public class CustomRenderContext extends BaseRenderContext {
    private Renderable terrainModel;

    public Renderable getTerrainModel() {
        return terrainModel;
    }

    public void setTerrainModel(Renderable terrainModel) {
        this.terrainModel = terrainModel;
    }
}
