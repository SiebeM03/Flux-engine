package me.siebe.flux.util.logging;

import me.siebe.flux.util.logging.config.LogLevel;
import me.siebe.flux.util.string.MessageFormatter;

import java.util.Objects;

/**
 * Default {@link Logger} implementation that delegates to {@link LoggingManager}.
 */
final class DefaultLogger implements Logger {
    private final String name;
    private final String category;

    DefaultLogger(String name, String category) {
        this.name = Objects.requireNonNull(name, "name");
        this.category = Objects.requireNonNull(category, "category");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String category() {
        return category;
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return LoggingManager.isEnabled(category, level);
    }

    @Override
    public void log(LogLevel level, String message) {
        if (!isEnabled(level)) return;
        LoggingManager.publish(category, name, level, Objects.toString(message, "null"), null);
    }

    @Override
    public void log(LogLevel level, String message, Object... arguments) {
        if (!isEnabled(level)) return;
        String resolvedMessage = formatMessage(message, arguments);
        Throwable throwable = null;
        if (arguments[arguments.length - 1] instanceof Throwable) {
            throwable = (Throwable) arguments[arguments.length - 1];
        }
        LoggingManager.publish(category, name, level, resolvedMessage, throwable);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        if (!isEnabled(level)) return;
        LoggingManager.publish(category, name, level, Objects.toString(message, "null"), throwable);
    }

    /**
     * Formats a message using the provided template and arguments.
     *
     * @param template  the template to format the message with
     * @param arguments the arguments to format the message with, these replace the {} placeholders in the template
     * @return the formatted message
     */
    private String formatMessage(String template, Object... arguments) {
        if (template == null) return "null";
        if (arguments == null || arguments.length == 0) return template;

        return MessageFormatter.format(template, arguments);
    }
}

