package game.core;

import game.core.logging.GameCategories;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.api.window.WindowMode;
import me.siebe.flux.api.window.WindowPlatform;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.core.FluxApplication;
import me.siebe.flux.lwjgl.gltf.GltfLoader;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.io.IOException;

public class GameApplication extends FluxApplication {
    private static final Logger logger = LoggerFactory.getLogger(GameApplication.class, GameCategories.APPLICATION);

    @Override
    protected void initGameSystems() {
        logger.info("Initializing Game Systems");

        try {
            GltfLoader.loadAsset("models/damaged-helmet/DamagedHelmet.glb");
//            GltfLoader.loadAsset("models/damaged-helmet/scene.gltf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void gameUpdate(final AppContext ctx) {
        logger.trace("Updating Game");
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
