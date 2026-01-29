package me.siebe.flux.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ComponentStoreTest {
    private static final int MAX_ENTITIES = 100;
    private ComponentStore<TestComponents.Position> store;


    @BeforeEach
    void setUp() {
        store = new ComponentStore<>(MAX_ENTITIES);
    }

    // =================================================================================================================
    // Basic operations
    // =================================================================================================================
    @Test
    void add_WithValidEntity_ShouldStoreComponent() {
        TestComponents.Position pos = new TestComponents.Position(10, 20);
        store.add(0, pos);

        assertTrue(store.has(0));
        assertEquals(pos, store.get(0));
        assertEquals(1, store.size());
    }

    @Test
    void add_WithMultipleEntities_ShouldStoreAllComponents() {
        TestComponents.Position pos1 = new TestComponents.Position(10, 20);
        TestComponents.Position pos2 = new TestComponents.Position(20, 30);
        TestComponents.Position pos3 = new TestComponents.Position(30, 40);

        store.add(0, pos1);
        store.add(1, pos2);
        store.add(2, pos3);

        assertEquals(3, store.size());

        assertTrue(store.has(0));
        assertTrue(store.has(1));
        assertTrue(store.has(2));

        assertEquals(pos1, store.get(0));
        assertEquals(pos2, store.get(1));
        assertEquals(pos3, store.get(2));
    }

    @Test
    void add_WithDuplicateEntity_ShouldThrowException() {
        store.add(0, new TestComponents.Position(10, 20));

        assertThrows(IllegalArgumentException.class, () -> {
            store.add(0, new TestComponents.Position(10, 20));
        });
    }

    @Test
    void get_WithNonExistentEntity_ShouldReturnNull() {
        assertNull(store.get(0));
        assertNull(store.get(99));
    }

    @Test
    void get_WithValidEntity_ShouldReturnComponent() {
        TestComponents.Position pos = new TestComponents.Position(5, 10);
        store.add(5, pos);

        TestComponents.Position retrieved = store.get(5);
        assertNotNull(retrieved);
        assertEquals(5f, retrieved.x);
        assertEquals(10f, retrieved.y);
    }

    @Test
    void has_WithNonExistentEntity_ShouldReturnFalse() {
        assertFalse(store.has(0));
        assertFalse(store.has(50));
    }

    @Test
    void has_WithExistingEntity_ShouldReturnTrue() {
        store.add(10, new TestComponents.Position());
        assertTrue(store.has(10));
    }


    // =================================================================================================================
    // Remove operations
    // =================================================================================================================
    @Test
    void remove_WithExistingEntity_ShouldRemoveComponent() {
        store.add(10, new TestComponents.Position());
        assertEquals(1, store.size());

        store.remove(10);

        assertFalse(store.has(10));
        assertNull(store.get(10));
        assertEquals(0, store.size());
    }

    @Test
    void remove_WithNonExistentEntity_ShouldNotThrow() {
        assertDoesNotThrow(() -> store.remove(99));
    }

    @Test
    void remove_WithSwapAndPop_ShouldMaintainDenseStorage() {
        // Add four entities
        store.add(0, new TestComponents.Position(0, 0));
        store.add(1, new TestComponents.Position(1, 1));
        store.add(2, new TestComponents.Position(2, 2));
        store.add(3, new TestComponents.Position(3, 3));

        // Remove second entity (should swap with last)
        store.remove(1);

        // Entity 0, 2 and 3 should still have components
        assertTrue(store.has(0));
        assertFalse(store.has(1));
        assertTrue(store.has(2));
        assertTrue(store.has(3));

        assertNull(store.get(1));

        // Verify correct values
        assertEquals(0f, store.get(0).x);
        assertEquals(2f, store.get(2).x);
        assertEquals(3f, store.get(3).x);
    }

    @Test
    void remove_LastElement_ShouldNotSwap() {
        store.add(0, new TestComponents.Position(0, 0));
        store.add(1, new TestComponents.Position(1, 1));
        store.add(2, new TestComponents.Position(2, 2));

        store.remove(2);

        assertEquals(2, store.size());
        assertTrue(store.has(0));
        assertTrue(store.has(1));
        assertFalse(store.has(2));
    }

    @Test
    void remove_OnlyElement_ShouldResultInEmptyStore() {
        store.add(5, new TestComponents.Position(5, 5));
        store.remove(5);

        assertEquals(0, store.size());
        assertFalse(store.has(5));
    }

    // =================================================================================================================
    // Index-based access (for iteration)
    // =================================================================================================================

    @Test
    void getEntityAt_ShouldReturnCorrectEntityId() {
        store.add(10, new TestComponents.Position(10, 10));
        store.add(20, new TestComponents.Position(20, 20));
        store.add(30, new TestComponents.Position(30, 30));

        // Check that we can retrieve entity IDs by index
        // Order should match insertion order
        assertEquals(10, store.getEntityAt(0));
        assertEquals(20, store.getEntityAt(1));
        assertEquals(30, store.getEntityAt(2));
    }

    @Test
    void getEntityAt_ShouldReturnCorrectComponent() {
        store.add(0, new TestComponents.Position(0, 0));
        store.add(1, new TestComponents.Position(1, 1));
        store.add(2, new TestComponents.Position(2, 2));

        assertEquals(0f, store.getComponentAt(0).x);
        assertEquals(1f, store.getComponentAt(1).x);
        assertEquals(2f, store.getComponentAt(2).x);
    }

    @Test
    void getEntityAt_AfterRemoval_ShouldReflectSwap() {
        store.add(0, new TestComponents.Position(0, 0));
        store.add(1, new TestComponents.Position(1, 1));
        store.add(2, new TestComponents.Position(2, 2));

        // Remove first element - last element (entity 2) should be swapped to index 0
        store.remove(0);

        assertEquals(2, store.size());
        // Entity 2's component should now be at index 0
        assertEquals(2, store.getEntityAt(0));
        assertEquals(1, store.getEntityAt(1));
    }


    // =================================================================================================================
    // Capacity and growth
    // =================================================================================================================

    @Test
    void add_ExceedingInitialCapacity_ShouldGrowAutomatically() {
        // Initial capacity is 16, add more than that
        for (int i = 0; i < 50; i++) {
            store.add(i, new TestComponents.Position(i, i));
        }

        assertEquals(50, store.size());

        // Verify all components are accessible
        for (int i = 0; i < 50; i++) {
            assertTrue(store.has(i));
            assertEquals(i, store.get(i).x);
        }
    }

    @Test
    void add_ManyEntities_ShouldHandleLargeCount() {
        for (int i = 0; i < MAX_ENTITIES; i++) {
            store.add(i, new TestComponents.Position(i, i * 2));
        }

        assertEquals(MAX_ENTITIES, store.size());

        // Verify some values
        assertEquals(0f, store.get(0).x);
        assertEquals(50f, store.get(50).x);
        assertEquals(99f, store.get(99).x);
    }


    // =================================================================================================================
    // Edge cases
    // =================================================================================================================

    @Test
    void size_OnNewStore_ShouldBeZero() {
        assertEquals(0, store.size());
    }

    @Test
    void add_AndRemove_MixedOperations_ShouldMaintainCorrectState() {
        store.add(0, new TestComponents.Position(0, 0));
        store.add(1, new TestComponents.Position(1, 1));
        assertEquals(2, store.size());

        store.remove(0);
        assertEquals(1, store.size());

        store.add(2, new TestComponents.Position(2, 2));
        assertEquals(2, store.size());

        store.remove(1);
        assertEquals(1, store.size());

        assertTrue(store.has(2));
        assertFalse(store.has(0));
        assertFalse(store.has(1));
    }

    @Test
    void componentModification_ShouldPersist() {
        store.add(0, new TestComponents.Position(0, 0));

        TestComponents.Position pos = store.get(0);
        pos.x = 100;
        pos.y = 200;

        TestComponents.Position retrieved = store.get(0);
        assertEquals(100f, retrieved.x);
        assertEquals(200f, retrieved.y);
    }

    @Test
    void multipleDifferentTypes_InSeparateStores_ShouldWorkIndependently() {
        ComponentStore<TestComponents.Position> positionStore = new ComponentStore<>(MAX_ENTITIES);
        ComponentStore<TestComponents.Velocity> velocityStore = new ComponentStore<>(MAX_ENTITIES);

        positionStore.add(0, new TestComponents.Position(1, 2));
        velocityStore.add(0, new TestComponents.Velocity(3, 4));

        assertEquals(1f, positionStore.get(0).x);
        assertEquals(2f, positionStore.get(0).y);
        assertEquals(3f, velocityStore.get(0).dx);
        assertEquals(4f, velocityStore.get(0).dy);

        positionStore.remove(0);
        assertTrue(velocityStore.has(0)); // Velocity should still exist
        assertFalse(positionStore.has(0));
    }
}
