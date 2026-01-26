package me.siebe.flux.api.ecs;

/**
 * Represents an entity in the Entity Component System (ECS).
 * <p>
 * An entity is a unique identifier that can have components attached to it.
 * Entities themselves contain no data or behavior - they are simply containers
 * for components. Components hold the actual data while systems operate on
 * entities that have specific component combinations.
 * <p>
 * Each entity has a unique integer ID that remains constant for its lifetime.
 */
public interface Entity {
    /**
     * Add a components to this entity.
     * <p>
     * If the entity already has a component of the same type, the behavior
     * is implementation-dependent (may replace or throw an exception)
     *
     * @param component the component to add
     * @return this entity for method chaining
     */
    Entity add(Object component);

    /**
     * Removes a component of the specified type from this entity
     *
     * @param componentType the class of the component type to remove
     * @return true if a component was removed, false if the entity didn't have
     * a component of that type
     */
    boolean removeType(Class<?> componentType);

    /**
     * Checks if this entity has a component of the specified type.
     *
     * @param componentType the component type to check for
     * @return true if the entity has a component of the specified type, false otherwise
     */
    boolean has(Class<?> componentType);

    /**
     * Gets a component of the specified type from this entity.
     *
     * @param <T>           the component type
     * @param componentType the class of the component type to retrieve
     * @return the component of the specified type, or null if the entity doesn't
     * have a component of that type
     */
    <T> T get(Class<T> componentType);

    /**
     * Gets the unique identifier for this entity.
     *
     * @return the entity's ID
     */
    int getId();

    /**
     * Marks this entity as deleted
     * <p>
     * This method should be called by teh World when deleting an entity.
     * After deletion, the entity should not be used for further operations.
     *
     * @return true if the entity was successfully marked as deleted
     */
    boolean delete();
}