package me.siebe.flux.util.exceptions;

public class ShaderException extends EngineException {
    public ShaderException(String message) {
        super(message);
    }

    public ShaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ShaderException failedToCreateProgram() {
        return new ShaderException("Failed to create shader program");
    }
}
