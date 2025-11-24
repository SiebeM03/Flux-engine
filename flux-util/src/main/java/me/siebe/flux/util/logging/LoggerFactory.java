package me.siebe.flux.util.logging;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory for {@link Logger} instances resembling the SLF4J usage patterns.
 */
public final class LoggerFactory {
    private static final ConcurrentMap<String, Logger> CACHE = new ConcurrentHashMap<>();
    private static final String GENERAL_CATEGORY = LoggingManager.normalizeCategory("general");

    private LoggerFactory() {
    }

    /**
     * Creates or retrieves a logger associated with the provided class using the default category.
     *
     * @param type the class for which logging is requested
     * @return a logger instance
     */
    public static Logger getLogger(Class<?> type) {
        return getLogger(type, GENERAL_CATEGORY);
    }

    /**
     * Creates or retrieves a logger associated with the provided class under a named category.
     *
     * @param type     the class for which logging is requested
     * @param category the category to publish under
     * @return a logger instance
     */
    public static Logger getLogger(Class<?> type, String category) {
        Objects.requireNonNull(type, "type");
        return getLogger(type.getName(), category);
    }

    /**
     * Creates or retrieves a logger associated with the provided name and category.
     *
     * @param name     the logger name (typically a class name)
     * @param category the category to publish under
     * @return a logger instance
     */
    public static Logger getLogger(String name, String category) {
        Objects.requireNonNull(name, "name");
        String normalizedCategory = LoggingManager.normalizeCategory(category);
        String cacheKey = normalizedCategory + ":" + name;
        return CACHE.computeIfAbsent(cacheKey, ignored ->
                new DefaultLogger(name, normalizedCategory)
        );
    }
}
