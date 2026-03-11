package game.core;

import game.core.demos.Demo;
import game.core.demos.input.camera_controller.CameraControllerDemo;
import game.core.demos.render.CustomRenderContext;
import game.core.demos.render.gltf.GltfLoadingDemo;
import game.core.demos.render.terrain.TerrainDemo;
import game.core.logging.GameCategories;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.ui.UIScene;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.api.window.WindowMode;
import me.siebe.flux.api.window.WindowPlatform;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.core.FluxApplication;
import me.siebe.flux.opengl.texture.Texture;
import me.siebe.flux.opengl.texture.TextureLoader;
import me.siebe.flux.renderer3d.steps.GltfStep;
import me.siebe.flux.ui.builder.UiBuilder;
import me.siebe.flux.ui.components.UiTexturedElement;
import me.siebe.flux.ui.render.UiRenderStep;
import me.siebe.flux.util.FluxColor;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.util.List;

public class GameApplication extends FluxApplication {
    private static final Logger logger = LoggerFactory.getLogger(GameApplication.class, GameCategories.APPLICATION);

    private List<Demo> demos = List.of(
            new CameraControllerDemo(),
//            new GltfLoadingDemo(),
            new TerrainDemo()
    );

    @Override
    protected void initGameSystems() {
        logger.info("Initializing Game Systems");

        // Init RenderContext
        Renderer renderer = AppContext.get().getRenderer();
        renderer.setRenderContext(
                new CustomRenderContext.Builder()
                        .emptyRenderables()
                        .build()
        );
//        renderer.getPipeline().addStep(new GltfStep());
        renderer.getPipeline().addStep(new UiRenderStep());

//        UIScene scene = AppContext.get().getUi().createScene();
//        UiContainer container = new UiContainer();
//        container.setWidth(700);
//        container.setHeight(700);
//        container.setBackground(FluxColor.RED);
//        scene.setRoot(container);

        UiTexturedElement texturedElement1 = new UiTexturedElement();
        texturedElement1.setSize(100, 100);
        texturedElement1.setPosition(500, 500);
        texturedElement1.setTexture(TextureLoader.get().load("assets/ui/inventoryBg.png"));

        UiTexturedElement texturedElement2 = new UiTexturedElement();
        texturedElement2.setSize(200, 200);
        texturedElement2.setPosition(0, 0);
        texturedElement2.setTexture(TextureLoader.get().load("assets/ui/test.png"));


        UIScene scene = AppContext.get().getUi().createScene();
        scene.setRoot(
                UiBuilder.container()
                        .width(700)
                        .height(400)
                        .background(FluxColor.RED)
//                        .child(UiBuilder.container()
//                                .width(200)
//                                .height(200)
//                                .x(100)
//                                .y(100)
//                                .background(FluxColor.BLUE)
//                                .build())
                        .child(UiBuilder.container()
                                .width(200)
                                .height(200)
                                .background(FluxColor.GREEN)
                                .x(400)
                                .y(100)
                                .child(texturedElement1)
                                .build())
                        .child(texturedElement2)
                        .build()
        );

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
