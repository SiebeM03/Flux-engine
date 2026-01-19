package me.siebe.flux.util.logging.config;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Immutable configuration for the logging subsystem.
 * <p>
 * Instances can be created via the {@link Builder}, and a default configuration is provided via
 * {@link #defaultConfiguration()}.
 */
public final class LoggingConfiguration {
    private static final DateTimeFormatter DEFAULT_TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    public static final String DEFAULT_CATEGORY = "general";

    private final LogLevel defaultLevel;
    private final Map<String, LogLevel> categoryLevels;
    private final Map<LogLevel, AnsiColor> levelColors;
    private final boolean colorEnabled;
    private final boolean startupBannerEnabled;
    private final boolean startupBannerLogSystemInfo;
    private final DateTimeFormatter timestampFormatter;
    private final List<LoggingOutputConfiguration> outputs;

    private LoggingConfiguration(Builder builder) {
        this.defaultLevel = builder.defaultLevel;
        this.categoryLevels = Collections.unmodifiableMap(new ConcurrentHashMap<>(builder.categoryLevels));
        this.levelColors = Collections.unmodifiableMap(new EnumMap<>(builder.levelColors));
        this.colorEnabled = builder.colorEnabled;
        this.startupBannerEnabled = builder.startupBannerEnabled;
        this.startupBannerLogSystemInfo = builder.startupBannerLogSystemInfo;
        this.timestampFormatter = builder.timestampFormatter;
        this.outputs = Collections.unmodifiableList(new ArrayList<>(builder.outputs));
    }

    /**
     * Creates a new builder populated with default values.
     *
     * @return a builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return the default logging configuration
     */
    public static LoggingConfiguration defaultConfiguration() {
        return builder().build();
    }

    /**
     * Generates a new builder populated with the current values of this configuration.
     *
     * @return a builder whose defaults match this configuration
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Resolves the minimum level for a given category, falling back to the default level if unspecified.
     *
     * @param category the category to look up
     * @return the resolved minimum level
     */
    public LogLevel minimumLevel(String category) {
        String normalized = normalizeCategory(category);
        return categoryLevels.getOrDefault(normalized, defaultLevel);
    }

    /**
     * Determines whether the provided level is enabled for the given category.
     *
     * @param category the category under evaluation
     * @param level    the level to check
     * @return {@code true} when the level is allowed
     */
    public boolean isEnabled(String category, LogLevel level) {
        return level.isAtLeast(minimumLevel(category));
    }

    /**
     * Formats the supplied instant using the configured timestamp formatter.
     *
     * @param instant the instant to format
     * @return the formatted timestamp
     */
    public String formatTimestamp(Instant instant) {
        return timestampFormatter.format(instant.atZone(ZoneId.systemDefault()));
    }

    /**
     * Applies ANSI coloring to a message according to the configured color scheme.
     *
     * @param level   the level being rendered
     * @param message the message to colorize
     * @return the message, potentially colorized
     */
    public String colorize(LogLevel level, String message) {
        if (!colorEnabled) {
            return message;
        }
        AnsiColor color = levelColors.getOrDefault(level, level.defaultColor());
        return color.apply(message);
    }

    /**
     * @return {@code true} when ANSI colors are enabled for logging output
     */
    public boolean isColorEnabled() {
        return colorEnabled;
    }

    /**
     * @return {@code true} when the startup banner is enabled
     */
    public boolean isStartupBannerEnabled() {
        return startupBannerEnabled;
    }

    /**
     * @return {@code true} when the system information should be logged on startup
     */
    public boolean isStartupBannerLogSystemInfo() {
        return startupBannerLogSystemInfo;
    }

    /**
     * @return an unmodifiable view of the category to level mappings
     */
    public Map<String, LogLevel> categoryLevels() {
        return categoryLevels;
    }

    /**
     * @return an unmodifiable view of the level color overrides
     */
    public Map<LogLevel, AnsiColor> levelColors() {
        return levelColors;
    }

    /**
     * @return the default minimum level used when no category override is present
     */
    public LogLevel defaultLevel() {
        return defaultLevel;
    }

    /**
     * @return an immutable list of output configurations that should receive log events
     */
    public List<LoggingOutputConfiguration> outputs() {
        return outputs;
    }

    private static String normalizeCategory(String category) {
        return Optional.ofNullable(category)
                .map(value -> value.isBlank() ? DEFAULT_CATEGORY : value)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .orElse(DEFAULT_CATEGORY);
    }


    /**
     * Builder for {@link LoggingConfiguration}.
     */
    public static final class Builder {
        private LogLevel defaultLevel = LogLevel.INFO;
        private final Map<String, LogLevel> categoryLevels = new ConcurrentHashMap<>();
        private final Map<LogLevel, AnsiColor> levelColors = new EnumMap<>(LogLevel.class);
        private boolean colorEnabled = true;
        private boolean startupBannerEnabled = true;
        private boolean startupBannerLogSystemInfo = true;
        private DateTimeFormatter timestampFormatter = DEFAULT_TIMESTAMP_FORMATTER;
        private final List<LoggingOutputConfiguration> outputs = new ArrayList<>();

        private Builder() {
            for (LogLevel level : LogLevel.values()) {
                levelColors.put(level, level.defaultColor());
            }
            outputs.add(LoggingOutputConfiguration.console().build());
        }

        private Builder(LoggingConfiguration configuration) {
            this.defaultLevel = configuration.defaultLevel;
            this.categoryLevels.putAll(configuration.categoryLevels);
            this.levelColors.putAll(configuration.levelColors);
            this.colorEnabled = configuration.colorEnabled;
            this.startupBannerEnabled = configuration.startupBannerEnabled;
            this.startupBannerLogSystemInfo = configuration.startupBannerLogSystemInfo;
            this.timestampFormatter = configuration.timestampFormatter;
            this.outputs.addAll(configuration.outputs);
        }

        /**
         * Sets the default logging level used when a category does not have a dedicated override.
         *
         * @param level the default level
         * @return this builder
         */
        public Builder defaultLevel(LogLevel level) {
            this.defaultLevel = Objects.requireNonNull(level, "level");
            return this;
        }

        /**
         * Defines the minimum level for a specific category.
         *
         * @param category the category identifier
         * @param level    the minimum level for the category
         * @return this builder
         */
        public Builder categoryLevel(String category, LogLevel level) {
            String normalized = normalizeCategory(category);
            this.categoryLevels.put(normalized, Objects.requireNonNull(level, "level"));
            return this;
        }

        /**
         * Removes the configured minimum level for the given category, causing it to use the default level.
         *
         * @param category the category identifier
         * @return this builder
         */
        public Builder removeCategoryLevel(String category) {
            String normalized = normalizeCategory(category);
            this.categoryLevels.remove(normalized);
            return this;
        }

        /**
         * Overrides the color used for the supplied log level.
         *
         * @param level the affected level
         * @param color the color to use
         * @return this builder
         */
        public Builder levelColor(LogLevel level, AnsiColor color) {
            this.levelColors.put(Objects.requireNonNull(level, "level"), Objects.requireNonNull(color, "color"));
            return this;
        }

        /**
         * Enables or disables ANSI coloring for log messages.
         *
         * @param enabled {@code true} to enable colors
         * @return this builder
         */
        public Builder colorEnabled(boolean enabled) {
            this.colorEnabled = enabled;
            return this;
        }

        /**
         * Enables or disables logging of the startup banner
         *
         * @param enabled {@code true} to log the startup banner
         * @return this builder
         */
        public Builder startupBannerEnabled(boolean enabled) {
            this.startupBannerEnabled = enabled;
            return this;
        }

        /**
         * Enables or disables logging of the system information at startup.
         *
         * @param enabled {@code true} to log system information
         * @return this builder
         */
        public Builder startupBannerLogSystemInfo(boolean enabled) {
            this.startupBannerLogSystemInfo = enabled;
            return this;
        }

        /**
         * Overrides the formatter used to render timestamps.
         *
         * @param formatter the formatter to use
         * @return this builder
         */
        public Builder timestampFormatter(DateTimeFormatter formatter) {
            this.timestampFormatter = Objects.requireNonNull(formatter, "formatter");
            return this;
        }

        /**
         * Removes all currently configured outputs.
         *
         * @return this builder
         */
        public Builder clearOutputs() {
            this.outputs.clear();
            return this;
        }

        /**
         * Adds an output configuration that should receive log events.
         *
         * @param output the output configuration to add
         * @return this builder
         */
        public Builder addOutput(LoggingOutputConfiguration output) {
            this.outputs.add(Objects.requireNonNull(output, "output"));
            return this;
        }

        /**
         * Builds an immutable {@link LoggingConfiguration} instance.
         *
         * @return the new configuration
         */
        public LoggingConfiguration build() {
            return new LoggingConfiguration(this);
        }
    }
}


