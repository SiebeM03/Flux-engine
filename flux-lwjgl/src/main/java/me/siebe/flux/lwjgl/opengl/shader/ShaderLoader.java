package me.siebe.flux.lwjgl.opengl.shader;

import me.siebe.flux.util.assets.AssetPool;

// FIXME the engine can have multiple shaders with similar names but in different resource folders.
//  Currently there is no distinction between such shaders, which means that if the user tries loading multiple shaders
//  with the same name, ShaderLoader will always return the shader that was first loaded.
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
