package me.siebe.flux.util.io;

import me.siebe.flux.util.io.handlers.JsonFileHandler;
import me.siebe.flux.util.io.handlers.YamlFileHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinates reading and writing of files through registered {@link FileHandler} implementations.
 * <p>
 * Consumers can either explicitly specify the {@link FileFormat} to use or rely on the manager to infer it from the
 * file extension. Additional handlers may be registered at runtime to extend the supported formats.
 */
public class FileIOManager {
    private static FileIOManager instance = FileIOManager.createDefault();

    private final Map<FileFormat, FileHandler> handlers = new ConcurrentHashMap<>();

    public static FileIOManager get() {
        return instance;
    }

    /**
     * Creates a manager with the provided collection of handlers. Later registrations replace existing handlers for the
     * same {@link FileFormat}.
     *
     * @param initialHandlers handlers to register on construction
     */
    public FileIOManager(Collection<? extends FileHandler> initialHandlers) {
        Objects.requireNonNull(initialHandlers, "initialHandlers");
        initialHandlers.forEach(this::registerHandler);
        instance = this;
    }

    /**
     * Creates a manager without predefined handlers. Handlers can still be added later using
     * {@link #registerHandler(FileHandler)}.
     */
    public FileIOManager() {
        this(Collections.emptyList());
    }

    /**
     * Creates a {@link FileIOManager} pre-configured with the default handlers shipped with the Flux engine.
     *
     * @return manager instance with default handlers registered
     */
    public static FileIOManager createDefault() {
        FileIOManager manager = new FileIOManager();
        manager.registerHandler(new YamlFileHandler());
        manager.registerHandler(new JsonFileHandler());
        return manager;
    }

    /**
     * Registers or replaces a handler for its declared {@link FileFormat}.
     *
     * @param handler handler to register
     */
    public void registerHandler(FileHandler handler) {
        Objects.requireNonNull(handler, "handler");
        handlers.put(handler.getFormat(), handler);
    }

    /**
     * Reads the file at the provided {@link Path} by inferring the handler from the file extension.
     *
     * @param path       source path
     * @param targetType target type
     * @param <T>        compile-time representation of {@code targetType}
     * @return deserialised instance of {@code targetType}
     */
    public <T> T read(Path path, Class<T> targetType) {
        FileHandler handler = getHandlerByPath(path);
        return handler.read(path, targetType);
    }

    /**
     * Reads the file at the provided {@link Path} using the specified {@link FileFormat}.
     *
     * @param path       source path
     * @param targetType target type
     * @param format     desired file format
     * @param <T>        compile-time representation of {@code targetType}
     * @return deserialised instance of {@code targetType}
     */
    public <T> T read(Path path, Class<T> targetType, FileFormat format) {
        FileHandler handler = requireHandler(format);
        return handler.read(path, targetType);
    }

    /**
     * Parses the provided content string using the specified {@link FileFormat}.
     *
     * @param content    file content as a string
     * @param targetType target type
     * @param format     desired file format
     * @param <T>        compile-time representation of {@code targetType}
     * @return deserialize instance of {@code targetType}
     */
    public <T> T parse(String content, Class<T> targetType, FileFormat format) {
        FileHandler handler = requireHandler(format);
        return handler.parse(content, targetType);
    }

    /**
     * Writes the provided object to the supplied {@link Path}, inferring the {@link FileHandler} from the file
     * extension. Parent directories are created automatically if they do not exist.
     *
     * @param path destination path
     * @param data object to serialise
     */
    public void write(Path path, Object data) {
        FileHandler handler = getHandlerByPath(path);
        ensureParentDirectoryExists(path);
        handler.write(path, data);
    }

    /**
     * Writes the provided object to the supplied {@link Path} using the specified {@link FileFormat}. Parent
     * directories are created automatically if they do not exist.
     *
     * @param path   destination path
     * @param data   object to serialise
     * @param format format to use for serialisation
     */
    public void write(Path path, Object data, FileFormat format) {
        FileHandler handler = requireHandler(format);
        ensureParentDirectoryExists(path);
        handler.write(path, data);
    }

    private FileHandler requireHandler(FileFormat format) {
        Objects.requireNonNull(format, "format");
        FileHandler handler = handlers.get(format);
        if (handler == null) {
            throw new FileIOException("No handler registered for file format: " + format.getName());
        }
        return handler;
    }

    private FileHandler getHandlerByPath(Path path) {
        Objects.requireNonNull(path, "path");
        FileFormat format = detectFormat(path)
                .orElseThrow(() -> new FileIOException("Unable to determine file format for: " + path));
        return requireHandler(format);
    }

    /**
     * Attempts to determine the {@link FileFormat} from the provided path's extension.
     *
     * @param path path to inspect
     * @return optional file format
     */
    public Optional<FileFormat> detectFormat(Path path) {
        Objects.requireNonNull(path, "path");
        String filename = path.getFileName().toString();
        int index = filename.lastIndexOf('.');
        if (index == -1 || index == filename.length() - 1) {
            return Optional.empty();
        }

        String extension = filename.substring(index + 1).toLowerCase();
        return handlers.keySet().stream()
                .filter(format -> format.matchesExtension(extension))
                .findFirst();
    }

    private void ensureParentDirectoryExists(Path path) {
        Path parent = path.getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new FileIOException("Failed to create parent directories for: " + path, exception);
        }
    }
}


