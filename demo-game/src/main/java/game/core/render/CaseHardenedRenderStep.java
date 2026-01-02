package game.core.render;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.lwjgl.opengl.shader.ShaderLoader;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import me.siebe.flux.lwjgl.opengl.texture.Texture;
import me.siebe.flux.lwjgl.opengl.texture.TextureLoader;
import me.siebe.flux.lwjgl.opengl.vertex.VertexArray;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class CaseHardenedRenderStep implements RenderStep {
    private Texture woodTexture;
    private Texture caseHardenedTexture;
    private Texture bakedTexture;
    private ShaderProgram shader;

    @Override
    public void init() {
        this.shader = ShaderLoader.get().load("shaders/casehardened");

        this.caseHardenedTexture = TextureLoader.get().load("models/ak-47/textures/case_hardened.png",
                () -> new Texture("models/ak-47/textures/case_hardened.png", GL_TEXTURE_2D, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE));

        this.woodTexture = TextureLoader.get().load("models/ak-47/textures/weapon_rif_ak47_baseColor.png",
                () -> new Texture("models/ak-47/textures/weapon_rif_ak47_baseColor.png", GL_TEXTURE_2D, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE));

        // Create framebuffer texture manually (don't use Texture constructor as it generates mipmaps)
        this.bakedTexture = createFramebufferTexture(4096, 4096);

        // Create and setup framebuffer
        int bakeFbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bakeFbo);

        // Attach texture to framebuffer (no need to bind the texture)
        glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D,
                this.bakedTexture.getGlId(),
                0
        );

        // Verify framebuffer is complete
        int fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (fboStatus != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Bake framebuffer not complete. Status: 0x" + Integer.toHexString(fboStatus));
        }

        // Actually bake albedo
        glViewport(0, 0, 4096, 4096);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Clear to transparent black
        glClear(GL_COLOR_BUFFER_BIT);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        shader.bind();

        woodTexture.bindToSlot(0);
        shader.uploadTexture("uWoodAlbedo", 0);

        caseHardenedTexture.bindToSlot(1);
        shader.uploadTexture("uSkin", 1);

        VertexArray vao = new VertexArray();
        vao.bind();
        glDrawArrays(GL_TRIANGLES, 0, 3);
        vao.unbind();

        shader.unbind();

        // Unbind framebuffer before configuring texture
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Generate mipmaps and set filters for the baked texture
        // This must be done after unbinding the framebuffer
        this.bakedTexture.bind();
        glGenerateMipmap(GL_TEXTURE_2D);
        this.bakedTexture.setFilters(GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
        this.bakedTexture.setWrap(GL_REPEAT, GL_REPEAT);
        this.bakedTexture.unbind();

        // Restore viewport
        glViewport(0, 0, AppContext.get().getWindow().getWidth(), AppContext.get().getWindow().getHeight());

        // Clean up framebuffer (texture remains valid)
        glDeleteFramebuffers(bakeFbo);
    }

    /**
     * Creates a texture suitable for use as a framebuffer attachment.
     * The texture is allocated with the specified dimensions.
     *
     * @param width  the width of the texture
     * @param height the height of the texture
     * @return a Texture configured for framebuffer use
     */
    private Texture createFramebufferTexture(int width, int height) {
        // Create texture using constructor - it will generate mipmaps, but we'll regenerate after rendering
        Texture texture = new Texture(width, height, GL_TEXTURE_2D, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, null);

        // Set initial filters to linear (no mipmaps needed until after rendering)
        texture.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        texture.unbind();

        return texture;
    }

    @Override
    public void prepare(BaseRenderContext context) {
        RenderStep.super.prepare(context);
    }

    @Override
    public void execute(BaseRenderContext context) {
//        shader.bind();
//
//        akModel.render();
//
//        shader.unbind();
    }

    /**
     * Gets the baked texture that combines the wood albedo with the case hardened pattern.
     *
     * @return the baked texture, or null if baking hasn't completed yet
     */
    public Texture getBakedTexture() {
        return bakedTexture;
    }
}
