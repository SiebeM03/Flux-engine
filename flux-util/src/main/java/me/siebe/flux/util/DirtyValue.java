package me.siebe.flux.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DirtyValue<T> {
    private T value;
    private boolean dirty;
    private final Supplier<T> calculator;
    private final Consumer<T> updater;

    public DirtyValue(Supplier<T> calculator) {
        this.calculator = calculator;
        this.updater = null;
        this.dirty = true;
        this.value = null;
    }

    public DirtyValue(T initialValue, Consumer<T> updater) {
        this.calculator = null;
        this.updater = updater;
        this.value = initialValue;
        this.dirty = true;
    }

    public void markDirty() {
        dirty = true;
    }

    public void update() {
        if (updater != null) {
            updater.accept(value);
        } else {
            value = calculator.get();
        }
        dirty = false;
    }

    public T get() {
        if (dirty) {
            update();
        }
        return value;
    }

    public void set(T value) {
        this.value = value;
        this.dirty = false;
    }
}
