package me.siebe.flux.util.memory;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.HashMap;
import java.util.Map;

public class NativeTracker {
    private static final Logger logger = LoggerFactory.getLogger(NativeTracker.class, LoggingCategories.MEMORY);

    private static final Map<String, Integer> leaks = new HashMap<>();
    private static boolean enabled = true;

    private NativeTracker() {}

    public static void enable(boolean value) {
        enabled = value;
    }

    public static void alloc(String tag) {
        if (!enabled) return;
        leaks.merge(tag, 1, Integer::sum);
    }

    public static void free(String tag) {
        if (!enabled) return;
        leaks.merge(tag, -1, Integer::sum);
    }

    public static void report() {
        if (!enabled) return;

        leaks.forEach((tag, count) -> {
            if (count != 0) {
                System.err.println("[LEAK] " + tag + ": " + count);
            }
        });
    }
}
