package me.siebe.flux.ecs;

import me.siebe.flux.api.ecs.EcsSystem;
import me.siebe.flux.api.ecs.Entity;
import me.siebe.flux.api.ecs.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleWorldTest {
    private World world;

    @BeforeEach
    void setUp() {
        world = World.create("test-world", 100);
    }


    // =================================================================================================================
    // World creation
    // =================================================================================================================

    @Test
    void create_WithDefaultSettings_ShouldCreateWorld() {
        World defaultWorld = World.create();

        assertNotNull(defaultWorld);
        assertTrue(defaultWorld.getId() > 0);
        assertNotNull(defaultWorld.getName());
    }

    @Test
    void create_WithName_ShouldUSeProvidedName() {
        World namedWorld = World.create("my-world");

        assertEquals("my-world", namedWorld.getName());
    }

    @Test
    void create_WithMaxEntities_ShouldAcceptParameter() {
        World limitedWorld = World.create(50);

        assertNotNull(limitedWorld);
    }

    @Test
    void create_WithNameAndMaxEntities_ShouldUseProvidedValues() {
        World customWorld = World.create("custom-world", 200);

        assertEquals("custom-world", customWorld.getName());
    }

    @Test
    void getId_ShouldReturnUniqueId() {
        World world1 = World.create();
        World world2 = World.create();

        assertNotEquals(world1.getId(), world2.getId());
    }

    @Test
    void getName_ShouldReturnConfiguredName() {
        assertEquals("test-world", world.getName());
    }

    @Test
    void factory_ShouldCreateConfigurableFactory() {
        World.Factory factory = World.factory();

        assertNotNull(factory);

        World factoryWorld = factory.withMaxEntities(50).create("factory-world");
        assertEquals("factory-world", factoryWorld.getName());
    }

    @Test
    void create_ShouldRegisterWorldWithEcsSystem() {
        World registeredWorld = World.create("registered-world");

        World retrieved = EcsSystem.getWorld(registeredWorld.getId());
        assertNotNull(retrieved);
        assertEquals(registeredWorld.getId(), retrieved.getId());
    }


    // =================================================================================================================
    // Entity creation
    // =================================================================================================================

    @Test
    void createEntity_WithNoComponents_ShouldCreateEmptyEntity() {
        Entity entity = world.createEntity();

        assertNotNull(entity);
        assertEquals(0, entity.getId());
    }

    @Test
    void createEntity_WithComponents_ShouldAttachComponents() {
        Entity entity = world.createEntity(
                new TestComponents.Position(10, 20),
                new TestComponents.Velocity(1, 2)
        );

        assertTrue(entity.has(TestComponents.Position.class));
        assertTrue(entity.has(TestComponents.Velocity.class));
        assertEquals(10f, entity.get(TestComponents.Position.class).x);
        assertEquals(1f, entity.get(TestComponents.Velocity.class).dx);
    }

    @Test
    void createEntity_WithNullComponents_ShouldIgnoreNulls() {
        Entity entity = world.createEntity(
                new TestComponents.Position(1, 2),
                null,
                new TestComponents.Velocity(3, 4),
                null
        );

        assertTrue(entity.has(TestComponents.Position.class));
        assertTrue(entity.has(TestComponents.Velocity.class));
    }

    @Test
    void createEntity_MultipleEntities_ShouldHaveSequentialIds() {
        Entity entity1 = world.createEntity();
        Entity entity2 = world.createEntity();
        Entity entity3 = world.createEntity();

        assertEquals(0, entity1.getId());
        assertEquals(1, entity2.getId());
        assertEquals(2, entity3.getId());
    }

    @Test
    void createEntity_AfterDeletion_ShouldRecycleId() {
        Entity entity1 = world.createEntity();
        Entity entity2 = world.createEntity();

        int deletedId = entity1.getId();
        world.deleteEntity(entity1);

        Entity entity3 = world.createEntity();

        // Should reuse the deleted ID
        assertEquals(deletedId, entity3.getId());
    }

    @Test
    void createEntity_WithManyEntities_ShouldHandleManyCreations() {
        for (int i = 0; i < 50; i++) {
            Entity entity = world.createEntity(new TestComponents.Position(i, i));
            assertEquals(i, entity.getId());
        }
    }


    // =================================================================================================================
    // Entity retrieval
    // =================================================================================================================

    @Test
    void getEntity_WithValidId_ShouldReturnEntity() {
        Entity created = world.createEntity();

        Entity retrieved = world.getEntity(created.getId());

        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
    }

    @Test
    void getEntity_WithInvalidId_ShouldReturnNull() {
        assertNull(world.getEntity(999));
    }

    @Test
    void getEntity_WithDeletedEntityId_ShouldReturnNull() {
        Entity entity = world.createEntity();
        int id = entity.getId();

        world.deleteEntity(entity);

        assertNull(world.getEntity(id));
    }

    @Test
    void getEntity_ShouldReturnSameEntityInstance() {
        Entity created = world.createEntity();

        Entity retrieved1 = world.getEntity(created.getId());
        Entity retrieved2 = world.getEntity(created.getId());

        assertSame(retrieved1, retrieved2);
    }


    // =================================================================================================================
    // Entity deletion
    // =================================================================================================================

    @Test
    void deleteEntity_WithValidEntity_ShouldReturnTrue() {
        Entity entity = world.createEntity();

        boolean deleted = world.deleteEntity(entity);

        assertTrue(deleted);
    }

    @Test
    void deleteEntity_WithNonExistentEntity_ShouldReturnFalse() {
        World otherWorld = World.create("other-world", 100);
        Entity entityFromOtherWorld = otherWorld.createEntity();

        // Entity doesn't exist in this world
        boolean deleted = world.deleteEntity(entityFromOtherWorld);

        assertFalse(deleted);
    }

    @Test
    void deleteEntity_ShouldRemoveAllComponents() {
        Entity entity = world.createEntity(
                new TestComponents.Position(),
                new TestComponents.Velocity()
        );
        int id = entity.getId();

        world.deleteEntity(entity);

        // Check the component registry to see if the components are still present
        SimpleWorld simpleWorld = (SimpleWorld) world;
        assertFalse(simpleWorld.getComponentRegistry().getComponentStore(TestComponents.Position.class).has(id));
        assertFalse(simpleWorld.getComponentRegistry().getComponentStore(TestComponents.Velocity.class).has(id));
    }

    @Test
    void deleteEntity_ShouldRecycleIdForReuseInCorrectOrder() {
        Entity entity1 = world.createEntity();
        Entity entity2 = world.createEntity();
        Entity entity3 = world.createEntity();

        world.deleteEntity(entity2);
        world.deleteEntity(entity1);

        // Stack-based recycling: last deleted (entity1) should be reused first
        Entity newEntity1 = world.createEntity();
        Entity newEntity2 = world.createEntity();

        assertEquals(entity1.getId(), newEntity1.getId());
        assertEquals(entity2.getId(), newEntity2.getId());
    }

    @Test
    void deleteEntity_ThenCreate_NewEntityShouldBeFunctional() {
        Entity original = world.createEntity(new TestComponents.Position(10, 20));
        int id = original.getId();

        world.deleteEntity(original);

        Entity recycled = world.createEntity(new TestComponents.Velocity(5, 5));

        assertEquals(id, recycled.getId());
        assertFalse(recycled.has(TestComponents.Position.class)); // Old components shouldn't persist
        assertTrue(recycled.has(TestComponents.Velocity.class));
    }


    // =================================================================================================================
    // Equals and hashCode
    // =================================================================================================================

    @Test
    void equals_WithSameWorld_ShouldReturnTrue() {
        assertEquals(world, world);
    }

    @Test
    void equals_WithDifferentWorldsSameId_ShouldCompareById() {
        // Note: World equality is based on ID
        World world1 = World.create("w1");
        World world2 = World.create("w2");

        assertNotEquals(world1, world2);
    }

    @Test
    void equals_WithNull_ShouldReturnFalse() {
        assertNotEquals(null, world);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        int hash1 = world.hashCode();
        int hash2 = world.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    void hashCode_DifferentWorlds_ShouldGenerallyDiffer() {
        World world1 = World.create();
        World world2 = World.create();

        assertNotEquals(world1.hashCode(), world2.hashCode());
    }


    // =================================================================================================================
    // Complex scenarios
    // =================================================================================================================

    @Test
    void createDeleteCycle_ShouldMaintainConsistentState() {
        // Create several entities
        Entity[] entities = new Entity[10];
        for (int i = 0; i < 10; i++) {
            entities[i] = world.createEntity(new TestComponents.Position(i, i));
        }

        // Delete ever other entity
        for (int i = 0; i < 10; i += 2) {
            world.deleteEntity(entities[i]);
        }

        // Verify deleted entities
        for (int i = 0; i < 10; i += 2) {
            assertNull(world.getEntity(entities[i].getId()));
        }

        // Verify remaining entities
        for (int i = 1; i < 10; i += 2) {
            Entity retrieved = world.getEntity(entities[i].getId());
            assertNotNull(retrieved);
            assertEquals(i, retrieved.get(TestComponents.Position.class).x);
        }

        // Create new entities (should reuse IDs)
        Entity newEntity1 = world.createEntity(new TestComponents.Velocity(100, 100));
        Entity newEntity2 = world.createEntity(new TestComponents.Velocity(200, 200));

        assertNotNull(world.getEntity(newEntity1.getId()));
        assertNotNull(world.getEntity(newEntity2.getId()));
    }

    @Test
    void multipleEntityTypes_ShouldCoexist() {
        // Create entities with different component combinations
        Entity player = world.createEntity(
                new TestComponents.Position(0, 0),
                new TestComponents.Velocity(1, 0),
                new TestComponents.Health(100, 100),
                new TestComponents.PlayerTag()
        );

        Entity enemy = world.createEntity(
                new TestComponents.Position(10, 10),
                new TestComponents.Velocity(-1, 0),
                new TestComponents.Health(50, 50),
                new TestComponents.EnemyTag()
        );

        Entity staticObject = world.createEntity(
                new TestComponents.Position(5, 5)
        );

        assertTrue(player.has(TestComponents.PlayerTag.class));
        assertFalse(player.has(TestComponents.EnemyTag.class));

        assertTrue(enemy.has(TestComponents.EnemyTag.class));
        assertFalse(enemy.has(TestComponents.PlayerTag.class));

        assertFalse(staticObject.has(TestComponents.Velocity.class));
        assertFalse(staticObject.has(TestComponents.Health.class));
    }

    @Test
    void largeNumberOfEntities_ShouldHandleEfficiently() {
        int count = 100;

        for (int i = 0; i < count; i++) {
            world.createEntity(new TestComponents.Position(i, i));
        }

        // Verify all entities exist
        for (int i = 0; i < count; i++) {
            Entity entity = world.getEntity(i);
            assertNotNull(entity);
            assertEquals(i, entity.get(TestComponents.Position.class).x);
        }
    }
}
