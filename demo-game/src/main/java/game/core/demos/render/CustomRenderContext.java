package game.core.demos.render;

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

    public static class Builder extends BaseRenderContext.Builder<CustomRenderContext> {
        private Renderable terrainModel;

        public Builder terrainModel(Renderable terrainModel) {
            this.terrainModel = terrainModel;
            return this;
        }

        @Override
        protected CustomRenderContext createContext() {
            CustomRenderContext ctx = new CustomRenderContext();
            ctx.setTerrainModel(terrainModel);
            return ctx;
        }
    }
}
