package game.core.demos.render.terrain;

import game.core.demos.Demo;
import game.core.demos.render.CustomRenderContext;
import me.siebe.flux.api.renderer.Renderer;
import me.siebe.flux.api.renderer.data.Renderable;
import me.siebe.flux.core.AppContext;

/**
 * Demo showcasing procedural terrain generation and rendering.
 *
 * <p>
 * This demo generates a terrain model at runtime using a seeded
 * {@code TerrainGenerator} and registers it within a
 * {@code CustomRenderContext}. The generated terrain is then added
 * to the renderer so it can be drawn as part of the scene.
 * </p>
 *
 * <h2>Behavior</h2>
 * <ul>
 *     <li>Checks whether the active render context is a {@code CustomRenderContext}.</li>
 *     <li>Generates a terrain model using a fixed random seed for reproducibility.</li>
 *     <li>Registers the generated terrain model inside the custom render context.</li>
 *     <li>Adds the terrain to the renderer's renderable list.</li>
 * </ul>
 *
 * <h2>Terrain Generation</h2>
 * <p>
 * The terrain is generated with:
 * </p>
 * <ul>
 *     <li>Width: 100 units</li>
 *     <li>Depth: 100 units</li>
 *     <li>Scale: 1.0</li>
 *     <li>Max height: 1.0</li>
 * </ul>
 *
 * <p>
 * The use of a fixed seed ensures deterministic terrain generation
 * across runs.
 * </p>
 *
 * @see TerrainGenerator
 * @see game.core.demos.render.CustomRenderContext
 */
public class TerrainDemo implements Demo {
    @Override
    public void init() {
        Renderer renderer = AppContext.get().getRenderer();
        if (renderer.getRenderContext() instanceof CustomRenderContext renderContext) {
            Renderable terrainModel = new TerrainGenerator(123345L).generateTerrainModel(100, 100, 1.0f, 1.0f);
            renderContext.setTerrainModel(terrainModel);
            renderContext.getRenderables().add(terrainModel);
        }
    }
    @Override
    public void update() {

    }
    @Override
    public void destroy() {

    }
}
