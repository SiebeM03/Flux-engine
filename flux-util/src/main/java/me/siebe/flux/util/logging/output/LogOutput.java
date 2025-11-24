package me.siebe.flux.util.logging.output;

import me.siebe.flux.util.logging.config.LoggingConfiguration;

/**
 * Contract implemented by sinks that emit log events to their destination.
 */
public interface LogOutput {

    /**
     * Writes the supplied event to the output.
     *
     * @param configuration the active logging configuration
     * @param event         the event to write
     */
    void write(LoggingConfiguration configuration, LogEvent event);
}

