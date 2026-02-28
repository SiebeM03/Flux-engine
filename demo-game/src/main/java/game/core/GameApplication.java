package game.core;

import game.core.demos.Demo;
import game.core.demos.input.camera_controller.CameraControllerDemo;
import game.core.demos.input.pause_actions.PauseActionsDemo;
import game.core.demos.render.CustomRenderContext;
import game.core.demos.render.gltf.GltfLoadingDemo;
import game.core.demos.render.terrain.TerrainDemo;
import game.core.logging.GameCategories;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.api.window.WindowMode;
import me.siebe.flux.api.window.WindowPlatform;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.core.FluxApplication;
import me.siebe.flux.renderer3d.steps.GltfStep;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GameApplication extends FluxApplication {
    private static final Logger logger = LoggerFactory.getLogger(GameApplication.class, GameCategories.APPLICATION);

    private List<Demo> demos = List.of(
            new CameraControllerDemo(),
            new PauseActionsDemo(),
            new GltfLoadingDemo(),
            new TerrainDemo()
    );

    @Override
    protected void initGameSystems() {
        logger.info("Initializing Game Systems");

        // Init RenderContext
        Renderer renderer = AppContext.get().getRenderer();
        renderer.setRenderContext(new CustomRenderContext());
        renderer.getRenderContext().setRenderables(new ArrayList<>());
        renderer.getPipeline().addStep(new GltfStep());

        demos.forEach(Demo::init);
    }

    @Override
    protected void gameUpdate(final AppContext ctx) {
        logger.trace("Updating Game");

        demos.forEach(Demo::update);
    }

    @Override
    protected void destroyGameSystems() {
        logger.info("Destroying Game Systems");
        demos.forEach(Demo::destroy);
    }

    @Override
    protected WindowBuilder createWindowBuilder() {
        return Window.builder(WindowPlatform.GLFW)
                .title("Demo Game")
                .mode(WindowMode.WINDOWED)
                .vsync(false);
    }
}
