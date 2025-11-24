package me.siebe.flux.util.logging.config;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable description of a logging output target.
 * <p>
 * Instances are typically created by {@link LoggingConfigLoader} from the YAML
 * configuration, but can also be constructed programmatically via the {@link Builder}.
 */
public final class LoggingOutputConfiguration {
    /**
     * Default formatting pattern applied when none is specified.
     */
    public static final String DEFAULT_PATTERN = "[{time}] [{level}] [{category}] {message}";

    private final LoggingOutputType type;
    private final String pattern;
    private final Path file;

    private LoggingOutputConfiguration(Builder builder) {
        this.type = builder.type;
        this.pattern = builder.pattern;
        this.file = builder.file;
    }

    /**
     * @return the type of output that should be created
     */
    public LoggingOutputType type() {
        return type;
    }

    /**
     * @return the pattern applied to render log messages for this output
     */
    public String pattern() {
        return pattern;
    }

    /**
     * @return the file path associated with this output, when applicable
     */
    public Optional<Path> file() {
        return Optional.ofNullable(file);
    }

    /**
     * Creates a builder for a console output.
     *
     * @return the builder
     */
    public static Builder console() {
        return new Builder(LoggingOutputType.CONSOLE);
    }

    /**
     * Creates a builder for a file output.
     *
     * @return the builder
     */
    public static Builder fileOutput() {
        return new Builder(LoggingOutputType.FILE);
    }

    /**
     * Builder for {@link LoggingOutputConfiguration}.
     */
    public static final class Builder {
        private final LoggingOutputType type;
        private String pattern = DEFAULT_PATTERN;
        private Path file;

        private Builder(LoggingOutputType type) {
            this.type = Objects.requireNonNull(type, "type");
        }

        /**
         * Overrides the pattern used to render log messages.
         *
         * @param pattern the pattern to apply
         * @return this builder
         */
        public Builder pattern(String pattern) {
            String trimmed = Objects.requireNonNull(pattern, "pattern").trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Pattern must not be blank.");
            }
            this.pattern = trimmed;
            return this;
        }

        /**
         * Defines the file associated with the output.
         *
         * @param file the file path
         * @return this builder
         */
        public Builder file(Path file) {
            this.file = Objects.requireNonNull(file, "file");
            return this;
        }

        /**
         * Builds the immutable configuration.
         *
         * @return the configuration instance
         */
        public LoggingOutputConfiguration build() {
            if (type == LoggingOutputType.FILE && file == null) {
                throw new IllegalStateException("File outputs require an associated file path.");
            }
            return new LoggingOutputConfiguration(this);
        }
    }
}

