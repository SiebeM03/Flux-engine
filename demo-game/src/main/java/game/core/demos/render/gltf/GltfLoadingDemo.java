package game.core.demos.render.gltf;

import game.core.demos.Demo;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.renderer3d.model.gltf.loading.GltfLoader;

import java.util.ArrayList;

/**
 * Demo showcasing how to load and render a glTF model.
 *
 * <p>
 * This demo loads a single glTF asset into the active render context. It demonstrates the minimal setup required
 * to import a model using the {@code GltfLoader} and submit it to the renderer.
 * </p>
 *
 * <h2>Behavior</h2>
 * <ul>
 *     <li>Loads a glTF scene from disk.</li>
 *     <li>Adds the loaded model to the renderer so it is displayed.</li>
 * </ul>
 *
 * <p>
 * The example model used in this demo is the widely known “Damaged Helmet”
 * glTF sample asset.
 * </p>
 *
 * <p>
 * This class is intended as a minimal example of integrating glTF model
 * loading into the rendering pipeline.
 * </p>
 *
 * @see me.siebe.flux.renderer3d.model.gltf.loading.GltfLoader
 */
public class GltfLoadingDemo implements Demo {
    @Override
    public void init() {
        Renderer renderer = AppContext.get().getRenderer();
        renderer.getRenderContext().getRenderables().add(GltfLoader.get().load("models/damaged-helmet/scene.gltf"));
    }
    @Override
    public void update() {

    }
    @Override
    public void destroy() {

    }
}
