package me.siebe.flux.opengl.texture;

import me.siebe.flux.util.assets.AssetPool;

public class TextureLoader extends AssetPool<Texture> {
    private static TextureLoader instance;

    private TextureLoader() {}

    public static TextureLoader get() {
        if (instance == null) instance = new TextureLoader();
        return instance;
    }

    @Override
    protected Texture create(String filepath) {
        return new Texture(filepath);
    }
}
