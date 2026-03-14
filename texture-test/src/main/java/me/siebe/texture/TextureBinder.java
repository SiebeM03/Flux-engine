package me.siebe.texture;

import me.siebe.flux.opengl.shader.ShaderProgram;
import me.siebe.flux.opengl.texture.Texture;

public class TextureBinder {
    public final TextureUnitAllocator allocator;

    public TextureBinder(TextureUnitAllocator allocator) {
        this.allocator = allocator;
    }

    public void bindTexture(ShaderProgram shader, String uniformName, Texture texture) {
        int slot = allocator.allocate();

        texture.bindToSlot(slot);

        shader.upload(uniformName, slot);
    }

    public void bindTextureArray(ShaderProgram shader, String uniformName, Texture[] textures) {
        int[] slots = new int[textures.length];

        for (int i = 0; i < textures.length; i++) {
            int slot = allocator.allocate();
            textures[i].bindToSlot(slot);
            slots[i] = slot;
        }

        shader.upload(uniformName, slots);
    }
}
