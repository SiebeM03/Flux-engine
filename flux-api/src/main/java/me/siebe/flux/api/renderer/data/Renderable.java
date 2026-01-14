package me.siebe.flux.api.renderer.data;

public interface Renderable {
    default void init() {}

    void render();

    default void destroy() {}
}
