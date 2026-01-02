package me.siebe.flux.lwjgl.opengl.shader;

public enum ShaderType {
    PBR("shaders/pbr"),
    SIMPLE_3D("shaders/simple3D");

    private final String path;

    ShaderType(String path) {
        this.path = path;
    }

    public ShaderProgram getShader() {
        return ShaderLoader.get().load(path);
    }
}