package me.siebe.flux.util.io;

import me.siebe.flux.util.string.StringUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a logical file format supported by the Flux engine.
 * <p>
 * A {@code FileFormat} encapsulates a human-friendly name together with the file extensions it recognises. Only the
 * name participates in equality checks to ensure that multiple registrations of the same logical format are prevented.
 * Additional extensions may still be provided so that different suffixes (for example {@code ".yaml"} or
 * {@code ".yml"}) map back to the same format instance.
 */
public final class FileFormat {

    /**
     * Built-in representation of the YAML file format. Additional extensions may be registered by creating a new
     * {@link FileFormat} instance with the same name.
     */
    public static final FileFormat YAML = new FileFormat("yaml", Set.of("yaml", "yml"));

    /**
     * Built-in representation of the JSON file format. Additional extensions may be registered by creating a new
     * {@link FileFormat} instance with the same name.
     */
    public static final FileFormat JSON = new FileFormat("json", Set.of("json"));

    private final String name;
    private final Set<String> extensions;

    /**
     * Creates a new file format descriptor.
     *
     * @param name       unique identifier for the format
     * @param extensions recognised file extensions without the leading dot (for example {@code "yaml"})
     * @throws IllegalArgumentException if no extensions are provided
     */
    public FileFormat(String name, Set<String> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            throw new IllegalArgumentException("At least one file extension must be provided.");
        }
        this.name = Objects.requireNonNull(name, "name");
        this.extensions = Set.copyOf(extensions);
    }

    /**
     * @return the unique identifier for this format. Two {@code FileFormat} instances with the same name are considered
     * equal.
     */
    public String getName() {
        return name;
    }

    /**
     * @return an immutable view of the file extensions recognised by this format
     */
    public Set<String> getExtensions() {
        return Collections.unmodifiableSet(extensions);
    }

    /**
     * Checks whether the provided file extension (without the leading dot) is recognised by this format.
     *
     * @param extension extension to check
     * @return {@code true} if the extension is recognised, {@code false} otherwise
     */
    public boolean matchesExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }
        return extensions.contains(extension.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileFormat other)) {
            return false;
        }
        return name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return StringUtils.toString(this);
    }
}


