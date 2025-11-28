package me.siebe.flux.renderer.pipeline.steps;

import me.siebe.flux.api.renderer.RenderContext;
import me.siebe.flux.api.renderer.RenderStep;
import me.siebe.flux.util.FluxColor;

import static org.lwjgl.opengl.GL11.*;

/**
 * Render step that clears the framebuffer and specified buffer bits.
 * <p>
 * This is typically the first step in a rendering pipeline to clear the previous frame.
 */
public class ClearStep implements RenderStep {
    private final int clearBits;
    private final FluxColor clearColor;

    /**
     * Creates a clear step that clears color and depth buffers with the specified color.
     *
     * @param color the color to clear the framebuffer with
     */
    public ClearStep(final FluxColor color) {
        this.clearBits = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;
        this.clearColor = color;
    }

    @Override
    public void init(RenderContext context) {
        glClearColor(clearColor.getR(), clearColor.getG(), clearColor.getB(), clearColor.getA());
    }

    @Override
    public void execute(RenderContext context) {
        glClear(clearBits);
    }
}