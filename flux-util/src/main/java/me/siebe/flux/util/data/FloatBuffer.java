package me.siebe.flux.util.data;

import java.util.Arrays;

public class FloatBuffer extends PrimitiveBuffer {
    private float[] data;

    public FloatBuffer(int initialCapacity) {
        data = new float[initialCapacity];
    }

    public void add(float value) {
        if (size >= data.length) {
            grow();
        }
        data[size++] = value;
    }

    private void grow() {
        float[] newData = new float[data.length * 2];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }

    public float[] toArray() {
        return Arrays.copyOf(data, size);
    }
}
