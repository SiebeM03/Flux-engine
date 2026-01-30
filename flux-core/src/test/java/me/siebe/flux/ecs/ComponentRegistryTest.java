package me.siebe.flux.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ComponentRegistryTest {
    private static final int MAX_ENTITIES = 100;
    private ComponentRegistry registry;


    @BeforeEach
    void setUp() {
        registry = new ComponentRegistry(MAX_ENTITIES);
    }


    // =================================================================================================================
    // Component store management
    // =================================================================================================================

    @Test
    void getComponentStore_WithNoStore_ShouldReturnNull() {
        assertNull(registry.getComponentStore(TestComponents.Position.class));
    }

    @Test
    void registerComponent_ShouldCreateStore() {
        ComponentStore<TestComponents.Position> store = new ComponentStore<>(MAX_ENTITIES);
        ComponentStore<TestComponents.Position> registered = registry.registerComponent(TestComponents.Position.class, store);

        assertNotNull(registered);
        assertEquals(store, registered);
        assertEquals(store, registry.getComponentStore(TestComponents.Position.class));
    }

    @Test
    void registerComponent_WithDuplicateType_ShouldThrowException() {
        ComponentStore<TestComponents.Position> store1 = new ComponentStore<>(MAX_ENTITIES);
        registry.registerComponent(TestComponents.Position.class, store1);

        ComponentStore<TestComponents.Position> store2 = new ComponentStore<>(MAX_ENTITIES);
        assertThrows(IllegalStateException.class, () -> {
            registry.registerComponent(TestComponents.Position.class, store2);
        });
    }

    @Test
    void getComponentStore_WithDifferentTypes_ShouldReturnCorrectStores() {
        ComponentStore<TestComponents.Position> positionStore = new ComponentStore<>(MAX_ENTITIES);
        ComponentStore<TestComponents.Velocity> velocityStore = new ComponentStore<>(MAX_ENTITIES);

        registry.registerComponent(TestComponents.Position.class, positionStore);
        registry.registerComponent(TestComponents.Velocity.class, velocityStore);

        assertEquals(positionStore, registry.getComponentStore(TestComponents.Position.class));
        assertEquals(velocityStore, registry.getComponentStore(TestComponents.Velocity.class));
    }


    // =================================================================================================================
    // Add component operations
    // =================================================================================================================

    @Test
    void addComponent_WithNoExistingStore_ShouldCreateAutomatically() {
        registry.addComponent(0, new TestComponents.Position(10, 20));

        ComponentStore<TestComponents.Position> store = registry.getComponentStore(TestComponents.Position.class);
        assertNotNull(store);
        assertTrue(store.has(0));
        assertEquals(10f, store.get(0).x);
    }

    @Test
    void addComponent_WithExistingStore_ShouldUseExistingStore() {
        // Add first component (creates store)
        registry.addComponent(0, new TestComponents.Position(0, 0));

        // Add another component of same type
        registry.addComponent(1, new TestComponents.Position(1, 1));

        ComponentStore<TestComponents.Position> store = registry.getComponentStore(TestComponents.Position.class);
        assertEquals(2, store.size());
    }

    @Test
    void addComponent_WithMultipleTypes_ShouldCreateSeparateStores() {
        registry.addComponent(0, new TestComponents.Position(1, 2));
        registry.addComponent(0, new TestComponents.Velocity(3, 4));

        assertNotNull(registry.getComponentStore(TestComponents.Position.class));
        assertNotNull(registry.getComponentStore(TestComponents.Velocity.class));

        assertEquals(1, registry.getComponentStore(TestComponents.Position.class).size());
        assertEquals(1, registry.getComponentStore(TestComponents.Velocity.class).size());
    }

    @Test
    void addComponent_WithSameEntityAndType_ShouldThrowException() {
        registry.addComponent(0, new TestComponents.Position(0, 0));

        assertThrows(IllegalArgumentException.class, () -> {
            registry.addComponent(0, new TestComponents.Position(1, 1));
        });
    }


    // =================================================================================================================
    // Remove component operations
    // =================================================================================================================

    @Test
    void removeComponents_WithSingleComponent_ShouldRemoveIt() {
        registry.addComponent(0, new TestComponents.Position(0, 0));

        registry.removeComponents(0);

        ComponentStore<TestComponents.Position> store = registry.getComponentStore(TestComponents.Position.class);
        assertFalse(store.has(0));
    }

    @Test
    void removeComponents_WithMultipleComponents_ShouldRemoveAll() {
        registry.addComponent(0, new TestComponents.Position(0, 0));
        registry.addComponent(0, new TestComponents.Velocity(0, 0));
        registry.addComponent(0, new TestComponents.Health(100, 100));

        registry.removeComponents(0);

        assertFalse(registry.getComponentStore(TestComponents.Position.class).has(0));
        assertFalse(registry.getComponentStore(TestComponents.Velocity.class).has(0));
        assertFalse(registry.getComponentStore(TestComponents.Health.class).has(0));
    }

    @Test
    void removeComponents_WithNoComponents_ShouldNotThrow() {
        assertDoesNotThrow(() -> {
            registry.removeComponents(99);
        });
    }

    @Test
    void removeComponents_ShouldNotAffectOtherEntities() {
        registry.addComponent(0, new TestComponents.Position(0, 0));
        registry.addComponent(1, new TestComponents.Position(1, 1));
        registry.addComponent(2, new TestComponents.Position(2, 2));

        registry.removeComponents(1);

        assertTrue(registry.getComponentStore(TestComponents.Position.class).has(0));
        assertFalse(registry.getComponentStore(TestComponents.Position.class).has(1));
        assertTrue(registry.getComponentStore(TestComponents.Position.class).has(2));
    }


    // =================================================================================================================
    // Get components operations
    // =================================================================================================================

    @Test
    void getComponents_WithNoComponents_ShouldReturnEmptyArray() {
        Object[] components = registry.getComponents(0);
        assertEquals(0, components.length);
    }

    @Test
    void getComponents_WithSingleComponent_ShouldReturnComponent() {
        TestComponents.Position pos = new TestComponents.Position(5, 10);
        registry.addComponent(0, pos);

        Object[] components = registry.getComponents(0);
        assertEquals(1, components.length);
        assertEquals(pos, components[0]);
    }

    @Test
    void getComponents_WithMultipleComponents_ShouldReturnAllComponents() {
        TestComponents.Position pos = new TestComponents.Position(1, 2);
        TestComponents.Velocity vel = new TestComponents.Velocity(3, 4);
        TestComponents.Health health = new TestComponents.Health(100, 100);

        registry.addComponent(0, pos);
        registry.addComponent(0, vel);
        registry.addComponent(0, health);

        Object[] components = registry.getComponents(0);
        assertEquals(3, components.length);

        // Check that all components are present (order may vary)
        boolean hasPosition = false;
        boolean hasVelocity = false;
        boolean hasHealth = false;
        for (Object component : components) {
            if (component instanceof TestComponents.Position) hasPosition = true;
            if (component instanceof TestComponents.Velocity) hasVelocity = true;
            if (component instanceof TestComponents.Health) hasHealth = true;
        }
        assertTrue(hasPosition);
        assertTrue(hasVelocity);
        assertTrue(hasHealth);
    }


    // =================================================================================================================
    // Edge cases and complex scenarios
    // =================================================================================================================

    @Test
    void addAndRemove_MixedOperations_ShouldMaintainConsistentState() {
        // Add components
        registry.addComponent(0, new TestComponents.Position(0, 0));
        registry.addComponent(1, new TestComponents.Position(1, 1));
        registry.addComponent(0, new TestComponents.Velocity(0, 0));

        // Remove entity 0's components
        registry.removeComponents(0);

        // Add new components
        registry.addComponent(2, new TestComponents.Position(2, 2));
        registry.addComponent(0, new TestComponents.Health(100, 100));  // Entity 0 gets new component type

        // Verify state
        // Entity 0: Health
        assertFalse(registry.getComponentStore(TestComponents.Position.class).has(0));
        assertFalse(registry.getComponentStore(TestComponents.Velocity.class).has(0));
        assertTrue(registry.getComponentStore(TestComponents.Health.class).has(0));
        // Entity 1: Position
        assertTrue(registry.getComponentStore(TestComponents.Position.class).has(1));
        // Entity 2: Position
        assertTrue(registry.getComponentStore(TestComponents.Position.class).has(2));
    }

    @Test
    void multipleEntities_WithDifferentComponentCombinations_ShouldWorldCorrectly() {
        // Entity 0: Position only
        registry.addComponent(0, new TestComponents.Position(0, 0));

        // Entity 1: Position + Velocity
        registry.addComponent(1, new TestComponents.Position(1, 1));
        registry.addComponent(1, new TestComponents.Velocity(1, 1));

        // Entity 2: Position + Velocity + Health
        registry.addComponent(2, new TestComponents.Position(2, 2));
        registry.addComponent(2, new TestComponents.Velocity(2, 2));
        registry.addComponent(2, new TestComponents.Health(100, 100));

        // Entity 3: Velocity only
        registry.addComponent(3, new TestComponents.Velocity(3, 3));

        assertEquals(1, registry.getComponents(0).length);
        assertEquals(2, registry.getComponents(1).length);
        assertEquals(3, registry.getComponents(2).length);
        assertEquals(1, registry.getComponents(3).length);

        assertEquals(3, registry.getComponentStore(TestComponents.Position.class).size());
        assertEquals(3, registry.getComponentStore(TestComponents.Velocity.class).size());
        assertEquals(1, registry.getComponentStore(TestComponents.Health.class).size());
    }

    @Test
    void emptyTagComponent_ShouldWorkCorrectly() {
        registry.addComponent(0, new TestComponents.PlayerTag());
        registry.addComponent(1, new TestComponents.EnemyTag());
        registry.addComponent(2, new TestComponents.PlayerTag());

        assertTrue(registry.getComponentStore(TestComponents.PlayerTag.class).has(0));
        assertTrue(registry.getComponentStore(TestComponents.PlayerTag.class).has(2));
        assertTrue(registry.getComponentStore(TestComponents.EnemyTag.class).has(1));

        assertEquals(2, registry.getComponentStore(TestComponents.PlayerTag.class).size());
        assertEquals(1, registry.getComponentStore(TestComponents.EnemyTag.class).size());
    }
}
