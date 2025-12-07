package me.siebe.flux.api.renderer;

import me.siebe.flux.util.exceptions.EngineException;

public class RenderException extends EngineException {
    public RenderException(String message) {
        super(message);
    }

    public RenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public static RenderException triedRenderingWithoutContext() {
        return new RenderException("Tried rendering while Render context was null." +
                "Make sure RenderContext is set during initialization.");
    }
}
