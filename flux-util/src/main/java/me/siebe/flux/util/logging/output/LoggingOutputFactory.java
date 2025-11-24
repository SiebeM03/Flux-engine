package me.siebe.flux.util.logging.output;

import me.siebe.flux.util.logging.config.LoggingConfiguration;
import me.siebe.flux.util.logging.config.LoggingOutputConfiguration;
import me.siebe.flux.util.logging.config.LoggingOutputType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Creates {@link LogOutput} instances based on {@link LoggingOutputConfiguration} definitions.
 */
public final class LoggingOutputFactory {
    private LoggingOutputFactory() {
    }

    /**
     * Builds the outputs configured for the supplied configuration.
     *
     * @param configuration the logging configuration to inspect
     * @return the outputs that should receive log events
     */
    public static List<LogOutput> createOutputs(LoggingConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        List<LoggingOutputConfiguration> outputConfigurations = configuration.outputs();
        List<LogOutput> outputs = new ArrayList<>();
        if (outputConfigurations.isEmpty()) {
            outputs.add(new ConsoleLogOutput(new LogMessageFormatter(LoggingOutputConfiguration.DEFAULT_PATTERN)));
            return List.copyOf(outputs);
        }

        for (LoggingOutputConfiguration outputConfiguration : outputConfigurations) {
            outputs.add(createOutput(outputConfiguration));
        }
        return List.copyOf(outputs);
    }

    private static LogOutput createOutput(LoggingOutputConfiguration configuration) {
        LogMessageFormatter formatter = new LogMessageFormatter(configuration.pattern());
        LoggingOutputType type = configuration.type();
        if (type == LoggingOutputType.CONSOLE) {
            return new ConsoleLogOutput(formatter);
        }
        if (type == LoggingOutputType.FILE) {
            return new FileLogOutput(configuration.file().orElseThrow(), formatter);
        }
        throw new IllegalStateException("Unsupported logging output type: " + type);
    }
}

