package me.siebe.flux.util.logging.output;

import me.siebe.flux.util.logging.config.LogLevel;
import me.siebe.flux.util.logging.config.LoggingConfiguration;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Writes log events to the system console.
 */
final class ConsoleLogOutput implements LogOutput {
    private final LogMessageFormatter formatter;

    ConsoleLogOutput(LogMessageFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    @Override
    public void write(LoggingConfiguration configuration, LogEvent event) {
        PrintStream stream = selectStream(event.level());
        String rendered = formatter.format(configuration, event);
        String payload = configuration.colorize(event.level(), rendered);
        stream.println(payload);
        if (event.throwable() != null) {
            event.throwable().printStackTrace(stream);
        }
    }

    private PrintStream selectStream(LogLevel level) {
        return level.isAtLeast(LogLevel.ERROR) ? System.err : System.out;
    }
}

