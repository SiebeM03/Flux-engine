package game.core;

import game.core.logging.GameCategories;
import game.core.render.CustomRenderContext;
import game.core.temp.TempCameraSetup;
import game.core.temp.TerrainGenerator;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.renderer.data.Renderable;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.api.window.WindowMode;
import me.siebe.flux.api.window.WindowPlatform;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.core.FluxApplication;
import me.siebe.flux.renderer3d.model.gltf.loading.GltfLoader;
import me.siebe.flux.renderer3d.steps.GltfStep;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.util.ArrayList;

public class GameApplication extends FluxApplication {
    private static final Logger logger = LoggerFactory.getLogger(GameApplication.class, GameCategories.APPLICATION);

    private TempCameraSetup cameraSetup;

    @Override
    protected void initGameSystems() {
        logger.info("Initializing Game Systems");

        Renderer renderer = AppContext.get().getRenderer();
        renderer.setRenderContext(new CustomRenderContext());

        renderer.getPipeline().addStep(new GltfStep());

        renderer.getRenderContext().setRenderables(new ArrayList<>());
        renderer.getRenderContext().getRenderables().add(GltfLoader.get().load("models/damaged-helmet/scene.gltf"));

        if (renderer.getRenderContext() instanceof CustomRenderContext renderContext) {
            Renderable terrainModel = new TerrainGenerator(123345L).generateTerrainModel(100, 100, 1.0f, 1.0f);
            renderContext.setTerrainModel(terrainModel);
            renderContext.getRenderables().add(terrainModel);
        }

        this.cameraSetup = new TempCameraSetup();
        this.cameraSetup.init();
    }

    @Override
    protected void gameUpdate(final AppContext ctx) {
        logger.trace("Updating Game");
        this.cameraSetup.update(ctx);
    }

    @Override
    protected void destroyGameSystems() {
        logger.info("Destroying Game Systems");
    }

    @Override
    protected WindowBuilder createWindowBuilder() {
        return Window.builder(WindowPlatform.GLFW)
                .title("Demo Game")
                .mode(WindowMode.WINDOWED)
                .vsync(false);
    }
}
