package me.siebe.flux.api.application;

public interface EngineSystem {
    default void init() {}

    default void update() {}

    default void destroy() {}
}
