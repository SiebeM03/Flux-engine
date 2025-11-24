package me.siebe.flux.util.exceptions;

public class GltfException extends FluxException {

    public GltfException(String message) {
        super(message);
    }

    public GltfException(String message, Throwable cause) {
        super(message, cause);
    }

    public static GltfException resourceNotFound(String resourcePath) {
        return new GltfException("Resource not found: " + resourcePath);
    }
}
