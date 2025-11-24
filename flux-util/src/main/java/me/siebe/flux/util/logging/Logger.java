package me.siebe.flux.util.logging;


import me.siebe.flux.util.logging.config.LogLevel;

/**
 * Entry point used by engine components to emit log messages.
 * <p>
 * The interface mirrors a subset of the SLF4J API to make the adoption straightforward.
 */
public interface Logger {

    /**
     * @return the logical name associated with this logger (typically a class name)
     */
    String name();

    /**
     * @return the category that this logger publishes under
     */
    String category();

    /**
     * Tests whether the supplied level is currently enabled for this logger.
     *
     * @param level the level being tested
     * @return {@code true} when logging is allowed
     */
    boolean isEnabled(LogLevel level);

    default boolean isTraceEnabled() {
        return isEnabled(LogLevel.TRACE);
    }

    default boolean isDebugEnabled() {
        return isEnabled(LogLevel.DEBUG);
    }

    default boolean isInfoEnabled() {
        return isEnabled(LogLevel.INFO);
    }

    default boolean isWarnEnabled() {
        return isEnabled(LogLevel.WARN);
    }

    default boolean isErrorEnabled() {
        return isEnabled(LogLevel.ERROR);
    }

    void log(LogLevel level, String message);

    void log(LogLevel level, String message, Object... arguments);

    void log(LogLevel level, String message, Throwable throwable);

    default void trace(String message) {
        log(LogLevel.TRACE, message);
    }

    default void trace(String message, Object... arguments) {
        log(LogLevel.TRACE, message, arguments);
    }

    default void trace(String message, Throwable throwable) {
        log(LogLevel.TRACE, message, throwable);
    }

    default void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    default void debug(String message, Object... arguments) {
        log(LogLevel.DEBUG, message, arguments);
    }

    default void debug(String message, Throwable throwable) {
        log(LogLevel.DEBUG, message, throwable);
    }

    default void info(String message) {
        log(LogLevel.INFO, message);
    }

    default void info(String message, Object... arguments) {
        log(LogLevel.INFO, message, arguments);
    }

    default void info(String message, Throwable throwable) {
        log(LogLevel.INFO, message, throwable);
    }

    default void warn(String message) {
        log(LogLevel.WARN, message);
    }

    default void warn(String message, Object... arguments) {
        log(LogLevel.WARN, message, arguments);
    }

    default void warn(String message, Throwable throwable) {
        log(LogLevel.WARN, message, throwable);
    }

    default void error(String message) {
        log(LogLevel.ERROR, message);
    }

    default void error(String message, Object... arguments) {
        log(LogLevel.ERROR, message, arguments);
    }

    default void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }
}
