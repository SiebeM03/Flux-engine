package me.siebe.flux.ecs;

import java.util.Arrays;

/**
 * Stores components of a specific type for entities.
 * <p>
 * ComponentStore provides efficient storage and retrieval of components using
 * a sparse array structure. It maintains bidirectional mappings between entity
 * IDs and component indices, allowing for O(1) lookups and efficient iteration.
 * <p>
 * Components are stored in a dense array, while entity-to-index mappings are
 * maintained in a sparse array. This design provides good cache locality for
 * iteration while maintaining fast random access.
 * <p>
 * This is an internal implementation class and should not be used directly
 *
 * @param <T> the component type
 */
final class ComponentStore<T> {
    private static final int INITIAL_COMPONENT_CAPACITY = 16;

    private T[] components;
    private int[] entityToIndex;
    private int[] indexToEntity;
    private int size;

    /**
     * Creates a new ComponentStore with the specified maximum entity count.
     *
     * @param maxEntities the maximum number of entities that can have components
     */
    @SuppressWarnings("unchecked")
    ComponentStore(int maxEntities) {
        components = (T[]) new Object[INITIAL_COMPONENT_CAPACITY];
        entityToIndex = new int[maxEntities];
        indexToEntity = new int[INITIAL_COMPONENT_CAPACITY];
        Arrays.fill(entityToIndex, -1);
        size = 0;
    }

    /**
     * Checks if an entity has a component of this type.
     *
     * @param entity the entity ID
     * @return true if the entity has a component in this store, false otherwise
     */
    boolean has(int entity) {
        return entityToIndex[entity] != -1;
    }

    /**
     * Gets the component for an entity.
     *
     * @param entity the entity ID
     * @return the component, or null if the entity doesn't have one in this store
     */
    T get(int entity) {
        int index = entityToIndex[entity];
        if (index == -1) return null;
        return components[index];
    }

    /**
     * Adds a component to an entity.
     *
     * @param entity    the entity ID
     * @param component the component to add
     * @throws IllegalArgumentException if the entity already has a component of this type
     */
    void add(int entity, T component) {
        if (has(entity)) {
            throw new IllegalArgumentException("Entity already has component");
        }

        ensureCapacity(size + 1);

        components[size] = component;
        indexToEntity[size] = entity;
        entityToIndex[entity] = size;
        size++;
    }

    /**
     * Removes a component from an entity.
     * <p>
     * Uses a swap-and-pop strategy to maintain dense storage: the last component
     * is moved into the removed slot, and the size is decreased
     *
     * @param entity the entity ID for which the component should be removed
     */
    void remove(int entity) {
        int index = entityToIndex[entity];
        if (index == -1) return;

        int lastIndex = size - 1;
        int lastEntity = indexToEntity[index];

        // Move last element into removed slot
        components[index] = components[lastIndex];
        indexToEntity[index] = lastEntity;
        entityToIndex[lastEntity] = index;

        // Clear last slot
        components[lastIndex] = null;
        entityToIndex[entity] = -1;
        size--;
    }

    /**
     * Gets the number of components stored.
     *
     * @return the number of components in this store
     */
    int size() {
        return size;
    }

    /**
     * Gets the entity ID at the specified index.
     * <p>
     * This method is used for iterating over all entities with this component type.
     *
     * @param index the component index
     * @return the entity ID
     */
    int getEntityAt(int index) {
        return indexToEntity[index];
    }

    /**
     * Gets the component at the specified index.
     * <p>
     * This method is used for iterating over all components of this type.
     *
     * @param index the component index
     */
    T getComponentAt(int index) {
        return components[index];
    }

    /**
     * Ensures the internal arrays have sufficient capacity.
     * <p>
     * When capacity is exceeded, arrays are doubled in size.
     *
     * @param capacity the required capacity
     */
    private void ensureCapacity(int capacity) {
        if (capacity <= components.length) return;

        int newCapacity = components.length * 2;
        components = Arrays.copyOf(components, newCapacity);
        indexToEntity = Arrays.copyOf(indexToEntity, newCapacity);
    }
}
