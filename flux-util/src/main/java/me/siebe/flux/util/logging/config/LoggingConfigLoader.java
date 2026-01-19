package me.siebe.flux.util.logging.config;

import me.siebe.flux.util.io.FileIOException;
import me.siebe.flux.util.io.FileIOManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Resolves {@link LoggingConfiguration} instances from YAML resources located
 * on the classpath.
 * <p>
 * The loader honours multiple candidate resource paths and uses the engine's
 * {@link FileIOManager} so that any
 * registered {@code FileHandler} implementations can participate in parsing the
 * configuration.
 */
public final class LoggingConfigLoader {
    private static final List<String> CONFIG_RESOURCE_CANDIDATES = List.of(
            "flux-logging.yml",
            "config/flux-logging.yml");

    private LoggingConfigLoader() {
    }

    /**
     * Attempts to resolve a configuration from the runtime classpath.
     *
     * @return optional configuration when a resource could be located and parsed
     * successfully
     */
    public static Optional<LoggingConfiguration> loadInitialConfiguration() {
        ClassLoader classLoader = resolveClassLoader();
        for (String resourcePath : CONFIG_RESOURCE_CANDIDATES) {
            URL resource = classLoader.getResource(resourcePath);
            if (resource == null) {
                continue;
            }
            try {
                LoggingConfigDocument document = readConfiguration(resource, resourcePath);
                return Optional.of(document.toConfiguration(resourcePath));
            } catch (FileIOException exception) {
                System.err.println(exception.getMessage());
            }
        }
        return Optional.empty();
    }

    private static LoggingConfigDocument readConfiguration(URL resource, String resourcePath) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("flux-logging-", ".yml");
            try (InputStream inputStream = resource.openStream()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return FileIOManager.get().read(tempFile, LoggingConfigDocument.class);
        } catch (IOException exception) {
            throw new FileIOException("Failed to read logging configuration from resource '" + resourcePath + "'.",
                    exception);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static String requireNonEmpty(String value, String resourcePath, String propertyName) {
        if (value == null || value.trim().isEmpty()) {
            throw new FileIOException(
                    "Property '" + propertyName + "' in '" + resourcePath + "' must be a non-empty string.");
        }
        return value.trim();
    }

    private static LogLevel parseLevel(String levelName, String resourcePath, String propertyName) {
        try {
            return LogLevel.valueOf(levelName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FileIOException("Unknown log level '" + levelName + "' in property '" + propertyName + "' of '"
                    + resourcePath + "'.", exception);
        }
    }

    private static AnsiColor parseColor(String colorName, String resourcePath, String propertyName) {
        try {
            return AnsiColor.valueOf(colorName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FileIOException("Unknown ANSI color '" + colorName + "' in property '" + propertyName + "' of '"
                    + resourcePath + "'.", exception);
        }
    }
    private static LoggingOutputType parseOutputType(String typeName, String resourcePath) {
        try {
            return LoggingOutputType.valueOf(typeName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FileIOException("Unknown output type '" + typeName + "' declared in '" + resourcePath + "'.",
                    exception);
        }
    }

    private static ClassLoader resolveClassLoader() {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        return contextLoader != null ? contextLoader : LoggingConfigLoader.class.getClassLoader();
    }

    /**
     * SnakeYAML-populated builder that materialises a {@link LoggingConfiguration}
     * once validation succeeds.
     */
    public static final class LoggingConfigDocument {
        private LogLevel defaultLevel;
        private Boolean colorEnabled;
        private String timestampFormat;
        private Map<String, String> categories = new LinkedHashMap<>();
        private Map<String, String> startupBannerConfig = new LinkedHashMap<>();
        private Map<String, String> levelColors = new LinkedHashMap<>();
        private List<LoggingOutputDocument> outputs = List.of();

        public LogLevel getDefaultLevel() {
            return defaultLevel;
        }

        public void setDefaultLevel(LogLevel defaultLevel) {
            this.defaultLevel = defaultLevel;
        }

        public void setColorEnabled(Boolean colorEnabled) {
            this.colorEnabled = colorEnabled;
        }

        public void setTimestampFormat(String timestampFormat) {
            this.timestampFormat = timestampFormat;
        }

        public void setCategories(Map<String, String> categories) {
            this.categories = categories == null ? new LinkedHashMap<>() : new LinkedHashMap<>(categories);
        }

        public void setStartupBannerConfig(Map<String, String> startupBannerConfig) {
            this.startupBannerConfig = startupBannerConfig == null ? new LinkedHashMap<>() : new LinkedHashMap<>(startupBannerConfig);
        }

        public void setLevelColors(Map<String, String> levelColors) {
            this.levelColors = levelColors == null ? new LinkedHashMap<>() : new LinkedHashMap<>(levelColors);
        }

        public void setOutputs(List<LoggingOutputDocument> outputs) {
            this.outputs = outputs == null ? List.of() : List.copyOf(outputs);
        }

        /**
         * Builds a validated {@link LoggingConfiguration} instance using the collected
         * values.
         *
         * @param resourcePath the source resource, used for error reporting
         * @return the resolved configuration
         */
        LoggingConfiguration toConfiguration(String resourcePath) {
            LoggingConfiguration.Builder builder = LoggingConfiguration.builder();

            if (defaultLevel != null) {
                builder.defaultLevel(defaultLevel);
            }

            if (colorEnabled != null) {
                builder.colorEnabled(colorEnabled);
            }

            if (startupBannerConfig != null) {
                if (startupBannerConfig.containsKey("enabled")) {
                    builder.startupBannerEnabled(Boolean.parseBoolean(startupBannerConfig.get("enabled")));
                }
                if (startupBannerConfig.containsKey("log-system-info")) {
                    builder.startupBannerLogSystemInfo(Boolean.parseBoolean(startupBannerConfig.get("log-system-info")));
                }
            }

            if (timestampFormat != null) {
                String trimmed = timestampFormat.trim();
                if (trimmed.isEmpty()) {
                    throw new FileIOException(
                            "Property 'timestampFormat' in '" + resourcePath + "' must be a non-empty string.");
                }
                try {
                    builder.timestampFormatter(DateTimeFormatter.ofPattern(trimmed));
                } catch (IllegalArgumentException exception) {
                    throw new FileIOException("Invalid timestamp pattern '" + trimmed + "' in '" + resourcePath + "'.",
                            exception);
                }
            }

            for (Map.Entry<String, String> entry : categories.entrySet()) {
                String category = requireNonEmpty(entry.getKey(), resourcePath, "categories key");
                String levelName = requireNonEmpty(entry.getValue(), resourcePath, "categories[" + category + "]");
                builder.categoryLevel(category, parseLevel(levelName, resourcePath, "categories[" + category + "]"));
            }

            for (Map.Entry<String, String> entry : levelColors.entrySet()) {
                String levelName = requireNonEmpty(entry.getKey(), resourcePath, "levelColors key");
                String colorName = requireNonEmpty(entry.getValue(), resourcePath, "levelColors[" + levelName + "]");
                LogLevel level = parseLevel(levelName, resourcePath, "levelColors");
                builder.levelColor(level, parseColor(colorName, resourcePath, "levelColors[" + level + "]"));
            }

            if (!outputs.isEmpty()) {
                builder.clearOutputs();
                for (LoggingOutputDocument output : outputs) {
                    builder.addOutput(output.toOutputConfiguration(resourcePath));
                }
            }

            return builder.build();
        }

        /**
         * Intermediate representation of the outputs defined in the configuration.
         */
        public static final class LoggingOutputDocument {
            private String type;
            private String pattern;
            private String file;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getPattern() {
                return pattern;
            }

            public void setPattern(String pattern) {
                this.pattern = pattern;
            }

            public String getFile() {
                return file;
            }

            public void setFile(String file) {
                this.file = file;
            }

            LoggingOutputConfiguration toOutputConfiguration(String resourcePath) {
                String typeName = requireNonEmpty(type, resourcePath, "outputs[].type");
                LoggingOutputType type = parseOutputType(typeName, resourcePath);

                LoggingOutputConfiguration.Builder builder = type == LoggingOutputType.FILE
                        ? LoggingOutputConfiguration.fileOutput()
                        : LoggingOutputConfiguration.console();

                if (pattern != null) {
                    String trimmedPattern = pattern.trim();
                    if (trimmedPattern.isEmpty()) {
                        throw new FileIOException("Property 'outputs[].pattern' in '" + resourcePath
                                + "' must be a non-empty string when specified.");
                    }
                    builder.pattern(trimmedPattern);
                }

                if (type == LoggingOutputType.FILE) {
                    String fileName = requireNonEmpty(file, resourcePath, "outputs[].file");
                    try {
                        builder.file(Paths.get(fileName));
                    } catch (Exception exception) {
                        throw new FileIOException(
                                "Property 'outputs[].file' in '" + resourcePath + "' is not a valid file path.",
                                exception);
                    }
                }

                return builder.build();
            }
        }
    }
}
