package me.siebe.flux.ui.components;

import me.siebe.flux.opengl.texture.Texture;
import me.siebe.flux.opengl.texture.TextureLoader;

public class UiTexturedElement extends UiContainer {
    protected Texture texture;

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public void setTexture(String texturePath) {
        this.texture = TextureLoader.get().load(texturePath);
    }

    public Texture getTexture() {
        return texture;
    }
}
