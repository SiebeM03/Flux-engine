package me.siebe.flux.util;

import me.siebe.flux.util.exceptions.Validator;

import java.util.function.Supplier;

public final class ValueUtils {
    private ValueUtils() {

    }

    public static <T> T valueWithFallback(T value, Supplier<T> fallback) {
        if (value == null) {
            return fallback.get();
        }
        return value;
    }

    public static <T extends Comparable<T>> T clampedValue(T value, T min, T max) {
        Validator.notNull(value, () -> "value");
        Validator.notNull(min, () -> "min");
        Validator.notNull(max, () -> "max");

        Validator.argument(min.compareTo(max) <= 0, () -> "min must be <= max");

        if (value.compareTo(min) < 0) return min;
        if (value.compareTo(max) > 0) return max;
        return value;
    }

    public static <T extends Comparable<T>> T bottomClamped(T value, T min) {
        Validator.notNull(value, () -> "value");
        Validator.notNull(min, () -> "min");

        if (value.compareTo(min) < 0) return min;
        return value;
    }

    public static <T extends Comparable<T>> T topClamped(T value, T max) {
        Validator.notNull(value, () -> "value");
        Validator.notNull(max, () -> "max");

        if (value.compareTo(max) > 0) return max;
        return value;
    }

}
