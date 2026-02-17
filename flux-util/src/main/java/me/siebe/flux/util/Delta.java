package me.siebe.flux.util;

public class Delta<T extends Number> {
    private T lastValue;
    private T currentValue;

    public Delta(T initialValue) {
        lastValue = initialValue;
        currentValue = initialValue;
    }

    public T getLastValue() {
        return lastValue;
    }

    public T getCurrentValue() {
        return currentValue;
    }

    public void updateValue(T newValue) {
        lastValue = currentValue;
        currentValue = newValue;
    }

    public double getDelta() {
        return currentValue.doubleValue() - lastValue.doubleValue();
    }
}
