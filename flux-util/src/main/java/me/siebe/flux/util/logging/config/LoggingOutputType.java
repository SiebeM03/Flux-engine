package me.siebe.flux.util.logging.config;

/**
 * Enumerates the supported logging output targets declared in the configuration file.
 */
public enum LoggingOutputType {
    /**
     * Writes log messages to the standard console streams.
     */
    CONSOLE,

    /**
     * Appends log messages to a file on disk.
     */
    FILE
}

