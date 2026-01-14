package me.siebe.flux.api.renderer;

public class RendererProvider {
    private static RendererProvider instance;
    private final Renderer renderer;

    private RendererProvider(Renderer renderer) {
        this.renderer = renderer;
    }

    public static void init(Renderer renderer) {
        if (instance != null) {
            throw new IllegalStateException("Renderer instance has already been initialized.");
        }
        instance = new RendererProvider(renderer);
    }

    public static Renderer get() {
        return instance.renderer;
    }
}
