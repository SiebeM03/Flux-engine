package me.siebe.flux.util.logging;

import me.siebe.flux.util.logging.config.AnsiColor;
import me.siebe.flux.util.logging.config.LogLevel;
import me.siebe.flux.util.logging.config.LoggingConfigLoader;
import me.siebe.flux.util.logging.config.LoggingConfiguration;
import me.siebe.flux.util.logging.output.LogEvent;
import me.siebe.flux.util.logging.output.LogOutput;
import me.siebe.flux.util.logging.output.LoggingOutputFactory;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Central orchestrator that stores the active logging configuration and handles message emission.
 */
public final class LoggingManager {
    private static final AtomicReference<ActiveLoggingState> STATE = new AtomicReference<>(initialState());
    private static final ConcurrentMap<String, String> NORMALIZED_CATEGORIES = new ConcurrentHashMap<>();

    private LoggingManager() {
    }

    /**
     * Attempts to load the initial logging configuration from the active game's resources.
     * <p>
     * Delegates to {@link LoggingConfigLoader} to resolve a configuration from the application's resources and falls
     * back to the default configuration if none could be loaded.
     *
     * @return the resolved configuration, never {@code null}
     */
    private static ActiveLoggingState initialState() {
        LoggingConfiguration configuration = LoggingConfigLoader.loadInitialConfiguration()
                .orElseGet(LoggingConfiguration::defaultConfiguration);
        return createState(configuration);
    }

    /**
     * Replaces the active logging configuration.
     *
     * @param configuration the configuration to use
     */
    public static void configure(LoggingConfiguration configuration) {
        STATE.set(createState(Objects.requireNonNull(configuration, "configuration")));
    }

    /**
     * Applies modifications to the current configuration using a builder.
     *
     * @param modifier a callback that customises the builder
     */
    public static void configure(Consumer<LoggingConfiguration.Builder> modifier) {
        Objects.requireNonNull(modifier, "modifier");
        LoggingConfiguration current = configuration();
        LoggingConfiguration.Builder builder = current.toBuilder();
        modifier.accept(builder);
        configure(builder.build());
    }

    /**
     * Overrides the minimum level for a specific category while keeping other configuration values intact.
     *
     * @param category the category to configure
     * @param level    the minimum level for the category
     */
    public static void setCategoryLevel(String category, LogLevel level) {
        configure(builder -> builder.categoryLevel(category, level));
    }

    /**
     * Removes a category specific level override, causing the category to inherit the default minimum level.
     *
     * @param category the category to reset
     */
    public static void clearCategoryLevel(String category) {
        configure(builder -> builder.removeCategoryLevel(category));
    }

    /**
     * Updates the color that should be used to render a specific log level.
     *
     * @param level the affected level
     * @param color the color to use
     */
    public static void setLevelColor(LogLevel level, AnsiColor color) {
        configure(builder -> builder.levelColor(level, color));
    }

    /**
     * Enables or disables ANSI coloring globally.
     *
     * @param enabled {@code true} when colors should be used
     */
    public static void setColorEnabled(boolean enabled) {
        configure(builder -> builder.colorEnabled(enabled));
    }

    /**
     * @return the active configuration
     */
    public static LoggingConfiguration configuration() {
        return STATE.get().configuration();
    }

    public static boolean isEnabled(String category, LogLevel level) {
        return configuration().isEnabled(category, level);
    }

    public static void publish(String category, String loggerName, LogLevel level, String message, Throwable throwable) {
        ActiveLoggingState state = STATE.get();
        LoggingConfiguration configuration = state.configuration();
        String normalizedCategory = normalizeCategory(category);
        if (!configuration.isEnabled(normalizedCategory, level)) {
            return;
        }

        LogEvent event = new LogEvent(
                Instant.now(),
                normalizedCategory,
                loggerName,
                level,
                Objects.toString(message, "null"),
                throwable
        );
        for (LogOutput output : state.outputs()) {
            output.write(configuration, event);
        }
    }

    public static String normalizeCategory(String category) {
        return NORMALIZED_CATEGORIES.computeIfAbsent(
                category == null ? "" : category,
                key -> key.isBlank() ? LoggingConfiguration.DEFAULT_CATEGORY : key.toLowerCase(Locale.ROOT)
        );
    }

    private static ActiveLoggingState createState(LoggingConfiguration configuration) {
        return new ActiveLoggingState(configuration, LoggingOutputFactory.createOutputs(configuration));
    }

    private record ActiveLoggingState(LoggingConfiguration configuration, List<LogOutput> outputs) {
    }
}

