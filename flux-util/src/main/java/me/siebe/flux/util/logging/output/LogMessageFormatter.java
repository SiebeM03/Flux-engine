package me.siebe.flux.util.logging.output;

import me.siebe.flux.util.logging.config.LoggingConfiguration;

import java.util.Map;
import java.util.Objects;

/**
 * Renders log events using a simple token replacement scheme.
 */
final class LogMessageFormatter {
    private final String pattern;

    LogMessageFormatter(String pattern) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
    }

    String format(LoggingConfiguration configuration, LogEvent event) {
        Map<String, String> replacements = Map.of(
                "{time}", configuration.formatTimestamp(event.timestamp()),
                "{level}", event.level().displayName(),
                "{category}", event.category(),
                "{logger}", event.loggerName(),
                "{message}", event.message()
        );

        String rendered = pattern;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            rendered = rendered.replace(entry.getKey(), entry.getValue());
        }
        return rendered;
    }
}

