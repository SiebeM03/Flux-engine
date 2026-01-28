package me.siebe.flux.ecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for managing component stores by component type.
 * <p>
 * The ComponentRegistry maintains a mapping from component class to their
 * respective {@link ComponentStore} instances. It provides a method to register,
 * retrieve, and manage components for entities.
 * <p>
 * This is an internal implementation class and should not be used directly.
 */
final class ComponentRegistry {
    private final Map<Class<?>, ComponentStore<?>> componentStores = new HashMap<>();
    private final int maxEntities;

    ComponentRegistry(int maxEntities) {
        this.maxEntities = maxEntities;
    }

    /**
     * Gets the component store for the specified component type.
     *
     * @param <T>  the component type
     * @param type the component class
     * @return the ComponentStore for the type, or null if no such store exists
     */
    @SuppressWarnings("unchecked")
    <T> ComponentStore<T> getComponentStore(Class<T> type) {
        return (ComponentStore<T>) componentStores.get(type);
    }

    /**
     * Registers a component store for the specified component type.
     *
     * @param <T>            the component type
     * @param type           the component class
     * @param componentStore the ComponentStore to register
     * @return the registered ComponentStore
     * @throws IllegalStateException if a component store is already registered for the type
     */
    <T> ComponentStore<T> registerComponent(Class<T> type, ComponentStore<T> componentStore) {
        if (componentStores.containsKey(type)) {
            throw new IllegalStateException("Component store already registered for type: " + type);
        }
        componentStores.put(type, componentStore);
        return componentStore;
    }

    /**
     * Adds a component to an entity.
     * <p>
     * If no component store exists for the component type, one will be created
     * automatically
     *
     * @param <T>       the component type
     * @param entity    the entity ID
     * @param component the component to add
     */
    @SuppressWarnings("unchecked")
    <T> void addComponent(int entity, T component) {
        Class<T> type = (Class<T>) component.getClass();
        ComponentStore<T> store = getComponentStore(type);
        if (store == null) {
            store = registerComponent(type, new ComponentStore<>(maxEntities));
        }
        store.add(entity, component);
    }

    /**
     * Removes all components from an entity.
     *
     * @param entity the entity ID
     */
    void removeComponents(int entity) {
        Object[] components = getComponents(entity);
        for (Object component : components) {
            Class<?> type = component.getClass();
            ComponentStore<?> store = getComponentStore(type);
            if (store == null) continue;
            store.remove(entity);
        }
    }

    /**
     * Gets all components for an entity.
     *
     * @param entity the entity ID
     * @return an array of all components attached to the entity
     */
    Object[] getComponents(int entity) {
        List<Object> components = new ArrayList<>();
        for (ComponentStore<?> store : componentStores.values()) {
            Object component = store.get(entity);
            if (component == null) continue;
            components.add(component);
        }
        return components.toArray();
    }
}
