package game.core;

import game.core.logging.GameCategories;
import game.core.temp.TempCameraSetup;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.api.window.WindowMode;
import me.siebe.flux.api.window.WindowPlatform;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.core.FluxApplication;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import me.siebe.flux.renderer3d.model.data.Model;
import me.siebe.flux.renderer3d.model.gltf.loading.GltfLoader;
import me.siebe.flux.renderer3d.steps.GltfStep;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import org.joml.Matrix4f;

public class GameApplication extends FluxApplication {
    private static final Logger logger = LoggerFactory.getLogger(GameApplication.class, GameCategories.APPLICATION);

    private TempCameraSetup cameraSetup;

    @Override
    protected void initGameSystems() {
        logger.info("Initializing Game Systems");
        this.cameraSetup = new TempCameraSetup();
        this.cameraSetup.init();

        AppContext.get().getRenderer().getPipeline().addStep(new GltfStep());
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
