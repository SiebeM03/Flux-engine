package me.siebe.flux.util.logging.config;

/**
 * Represents ANSI escape codes that can be used to colorize console output.
 * <p>
 * The codes declared here are a curated subset that is suitable for the logging system.
 * The {@link #apply(String)} helper can be used to wrap a message with the color and a reset suffix.
 */
public enum AnsiColor {
    /**
     * Resets the console color back to the terminal default.
     */
    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),
    BRIGHT_BLACK("\u001B[90m"),
    BRIGHT_RED("\u001B[91m"),
    BRIGHT_GREEN("\u001B[92m"),
    BRIGHT_YELLOW("\u001B[93m"),
    BRIGHT_BLUE("\u001B[94m"),
    BRIGHT_MAGENTA("\u001B[95m"),
    BRIGHT_CYAN("\u001B[96m"),
    BRIGHT_WHITE("\u001B[97m");

    private final String code;

    AnsiColor(String code) {
        this.code = code;
    }

    /**
     * @return the raw ANSI escape sequence for the color
     */
    public String code() {
        return code;
    }

    /**
     * Wraps the provided message in this color and appends a reset code.
     *
     * @param message the message to colorize
     * @return the colorized message
     */
    public String apply(String message) {
        return code + message + RESET.code;
    }
}


