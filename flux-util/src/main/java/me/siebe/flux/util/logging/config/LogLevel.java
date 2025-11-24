package me.siebe.flux.util.logging.config;

/**
 * Enumerates the supported logging levels ordered by severity.
 */
public enum LogLevel {
    TRACE(0, "TRACE", AnsiColor.BRIGHT_BLACK),
    DEBUG(1, "DEBUG", AnsiColor.CYAN),
    INFO(2, "INFO", AnsiColor.BRIGHT_WHITE),
    WARN(3, "WARN", AnsiColor.BRIGHT_YELLOW),
    ERROR(4, "ERROR", AnsiColor.BRIGHT_RED);

    private final int priority;
    private final String displayName;
    private final AnsiColor defaultColor;

    LogLevel(int priority, String displayName, AnsiColor defaultColor) {
        this.priority = priority;
        this.displayName = displayName;
        this.defaultColor = defaultColor;
    }

    /**
     * @return {@code true} when this level is as or more severe than the provided minimum level
     */
    public boolean isAtLeast(LogLevel minimum) {
        return priority >= minimum.priority;
    }

    /**
     * @return a human readable name for this level
     */
    public String displayName() {
        return displayName;
    }

    /**
     * @return the priority used to compare severity between levels
     */
    public int priority() {
        return priority;
    }

    /**
     * @return the default color suggested for this level
     */
    public AnsiColor defaultColor() {
        return defaultColor;
    }
}

