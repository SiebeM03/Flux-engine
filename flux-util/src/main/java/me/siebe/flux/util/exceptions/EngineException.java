package me.siebe.flux.util.exceptions;

public class EngineException extends FluxException {

    public EngineException(String message) {
        super(message);
    }

    public EngineException(String message, Throwable cause) {
        super(message, cause);
    }
}

