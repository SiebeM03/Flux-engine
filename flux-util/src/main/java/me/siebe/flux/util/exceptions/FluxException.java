package me.siebe.flux.util.exceptions;

/**
 * Base class for all unchecked exceptions thrown by the Flux engine or games.
 * <p>
 * Extending {@link RuntimeException} keeps the API lightweight while allowing callers to catch a single type when they
 * need to handle engine specific failures.
 */
public class FluxException extends RuntimeException {

    /**
     * Creates a new exception with the provided message.
     *
     * @param message human-readable description of the failure
     */
    public FluxException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the provided message and root cause.
     *
     * @param message description of the failure
     * @param cause   the underlying reason for the exception
     */
    public FluxException(String message, Throwable cause) {
        super(message, cause);
    }
}

