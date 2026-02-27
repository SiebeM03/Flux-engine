package me.siebe.flux.opengl.shader;

public record ShaderAttribute(
        String name,
        int location,
        ShaderDataType type,
        int size
) {}
