package me.siebe.flux.api.ecs;

import me.siebe.flux.api.ecs.Results.With1;
import me.siebe.flux.api.ecs.Results.With2;
import me.siebe.flux.api.ecs.Results.With3;
import me.siebe.flux.api.ecs.Results.With4;
import me.siebe.flux.util.system.ProvidableSystem;
import me.siebe.flux.util.system.SystemProvider;
import me.siebe.flux.util.system.SystemProviderType;

/**
 * Represents and Entity Component System (ECS) world.
 * <p>
 * A World is the central container for all entities and their components in an ECS architecture.
 * It provides methods to create, retrieve and delete entities, as well as query for entities
 * that have specific component combinations.
 * <p>
 * The ECS pattern separates data (components) from behavior (systems) promoting composition
 * over inheritance and enabling efficient data-oriented design.
 */
public interface World {

    /**
     * Gets the unique identifier for this world.
     *
     * @return the world's unique identifier
     */
    int getId();

    /**
     * Gets the name of this world.
     *
     * @return the world's name, never null
     */
    String getName();

    // =================================================================================================================
    // World creation methods
    // =================================================================================================================

    /**
     * Creates a new World with default settings.
     * <p>
     * The world will be created with the following values:
     * <ul>
     *     <li>Maximum entity count: 1,000,000 (DEFAULT)</li>
     *     <li>Auto-generated name: {@code simple-world-<id>} (DEFAULT)</li>
     * </ul>
     *
     * @return a new World instance
     */
    static World create() {
        return factory().create();
    }

    /**
     * Creates a new World with the specified maximum entity count.
     * <p>
     * The world will be created with the following values:
     * <ul>
     *     <li>Maximum entity count: {@code <maxEntities>}</li>
     *     <li>Auto-generated name: {@code simple-world-<id>} (DEFAULT)</li>
     * </ul>
     *
     * @param maxEntities the maximum number of entities this world can contain
     * @return a new World instance
     */
    static World create(int maxEntities) {
        return factory(maxEntities).create();
    }

    /**
     * Creates a new World with the specified name.
     * <p>
     * The world will be created with the following values:
     * <ul>
     *     <li>Maximum entity count: 1,000,000 (DEFAULT)</li>
     *     <li>Specified world name: {@code <name>}</li>
     * </ul>
     *
     * @param name the name for the world
     * @return a new World instance
     */
    static World create(String name) {
        return factory().create(name);
    }

    /**
     * Creates a new World with the specified name.
     * <p>
     * The world will be created with the following values:
     * <ul>
     *     <li>Maximum entity count: {@code <maxEntities>}</li>
     *     <li>Specified world name: {@code <name>}</li>
     * </ul>
     *
     * @param name        the name for the world
     * @param maxEntities the maximum number of entities this world can contain
     * @return a new World instance
     */
    static World create(String name, int maxEntities) {
        return factory(maxEntities).create(name);
    }

    /**
     * Gets a factory for creating World instances.
     * <p>
     * The factory is provided through the {@link SystemProvider}, allowing custom implementations to be used.
     *
     * @return a World.Factory instance
     */
    static World.Factory factory() {
        return SystemProvider.provide(World.Factory.class, SystemProviderType.ALL);
    }

    /**
     * Gets a factory for creating World instances with a specified maximum entity count.
     *
     * @param maxEntities the maximum number of entities the created worlds can contain
     * @return a configured World.Factory instance
     */
    static World.Factory factory(int maxEntities) {
        return factory().withMaxEntities(maxEntities);
    }

    /**
     * Factory interface for creating World instances.
     */
    interface Factory extends ProvidableSystem {
        /**
         * Configures the factory to create worlds with specified maximum entity count.
         *
         * @param maxEntities the maximum number of entities each World created by this factory can contain
         * @return this factory instance for method chaining
         */
        Factory withMaxEntities(int maxEntities);

        /**
         * Creates a new World with default settings and auto-generated name.
         *
         * @return a new World instance
         */
        World create();

        /**
         * Creates a new world with the specified name.
         *
         * @param name the name for the world
         * @return a new World instance
         */
        World create(String name);
    }


    // =================================================================================================================
    // Entity managing method
    // =================================================================================================================

    /**
     * Creates a new entity in this world with the specified components.
     * <p>
     * The entity is assigned a unique ID and the provided components are attached to it.
     * Null components in the array are ignored.
     *
     * @param components the initial components to attach to the entity
     * @return the newly created Entity
     */
    Entity createEntity(Object... components);

    /**
     * Retrieves an entity by its ID.
     *
     * @param id the entity ID
     * @return the Entity with the specified ID, or null if no such entity exists in this world
     */
    Entity getEntity(int id);

    /**
     * Deletes an entity from this world.
     * <p>
     * This removes the entity and all its components from the world. The entity's ID
     * may be recycled for future entities.
     *
     * @param entity the entity to delete
     * @return true if the entity was successfully deleted, false otherwise
     */
    boolean deleteEntity(Entity entity);


    // =================================================================================================================
    // Entity searching methods
    // =================================================================================================================

    /**
     * Finds all entities in this world that have a component of the specified type.
     *
     * @param <T>  the component type
     * @param type the component class
     * @return a {@link Results} object containing all entities with the specified component
     */
    <T> Results<With1<T>> findEntitiesWith(Class<T> type);

    /**
     * Finds all entities in this world that have both of the specified component types.
     *
     * @param <T1>  the first component type
     * @param <T2>  the second component type
     * @param type1 the first component class
     * @param type2 the second component class
     * @return a Results object containing all entities with both components
     */
    <T1, T2> Results<With2<T1, T2>> findEntitiesWith(Class<T1> type1, Class<T2> type2);

    /**
     * Finds all entities in this world that have all three of the specified component types.
     *
     * @param <T1>  the first component type
     * @param <T2>  the second component type
     * @param <T3>  the third component type
     * @param type1 the first component class
     * @param type2 the second component class
     * @param type3 the third component class
     * @return a Results object containing all entities with all three components
     */
    <T1, T2, T3> Results<With3<T1, T2, T3>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3);

    /**
     * Finds all entities in this world that have all four of the specified component types.
     *
     * @param <T1>  the first component type
     * @param <T2>  the second component type
     * @param <T3>  the third component type
     * @param <T4>  the fourth component type
     * @param type1 the first component class
     * @param type2 the second component class
     * @param type3 the third component class
     * @param type4 the fourth component class
     * @return a Results object containing all entities with all four components
     */
    <T1, T2, T3, T4> Results<With4<T1, T2, T3, T4>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4);
}
