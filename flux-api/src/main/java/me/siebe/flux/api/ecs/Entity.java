package me.siebe.flux.api.ecs;

public interface Entity {
    Entity add(Object component);

    boolean removeType(Class<?> componentType);

    boolean has(Class<?> componentType);

    <T> T get(Class<T> componentType);
}