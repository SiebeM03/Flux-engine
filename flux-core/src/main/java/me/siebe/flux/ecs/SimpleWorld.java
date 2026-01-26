package me.siebe.flux.ecs;

import me.siebe.flux.api.ecs.EcsSystem;
import me.siebe.flux.api.ecs.Entity;
import me.siebe.flux.api.ecs.Results;
import me.siebe.flux.api.ecs.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple implementation of the {@link World} interface.
 * <p>
 * This implementation provides a straightforward ECS world that manages entities
 * and their components. Entity IDs are recycled when entities are deleted to
 * improve memory efficiency.
 * <p>
 * This is the default implementation provided by the Flux engine.
 */
public class SimpleWorld implements World {
    private final int id;
    private final String name;

    private final List<Entity> entities = new ArrayList<>();
    private final ComponentRegistry componentRegistry;

    private final AtomicInteger nextEntityId = new AtomicInteger(0);
    private final Stack<Integer> recycledEntityIds = new Stack<>();

    private SimpleWorld(String name, int id, int maxEntities) {
        this.name = name;
        this.id = id;
        this.componentRegistry = new ComponentRegistry(maxEntities);
    }

    @Override
    public int getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Entity IDs are assigned sequentially, or reused from deleted entities
     * if available. Null components in the array are ignored.
     */
    @Override
    public Entity createEntity(Object... components) {
        int id = recycledEntityIds.isEmpty() ? nextEntityId.getAndIncrement() : recycledEntityIds.pop();
        SimpleEntity entity = new SimpleEntity(id);
        entities.add(entity);

        for (Object component : components) {
            if (component == null) continue;
            componentRegistry.addComponent(entity.getId(), component);
        }
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public Entity getEntity(int id) {
        return entities.stream()
                .filter(entity -> ((SimpleEntity) entity).getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * When an entity is deleted, its ID is recycled and can be reused for
     * future entities. All components are removed from the {@link SimpleWorld#componentRegistry}
     */
    @Override
    public boolean deleteEntity(Entity entity) {
        componentRegistry.removeComponents(entity.getId());
        recycledEntityIds.push(entity.getId());
        entities.remove(entity);
        entity.delete();
        return true;
    }


    ComponentRegistry getComponentRegistry() {
        return componentRegistry;
    }


    /** {@inheritDoc} */
    @Override
    public <T> Results<Results.With1<T>> findEntitiesWith(Class<T> type) {
        return WorldQuery.findEntitiesWith(componentRegistry, type);
    }

    /** {@inheritDoc} */
    @Override
    public <T1, T2> Results<Results.With2<T1, T2>> findEntitiesWith(Class<T1> type1, Class<T2> type2) {
        return WorldQuery.findEntitiesWith(componentRegistry, type1, type2);
    }

    /** {@inheritDoc} */
    @Override
    public <T1, T2, T3> Results<Results.With3<T1, T2, T3>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        return WorldQuery.findEntitiesWith(componentRegistry, type1, type2, type3);
    }

    /** {@inheritDoc} */
    @Override
    public <T1, T2, T3, T4> Results<Results.With4<T1, T2, T3, T4>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
        return WorldQuery.findEntitiesWith(componentRegistry, type1, type2, type3, type4);
    }


    /**
     * Factory implementation for creating SimpleWorld instances.
     */
    public static class Factory implements World.Factory {
        private static final AtomicInteger counter = new AtomicInteger(1);
        private static final int DEFAULT_MAX_ENTITIES = 1_000_000;
        private int maxEntities = DEFAULT_MAX_ENTITIES;

        /** {@inheritDoc} */
        @Override
        public World.Factory withMaxEntities(int maxEntities) {
            this.maxEntities = maxEntities;
            return this;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Creates a world with an auto-generated name in the format {@code simple-world-<N>}
         * where N is an incrementing counter.
         */
        @Override
        public World create() {
            return create(null);
        }

        /**
         * {@inheritDoc}
         * <p>
         * If the provided name is null or empty, an auto-generated name will be used.
         */
        @Override
        public World create(String name) {
            if (name == null || name.isEmpty()) {
                name = "simple-world-" + counter.get();
            }
            World world = new SimpleWorld(name, counter.get(), maxEntities);
            EcsSystem.registerWorld(world);
            counter.incrementAndGet();
            return world;
        }
    }
}
