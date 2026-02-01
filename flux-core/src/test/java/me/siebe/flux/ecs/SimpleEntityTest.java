package me.siebe.flux.ecs;

import me.siebe.flux.api.ecs.Entity;
import me.siebe.flux.api.ecs.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleEntityTest {
    private SimpleWorld world;

    @BeforeEach
    void setUp() {
        world = (SimpleWorld) World.create("test-world", 100);
    }

    // =================================================================================================================
    // Basic entity operations
    // =================================================================================================================

    @Test
    void getId_ShouldReturnAssignedId() {
        Entity entity = world.createEntity();
        assertEquals(0, entity.getId());

        Entity entity2 = world.createEntity();
        assertEquals(1, entity2.getId());
    }

    @Test
    void add_WithSingleComponent_ShouldAddComponent() {
        Entity entity = world.createEntity();
        entity.add(new TestComponents.Position(10, 20));

        assertTrue(entity.has(TestComponents.Position.class));
        assertEquals(10f, entity.get(TestComponents.Position.class).x);
        assertEquals(20f, entity.get(TestComponents.Position.class).y);
    }

    @Test
    void add_ShouldReturnSameEntityForChaining() {
        Entity entity = world.createEntity();
        Entity returned = entity.add(new TestComponents.Position(10, 20));

        assertSame(entity, returned);
    }

    @Test
    void add_WithMethodChaining_ShouldWorkCorrectly() {
        Entity entity = world.createEntity()
                .add(new TestComponents.Position(1, 2))
                .add(new TestComponents.Velocity(3, 4))
                .add(new TestComponents.Health(100, 100));

        assertTrue(entity.has(TestComponents.Position.class));
        assertTrue(entity.has(TestComponents.Velocity.class));
        assertTrue(entity.has(TestComponents.Health.class));
    }

    @Test
    void add_WithNull_ShouldNotThrow() {
        Entity entity = world.createEntity();
        Entity returned = entity.add(null);

        assertSame(entity, returned);
    }

    @Test
    void add_WithDuplicateComponentType_ShouldThrowException() {
        Entity entity = world.createEntity();
        entity.add(new TestComponents.Position(0, 0));

        assertThrows(IllegalArgumentException.class, () -> {
            entity.add(new TestComponents.Position(1, 1));
        });
    }

    // =================================================================================================================
    // Get component operations
    // =================================================================================================================

    @Test
    void get_WithExistingComponent_ShouldReturnComponent() {
        Entity entity = world.createEntity();
        TestComponents.Position pos = new TestComponents.Position(10, 20);
        entity.add(pos);

        TestComponents.Position retrieved = entity.get(TestComponents.Position.class);
        assertNotNull(retrieved);
        assertSame(pos, retrieved);
    }

    @Test
    void get_WithNonExistentComponent_ShouldReturnNull() {
        Entity entity = world.createEntity();
        assertNull(entity.get(TestComponents.Position.class));
    }

    @Test
    void get_WithNonExistentComponentType_ShouldReturnNull() {
        Entity entity = world.createEntity();
        entity.add(new TestComponents.Position());

        assertNull(entity.get(TestComponents.Velocity.class));
    }

    @Test
    void get_ShouldReturnSameComponentInstance() {
        Entity entity = world.createEntity();
        TestComponents.Counter counter = new TestComponents.Counter(0);
        entity.add(counter);

        TestComponents.Counter retrieved = entity.get(TestComponents.Counter.class);
        retrieved.increment();

        assertEquals(1, entity.get(TestComponents.Counter.class).count);
        assertEquals(1, counter.count);
    }


    // =================================================================================================================
    // Has component operations
    // =================================================================================================================

    @Test
    void has_WithExistingComponent_ShouldReturnTrue() {
        Entity entity = world.createEntity();
        entity.add(new TestComponents.Position());

        assertTrue(entity.has(TestComponents.Position.class));
    }

    @Test
    void has_WithNonExistentComponent_ShouldReturnFalse() {
        Entity entity = world.createEntity();

        assertFalse(entity.has(TestComponents.Position.class));
    }

    @Test
    void has_AfterRemoval_ShouldReturnFalse() {
        Entity entity = world.createEntity();
        entity.add(new TestComponents.Position());
        entity.removeType(TestComponents.Position.class);

        assertFalse(entity.has(TestComponents.Position.class));
    }


    // =================================================================================================================
    // Remove component operations
    // =================================================================================================================

    @Test
    void removeType_WithExistingComponent_ShouldReturnTrue() {
        Entity entity = world.createEntity();
        entity.add(new TestComponents.Position());

        boolean removed = entity.removeType(TestComponents.Position.class);

        assertTrue(removed);
        assertFalse(entity.has(TestComponents.Position.class));
    }

    @Test
    void removeType_WithNonExistentComponent_ShouldReturnFalse() {
        Entity entity = world.createEntity();

        boolean removed = entity.removeType(TestComponents.Position.class);

        assertFalse(removed);
    }

    @Test
    void removeType_ShouldNotAffectOtherComponents() {
        Entity entity = world.createEntity()
                .add(new TestComponents.Position())
                .add(new TestComponents.Velocity())
                .add(new TestComponents.Health());

        entity.removeType(TestComponents.Velocity.class);

        assertTrue(entity.has(TestComponents.Position.class));
        assertFalse(entity.has(TestComponents.Velocity.class));
        assertTrue(entity.has(TestComponents.Health.class));
    }

    @Test
    void removeType_WithUnregisteredComponentType_ShouldReturnFalse() {
        Entity entity = world.createEntity();

        boolean removed = entity.removeType(TestComponents.Transform.class);

        assertFalse(removed);
    }

    // =================================================================================================================
    // Delete operations
    // =================================================================================================================

    @Test
    void delete_ShouldReturnTrue() {
        Entity entity = world.createEntity();

        boolean deleted = entity.delete();

        assertTrue(deleted);
    }

    @Test
    void delete_ShouldRemoveEntityFromWorld() {
        Entity entity = world.createEntity();
        int id = entity.getId();

        entity.delete();

        assertNull(world.getEntity(id));
    }

    @Test
    void delete_ShouldRemoveAllComponents() {
        Entity entity = world.createEntity()
                .add(new TestComponents.Position())
                .add(new TestComponents.Velocity());
        int id = entity.getId();

        entity.delete();

        // Components should be removed (verify through a new entity check)
        ComponentStore<TestComponents.Position> posStore = world.getComponentRegistry().getComponentStore(TestComponents.Position.class);
        ComponentStore<TestComponents.Velocity> velStore = world.getComponentRegistry().getComponentStore(TestComponents.Velocity.class);

        assertFalse(posStore.has(id));
        assertFalse(velStore.has(id));
    }


    // =================================================================================================================
    // Equals and hashCode
    // =================================================================================================================

    @Test
    void equals_WithSameEntity_ShouldReturnTrue() {
        Entity entity = world.createEntity();
        assertEquals(entity, entity);
    }

    @Test
    void equals_WithDifferentEntitiesSameWorld_ShouldReturnFalse() {
        Entity entity1 = world.createEntity();
        Entity entity2 = world.createEntity();

        assertNotEquals(entity1, entity2);
    }

    @Test
    void equals_WithSameIdDifferentWorlds_ShouldReturnFalse() {
        World world2 = World.create("test-world-2", 100);

        Entity entity1 = world.createEntity();
        Entity entity2 = world2.createEntity();

        // Both have ID 0, but different worlds
        assertEquals(entity1.getId(), entity2.getId());
        assertNotEquals(entity1, entity2);
    }

    @Test
    void equals_WithNull_ShouldReturnFalse() {
        Entity entity = world.createEntity();
        assertNotEquals(null, entity);
    }

    @Test
    void equals_WithDifferentType_ShouldReturnFalse() {
        Entity entity = world.createEntity();
        assertNotEquals("not an entity", entity);
    }

    @Test
    void hashCode_ForSameEntity_ShouldBeConsistent() {
        Entity entity = world.createEntity();
        int hash1 = entity.hashCode();
        int hash2 = entity.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    void hashCode_ForDifferentEntities_ShouldGenerallyDiffer() {
        Entity entity1 = world.createEntity();
        Entity entity2 = world.createEntity();

        // Not strictly required, but expected for good hashing
        assertNotEquals(entity1.hashCode(), entity2.hashCode());
    }


    // =================================================================================================================
    // Complex scenarios
    // =================================================================================================================

    @Test
    void entity_After_WorldDeletesIt_ShouldNotBeRetrievable() {
        Entity entity = world.createEntity();
        int id = entity.getId();

        world.deleteEntity(entity);

        assertNull(world.getEntity(id));
    }

    @Test
    void multipleEntities_ShouldHaveIndependentComponents() {
        Entity entity1 = world.createEntity().add(new TestComponents.Position(1, 1));
        Entity entity2 = world.createEntity().add(new TestComponents.Position(2, 2));
        Entity entity3 = world.createEntity().add(new TestComponents.Position(3, 3));

        assertEquals(1f, entity1.get(TestComponents.Position.class).x);
        assertEquals(2f, entity2.get(TestComponents.Position.class).x);
        assertEquals(3f, entity3.get(TestComponents.Position.class).x);

        // Modifying one shouldn't affect others
        entity1.get(TestComponents.Position.class).x = 100f;

        assertEquals(100f, entity1.get(TestComponents.Position.class).x);
        assertEquals(2f, entity2.get(TestComponents.Position.class).x);
        assertEquals(3f, entity3.get(TestComponents.Position.class).x);
    }

    @Test
    void entity_ComponentModification_ShouldPersist() {
        Entity entity = world.createEntity().add(new TestComponents.Counter(0));

        entity.get(TestComponents.Counter.class).increment();
        entity.get(TestComponents.Counter.class).increment();
        entity.get(TestComponents.Counter.class).increment();

        assertEquals(3, entity.get(TestComponents.Counter.class).count);
    }
}
