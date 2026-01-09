package me.siebe.flux.lwjgl.opengl.shader;

import me.siebe.flux.util.assets.AssetPool;

public class ShaderLoader extends AssetPool<ShaderProgram> {
    private static ShaderLoader instance;

    private ShaderLoader() {}

    public static ShaderLoader get() {
        if (instance == null) instance = new ShaderLoader();
        return instance;
    }

    @Override
    protected ShaderProgram create(String filepath) {
        return new ShaderProgram(filepath);
    }
}
