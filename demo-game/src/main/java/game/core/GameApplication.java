package game.core;

import game.core.logging.GameCategories;
import me.siebe.flux.api.renderer.RenderStep;
import me.siebe.flux.api.renderer.models.Model;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.api.window.WindowMode;
import me.siebe.flux.api.window.WindowPlatform;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.core.FluxApplication;
import me.siebe.flux.lwjgl.gltf.GltfLoader;
import me.siebe.flux.renderer.pipeline.steps.GltfModelRenderStep;
import me.siebe.flux.renderer.pipeline.steps.TriangleStep;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import org.joml.Matrix4f;

import java.io.IOException;

public class GameApplication extends FluxApplication {
    private static final Logger logger = LoggerFactory.getLogger(GameApplication.class, GameCategories.APPLICATION);

    private GltfModelRenderStep gltfModelRenderStep;

    @Override
    protected void initGameSystems() {
        logger.info("Initializing Game Systems");
        AppContext.withContextNoReturn(ctx -> {
            RenderStep step = new TriangleStep("shaders/2d");
            ctx.getRenderPipeline().addStep(step);
            step.init(null);
        });

        try {
            // GltfLoader.loadAsset("models/damaged-helmet/DamagedHelmet.glb");
            // GltfLoader.loadAsset("models/damaged-helmet/scene.gltf");
            // Load a GLTF model
            Model model = GltfLoader.loadAsset("models/damaged-helmet/scene.gltf");

            AppContext.withContextNoReturn(ctx -> {
                gltfModelRenderStep = new GltfModelRenderStep();
                ctx.getRenderPipeline().addStep(gltfModelRenderStep);
                gltfModelRenderStep.registerModel("damaged-helmet", model);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void gameUpdate(final AppContext ctx) {
        logger.trace("Updating Game");

        gltfModelRenderStep.clearInstances();
        Matrix4f transform = new Matrix4f().translate(0, 0, -5);
        gltfModelRenderStep.addInstance("damaged-helmet", transform);
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
