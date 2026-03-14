package me.siebe.flux.util.data.buffer;


public abstract class PrimitiveBuffer {
    protected int size;

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
    }
}