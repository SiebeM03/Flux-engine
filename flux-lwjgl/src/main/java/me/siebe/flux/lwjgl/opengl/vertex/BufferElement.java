package me.siebe.flux.lwjgl.opengl.vertex;

import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;

public class BufferElement {
    String name;
    ShaderDataType type;
    int byteSize;
    int offset;
    boolean normalized;

    public BufferElement(String name, ShaderDataType type, boolean normalized) {
        this.name = name;
        this.type = type;
        this.byteSize = type.getTotalByteSize();
        this.normalized = normalized;
    }

    public int getComponentSize() {
        return type.getComponentCount();
    }
}
