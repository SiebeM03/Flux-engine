package me.siebe.flux.util.data.buffer;

import java.util.Arrays;

public class IntBuffer extends PrimitiveBuffer {
    private int[] data;

    public IntBuffer(int initialCapacity) {
        data = new int[initialCapacity];
    }

    public void add(int value) {
        if (size >= data.length) {
            grow();
        }
        data[size++] = value;
    }

    private void grow() {
        int[] newData = new int[data.length * 2];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }

    public int[] toArray() {
        return Arrays.copyOf(data, size);
    }
}
