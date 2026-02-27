package me.siebe.flux.opengl.vertex;

import java.util.List;

public class BufferLayout {
    private final List<BufferElement> elements;
    private int stride;

    public BufferLayout(List<BufferElement> elements) {
        this.elements = elements;
        calculateOffsetsAndStride();
    }

    public BufferLayout(BufferElement... elements) {
        this(List.of(elements));
    }

    public void addElement(BufferElement element) {
        elements.add(element);
        calculateOffsetsAndStride();
    }

    public List<BufferElement> getElements() {
        return elements;
    }

    public int getStride() {
        return stride;
    }

    public int getComponentCount() {
        return elements.stream().mapToInt(BufferElement::getComponentSize).sum();
    }

    private void calculateOffsetsAndStride() {
        int offset = 0;
        this.stride = 0;
        for (BufferElement element : elements) {
            element.offset = offset;
            offset += element.byteSize;
            stride += element.byteSize;
        }
    }
}
