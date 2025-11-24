package me.siebe.flux.util.io;

import me.siebe.flux.util.exceptions.FluxException;

import java.nio.file.Path;

/**
 * Unchecked exception thrown when file input or output operations fail within the Flux engine utilities.
 */
public class FileIOException extends FluxException {

    /**
     * Creates an exception with a context-rich message.
     *
     * @param message description of what went wrong
     */
    public FileIOException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and underlying cause.
     *
     * @param message description of what went wrong
     * @param cause   underlying exception
     */
    public FileIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public static FileIOException assetsFolderNotFound(Path assetsPath) {
        return new FileIOException("Assets folder not found. Expected location: " +
                (assetsPath != null ? assetsPath.toAbsolutePath() : "unknown") +
                ". Please create an 'assets' folder beside the JAR file or in the working directory.");
    }
}


