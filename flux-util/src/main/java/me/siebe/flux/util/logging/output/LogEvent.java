package me.siebe.flux.util.logging.output;

import me.siebe.flux.util.logging.config.LogLevel;

import java.time.Instant;
import java.util.Objects;

/**
 * Carries contextual information about a single log publication.
 */
public final class LogEvent {
    private final Instant timestamp;
    private final String category;
    private final String loggerName;
    private final LogLevel level;
    private final String message;
    private final Throwable throwable;

    public LogEvent(Instant timestamp,
                    String category,
                    String loggerName,
                    LogLevel level,
                    String message,
                    Throwable throwable) {
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.category = Objects.requireNonNull(category, "category");
        this.loggerName = Objects.requireNonNull(loggerName, "loggerName");
        this.level = Objects.requireNonNull(level, "level");
        this.message = Objects.requireNonNull(message, "message");
        this.throwable = throwable;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public String category() {
        return category;
    }

    public String loggerName() {
        return loggerName;
    }

    public LogLevel level() {
        return level;
    }

    public String message() {
        return message;
    }

    public Throwable throwable() {
        return throwable;
    }
}

