package me.siebe.flux.lwjgl.opengl.shader;

public record ShaderAttribute(
        String name,
        int location,
        ShaderDataType type,
        int size
) {}
