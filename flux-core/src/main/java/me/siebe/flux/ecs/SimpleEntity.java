package me.siebe.flux.ecs;

import me.siebe.flux.api.ecs.Entity;

/**
 * A simple implementation of the {@link Entity} interface.
 * <p>
 * This implementation provides basic entity functionality with a unique ID.
 * Component management is handled by the {@link SimpleWorld World} through the {@link ComponentRegistry}.
 * <p>
 * This is an internal implementation class and should not be instantiated directly.
 */
class SimpleEntity implements Entity {
    private final int id;
    private final SimpleWorld world;

    /**
     * Creates a new SimpleEntity with the specified ID and world reference.
     *
     * @param id    the unique identifier for this entity
     * @param world the world this entity belongs to
     */
    SimpleEntity(int id, SimpleWorld world) {
        this.id = id;
        this.world = world;
    }

    /** {@inheritDoc} */
    @Override
    public int getId() {
        return id;
    }

    public SimpleWorld getWorld() {
        return world;
    }

    @Override
    public Entity add(Object component) {
        if (component == null) return this;
        world.getComponentRegistry().addComponent(id, component);
        return this;
    }

    @Override
    public boolean removeType(Class<?> componentType) {
        ComponentStore<?> store = world.getComponentRegistry().getComponentStore(componentType);
        if (store == null) return false;
        store.remove(id);
        return true;
    }

    @Override
    public boolean has(Class<?> componentType) {
        ComponentStore<?> store = world.getComponentRegistry().getComponentStore(componentType);
        if (store == null) return false;
        return store.has(id);
    }

    @Override
    public <T> T get(Class<T> componentType) {
        ComponentStore<T> store = world.getComponentRegistry().getComponentStore(componentType);
        if (store == null) return null;
        return store.get(id);
    }

    @Override
    public boolean delete() {
        return world.deleteEntity(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleEntity entity)) return false;
        if (!world.equals(entity.world)) return false;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return 31 * id + world.hashCode();
    }
}
