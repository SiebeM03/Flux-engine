package me.siebe.flux.ecs;

import me.siebe.flux.api.ecs.EcsSystem;
import me.siebe.flux.api.ecs.Entity;
import me.siebe.flux.api.ecs.World;

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

    /**
     * Creates a new SimpleEntity with the specified ID.
     *
     * @param id the unique identifier for this entity
     */
    SimpleEntity(int id) {
        this.id = id;
    }

    /** {@inheritDoc} */
    @Override
    public int getId() {
        return id;
    }

    // FIXME improve this mess
    @Override
    public Entity add(Object component) {
        if (component == null) return this;
        World world = EcsSystem.getWorldByEntityId(id);
        if (world == null) return null;
        if (!(world instanceof SimpleWorld simpleWorld)) return null;

        simpleWorld.getComponentRegistry().addComponent(id, component);
        return this;
    }

    @Override
    public boolean removeType(Class<?> componentType) {
        ComponentStore<?> store = getComponentStore(componentType);
        if (store == null) return false;
        store.remove(id);
        return true;
    }

    @Override
    public boolean has(Class<?> componentType) {
        ComponentStore<?> store = getComponentStore(componentType);
        if (store == null) return false;
        return store.has(id);
    }

    @Override
    public <T> T get(Class<T> componentType) {
        ComponentStore<T> store = getComponentStore(componentType);
        if (store == null) return null;
        return store.get(id);
    }

    @Override
    public boolean delete() {
        World world = EcsSystem.getWorldByEntityId(id);
        if (world == null) return false;
        return world.deleteEntity(this);
    }

    private <T> ComponentStore<T> getComponentStore(Class<T> componentType) {
        World world = EcsSystem.getWorldByEntityId(id);
        if (world == null) return null;
        if (!(world instanceof SimpleWorld simpleWorld)) return null;
        return simpleWorld.getComponentRegistry().getComponentStore(componentType);
    }
}
