package me.siebe.flux.util.io;

import java.nio.file.Path;

/**
 * Abstraction for loading and storing domain objects using a specific {@link FileFormat}.
 */
public interface FileHandler {

    /**
     * @return the format handled by this implementation
     */
    FileFormat getFormat();

    /**
     * Reads content from the provided {@link Path} and converts it to the requested target type.
     *
     * @param path       source file path
     * @param targetType type to convert the file contents to
     * @param <T>        compile-time representation of {@code targetType}
     * @return deserialised instance of {@code targetType}
     * @throws FileIOException when the file cannot be read or converted
     */
    <T> T read(Path path, Class<T> targetType);

    /**
     * Parses the provided content string and converts it to the requested target type.
     * <p>
     * This method is useful when file content is already available as a string (e.g., from a resource,
     * network, or already read from disk) and you want to parse it without going through the file system.
     *
     * @param content    file content as a string
     * @param targetType type to convert the content to
     * @param <T>        compile-time representation of {@code targetType}
     * @return deserialize instance of {@code targetType}
     * @throws FileIOException when the content cannot be parsed or converted
     */
    <T> T parse(String content, Class<T> targetType);

    /**
     * Writes the provided data to the supplied {@link Path}.
     *
     * @param path destination file path
     * @param data object to serialise
     * @throws FileIOException when the file cannot be written
     */
    void write(Path path, Object data);
}


