package me.siebe.flux.test.assertions;

import me.siebe.flux.api.ecs.Entity;
import me.siebe.flux.api.ecs.Results;
import me.siebe.flux.api.ecs.World;

import java.util.function.Consumer;

/**
 * Assertion helpers for Flux's tests related to ECS. Throw {@link AssertionError} on failure so they work
 * with JUnit 5 and other test runners.
 */
public final class ECSTestAssertions {
    private ECSTestAssertions() {}

    /**
     * Asserts that the number of items in the query results equals the expected value.
     *
     * @param results  query results (e.g. from {@link World#findEntitiesWith(Class)})
     * @param expected expected count
     * @throws AssertionError if the count does not match
     */
    public static void assertResultCount(Results<?> results, int expected) {
        long count = results.stream().count();
        if (count != expected) {
            throw new AssertionError("Expected " + expected + " result(s), but got " + count);
        }
    }

    /**
     * Asserts that the world has exactly one entity with the given component type, then
     * runs the consumer on that component for further checks.
     *
     * @param world           the world to query
     * @param componentType   the component type (e.g. Position.class)
     * @param assertComponent consumer that can assert on the single component (e.g. {@code pos -> assertEquals(10f, pos.x)})
     * @param <T>             the component type
     * @throws AssertionError if there are zero or more than one matching entities, or if the consumer throws
     */
    public static <T> void assertOneEntityWith(World world, Class<T> componentType, Consumer<T> assertComponent) {
        Results<Results.With1<T>> results = world.findEntitiesWith(componentType);
        var list = results.stream().toList();
        if (list.isEmpty()) {
            throw new AssertionError("Expected exactly one entity with " + componentType.getSimpleName() + ", but found none");
        }
        if (list.size() > 1) {
            throw new AssertionError("Expected exactly one entity with " + componentType.getSimpleName() + ", but found " + list.size());
        }
        if (assertComponent != null) {
            assertComponent.accept(list.getFirst().comp());
        }
    }

    /**
     * Asserts that the entity has a component of the given type and runs the consumer on it.
     *
     * @param entity          the entity
     * @param componentType   the component type
     * @param assertComponent optional consumer to assert on the component
     * @param <T>             the component type
     * @throws AssertionError if the entity does not have the component, or if the consumer throws
     */
    public static <T> void assertHasComponent(Entity entity, Class<T> componentType, Consumer<T> assertComponent) {
        if (entity == null) {
            throw new AssertionError("Entity is null");
        }
        if (!entity.has(componentType)) {
            throw new AssertionError("Entity " + entity.getId() + " does not have component " + componentType.getSimpleName());
        }
        T comp = entity.get(componentType);
        if (assertComponent != null) {
            assertComponent.accept(comp);
        }
    }

    /**
     * Asserts that the entity has a component of the given type.
     */
    public static <T> void assertHasComponent(Entity entity, Class<T> componentType) {
        assertHasComponent(entity, componentType, null);
    }
}
