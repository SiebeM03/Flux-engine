package me.siebe.flux.util.data;

abstract class PrimitiveBuffer {
    protected int size;

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
    }
}
