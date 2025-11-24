package me.siebe.flux.util.exceptions;

public class WindowException extends EngineException {

    public WindowException(String message) {
        super(message);
    }

    public WindowException(String message, Throwable cause) {
        super(message, cause);
    }

    public static WindowException creationFailed(String reason) {
        return new WindowException("Failed to create window: " + reason);
    }

    public static WindowException creationFailed(String reason, Throwable cause) {
        return new WindowException("Failed to create window: " + reason, cause);
    }
}