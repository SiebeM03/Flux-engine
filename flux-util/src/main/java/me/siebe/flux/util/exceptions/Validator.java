package me.siebe.flux.util.exceptions;

import me.siebe.flux.util.io.FileIOException;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public final class Validator {
    private static final Logger logger = LoggerFactory.getLogger(Validator.class);

    private Validator() {}


    // =================================================================================================================
    // Null handling
    // =================================================================================================================
    public static <T> T notNull(T value) throws NullPointerException {
        return notNull(value, () -> "value");
    }

    public static <T> T notNull(T value, Supplier<String> nameSupplier) throws NullPointerException {
        if (value == null) {
            fail(nameSupplier.get() + " must not be null", NullPointerException.class);
        }
        return value;
    }


    // =================================================================================================================
    // Number comparison
    // =================================================================================================================
    public static <T extends Comparable<T>> void assertGreaterThan(T value, T threshold, String name) throws IllegalArgumentException {
        argument(value.compareTo(threshold) > 0, () -> name + " must be > " + threshold + ", was " + value);
    }

    public static <T extends Comparable<T>> void assertGreaterThanOrEqual(T value, T threshold, String name) throws IllegalArgumentException {
        argument(value.compareTo(threshold) >= 0, () -> name + " must be >= " + threshold + ", was " + value);
    }

    public static <T extends Comparable<T>> void assertLessThan(T value, T threshold, String name) throws IllegalArgumentException {
        argument(value.compareTo(threshold) < 0, () -> name + " must be < " + threshold + ", was " + value);
    }

    public static <T extends Comparable<T>> void assertLessThanOrEqual(T value, T threshold, String name) throws IllegalArgumentException {
        argument(value.compareTo(threshold) <= 0, () -> name + " must be <= " + threshold + ", was " + value);
    }


    // =================================================================================================================
    // File checks
    // =================================================================================================================
    public static void assertFileExists(Path path) {
        if (!Files.exists(path)) {
            fail(path + " does not exist", FileIOException.class);
        }
    }


    // =================================================================================================================
    // Generic handling
    // =================================================================================================================
    public static void state(boolean condition, Supplier<String> messageSupplier) throws IllegalStateException {
        if (!condition) {
            fail(messageSupplier.get(), IllegalStateException.class);
        }
    }

    public static void argument(boolean condition, Supplier<String> messageSupplier) throws IllegalArgumentException {
        if (!condition) {
            fail(messageSupplier.get(), IllegalArgumentException.class);
        }
    }


    // =================================================================================================================
    // Private utility methods
    // =================================================================================================================
    private static void fail(String message, Class<? extends RuntimeException> exceptionType) {
        logger.error("Validation failed: {}", message);

        throw createException(exceptionType, message);
    }

    private static RuntimeException createException(Class<? extends RuntimeException> type, String message) {
        try {
            return type.getConstructor(String.class).newInstance(message);
        } catch (Exception e) {
            return new RuntimeException(message);
        }
    }
}
