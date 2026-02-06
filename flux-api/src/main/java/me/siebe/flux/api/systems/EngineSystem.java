package me.siebe.flux.api.systems;

public interface EngineSystem {
    default void init() {}

    default void update() {}

    default void destroy() {}
}
