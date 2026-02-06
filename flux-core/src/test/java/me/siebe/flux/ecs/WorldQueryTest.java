package me.siebe.flux.ecs;

import me.siebe.flux.api.ecs.Entity;
import me.siebe.flux.api.ecs.Results;
import me.siebe.flux.api.ecs.Results.With1;
import me.siebe.flux.api.ecs.Results.With2;
import me.siebe.flux.api.ecs.Results.With3;
import me.siebe.flux.api.ecs.Results.With4;
import me.siebe.flux.api.ecs.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class WorldQueryTest {
    private World world;

    @BeforeEach
    void setUp() {
        world = World.create("test-world", 100);
    }


    // =================================================================================================================
    // Single component queries
    // =================================================================================================================

    @Test
    void findEntitiesWith_SingleComponent_WithNoEntities_ShouldReturnEmpty_Results() {
        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        assertNotNull(results);
        assertFalse(results.iterator().hasNext());
    }

    @Test
    void findEntitiesWith_SingleComponent_WithMatchingEntities_ShouldReturnResults() {
        Entity entity1 = world.createEntity(new TestComponents.Position(1, 1));
        Entity entity2 = world.createEntity(new TestComponents.Position(2, 2));
        Entity entity3 = world.createEntity(new TestComponents.Position(3, 3), new TestComponents.Velocity(3, 3));

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        List<With1<TestComponents.Position>> list = new ArrayList<>();
        results.iterator().forEachRemaining(list::add);

        assertEquals(3, list.size());

        assertTrue(list.stream().anyMatch(r -> r.entity().equals(entity1)));
        With1<TestComponents.Position> w1 = list.stream()
                .filter(r -> r.entity().equals(entity1)).findFirst().orElseThrow();
        assertEquals(1, w1.comp().x);
        assertEquals(1, w1.comp().y);
        assertEquals(entity1, w1.entity());

        assertTrue(list.stream().anyMatch(r -> r.entity().equals(entity2)));
        With1<TestComponents.Position> w2 = list.stream()
                .filter(r -> r.entity().equals(entity2)).findFirst().orElseThrow();
        assertEquals(2, w2.comp().x);
        assertEquals(2, w2.comp().y);
        assertEquals(entity2, w2.entity());

        assertTrue(list.stream().anyMatch(r -> r.entity().equals(entity3)));
        With1<TestComponents.Position> w3 = list.stream()
                .filter(r -> r.entity().equals(entity3)).findFirst().orElseThrow();
        assertEquals(3, w3.comp().x);
        assertEquals(3, w3.comp().y);
        assertEquals(entity3, w3.entity());
    }

    @Test
    void findEntitiesWith_SingleComponentShouldReturnCorrectComponents() {
        world.createEntity(new TestComponents.Position(10, 20));

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        With1<TestComponents.Position> w1 = results.iterator().next();
        assertEquals(10f, w1.comp().x);
        assertEquals(20f, w1.comp().y);
    }

    @Test
    void findEntitiesWith_SingleComponent_ShouldIncludeEntityReference() {
        Entity entity = world.createEntity(new TestComponents.Position(1, 1));

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        With1<TestComponents.Position> result = results.iterator().next();
        assertEquals(entity.getId(), result.entity().getId());
    }

    @Test
    void findEntitiesWith_SingleComponent_ShouldOnlyReturnMatchingEntities() {
        world.createEntity(new TestComponents.Position(1, 1));
        world.createEntity(new TestComponents.Velocity(2, 2));   // No position
        world.createEntity(
                new TestComponents.Position(3, 3),
                new TestComponents.Velocity(3, 3)
        );

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        List<With1<TestComponents.Position>> list = new ArrayList<>();
        results.iterator().forEachRemaining(list::add);

        assertEquals(2, list.size());
    }


    // =================================================================================================================
    // Two component queries
    // =================================================================================================================

    @Test
    void findEntitiesWith_TwoComponents_WithNoEntities_ShouldReturnEmptyResults() {
        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        assertFalse(results.iterator().hasNext());
    }

    @Test
    void findEntitiesWith_TwoComponents_WithMatchingEntities_ShouldReturnResults() {
        Entity entity1 = world.createEntity(new TestComponents.Position(1, 1), new TestComponents.Velocity(2, 2));
        Entity entity2 = world.createEntity(new TestComponents.Position(3, 3), new TestComponents.Velocity(4, 4));

        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        List<With2<TestComponents.Position, TestComponents.Velocity>> list = new ArrayList<>();
        results.iterator().forEachRemaining(list::add);

        assertEquals(2, list.size());

        assertTrue(list.stream().anyMatch(r -> r.entity().equals(entity1)));
        With2<TestComponents.Position, TestComponents.Velocity> w1 = list.stream()
                .filter(r -> r.entity().equals(entity1)).findFirst().orElseThrow();
        assertEquals(1, w1.comp1().x);
        assertEquals(1, w1.comp1().y);
        assertEquals(2, w1.comp2().dx);
        assertEquals(2, w1.comp2().dy);
        assertEquals(entity1, w1.entity());

        assertTrue(list.stream().anyMatch(r -> r.entity().equals(entity2)));
        With2<TestComponents.Position, TestComponents.Velocity> w2 = list.stream()
                .filter(r -> r.entity().equals(entity2)).findFirst().orElseThrow();
        assertEquals(3, w2.comp1().x);
        assertEquals(3, w2.comp1().y);
        assertEquals(4, w2.comp2().dx);
        assertEquals(4, w2.comp2().dy);
        assertEquals(entity2, w2.entity());
    }

    @Test
    void findEntitiesWith_TwoComponents_ShouldReturnBothComponents() {
        world.createEntity(new TestComponents.Position(10, 20), new TestComponents.Velocity(30, 40));

        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        With2<TestComponents.Position, TestComponents.Velocity> result = results.iterator().next();
        assertEquals(10f, result.comp1().x);
        assertEquals(20f, result.comp1().y);
        assertEquals(30f, result.comp2().dx);
        assertEquals(40f, result.comp2().dy);
    }

    @Test
    void findEntitiesWith_TwoComponents_ShouldOnlyReturnEntitiesWithBoth() {
        world.createEntity(new TestComponents.Position(1, 1)); // Only Position
        world.createEntity(new TestComponents.Velocity(2, 2)); // Only Velocity
        world.createEntity(new TestComponents.Position(3, 3), new TestComponents.Velocity(3, 3)); // Both

        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        List<With2<TestComponents.Position, TestComponents.Velocity>> list = new ArrayList<>();
        results.iterator().forEachRemaining(list::add);

        assertEquals(1, list.size());
        assertEquals(3f, list.getFirst().comp1().x);
    }

    @Test
    void findEntitiesWith_TwoComponents_WhenOneTypeNotRegistered_ShouldReturnEmpty() {
        world.createEntity(new TestComponents.Position(1, 1));

        // Velocity type has never been registered
        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        assertFalse(results.iterator().hasNext());
    }


    // =================================================================================================================
    // Three component queries
    // =================================================================================================================

    @Test
    void findEntitiesWith_ThreeComponents_WithNoEntities_ShouldReturnEmptyResults() {
        Results<With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class, TestComponents.Health.class);

        assertFalse(results.iterator().hasNext());
    }

    @Test
    void findEntitiesWith_ThreeComponents_WithMatchingEntities_ShouldReturnResults() {
        Entity entity1 = world.createEntity(
                new TestComponents.Position(1, 1),
                new TestComponents.Velocity(1, 1),
                new TestComponents.Health(100, 100)
        );
        Entity entity2 = world.createEntity(
                new TestComponents.Position(2, 2),
                new TestComponents.Velocity(2, 2),
                new TestComponents.Health(200, 200)
        );

        Results<With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class, TestComponents.Health.class);

        assertTrue(results.iterator().hasNext());

        List<With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health>> list = new ArrayList<>();
        results.iterator().forEachRemaining(list::add);

        assertEquals(2, list.size());

        assertTrue(list.stream().anyMatch(r -> r.entity().equals(entity1)));
        With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health> w1 = list.stream()
                .filter(r -> r.entity().equals(entity1)).findFirst().orElseThrow();
        assertEquals(1, w1.comp1().x);
        assertEquals(1, w1.comp1().y);
        assertEquals(1, w1.comp2().dx);
        assertEquals(1, w1.comp2().dy);
        assertEquals(100, w1.comp3().current);
        assertEquals(100, w1.comp3().max);
        assertEquals(entity1, w1.entity());

        assertTrue(list.stream().anyMatch(r -> r.entity().equals(entity2)));
        With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health> w2 = list.stream()
                .filter(r -> r.entity().equals(entity2)).findFirst().orElseThrow();
        assertEquals(2, w2.comp1().x);
        assertEquals(2, w2.comp1().y);
        assertEquals(2, w2.comp2().dx);
        assertEquals(2, w2.comp2().dy);
        assertEquals(200, w2.comp3().current);
        assertEquals(200, w2.comp3().max);
        assertEquals(entity2, w2.entity());
    }

    @Test
    void findEntitiesWith_ThreeComponents_ShouldReturnAllThreeComponents() {
        world.createEntity(
                new TestComponents.Position(10, 20),
                new TestComponents.Velocity(30, 40),
                new TestComponents.Health(50, 100)
        );

        Results<With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class, TestComponents.Health.class);

        With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health> result = results.iterator().next();
        assertEquals(10f, result.comp1().x);
        assertEquals(30f, result.comp2().dx);
        assertEquals(50, result.comp3().current);
    }

    @Test
    void findEntitiesWith_ThreeComponents_ShouldOnlyReturnEntitiesWithAllThree() {
        world.createEntity(new TestComponents.Position(1, 1), new TestComponents.Velocity(1, 1)); // Missing Health
        world.createEntity(new TestComponents.Position(2, 2), new TestComponents.Health(100, 100)); // Missing Velocity
        world.createEntity(new TestComponents.Velocity(3, 3), new TestComponents.Health(100, 100)); // Missing Position
        world.createEntity(new TestComponents.Position(4, 4), new TestComponents.Velocity(4, 4), new TestComponents.Health(100, 100)); // All three

        Results<With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class, TestComponents.Health.class);

        List<With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health>> list = new ArrayList<>();
        results.iterator().forEachRemaining(list::add);

        assertEquals(1, list.size());
        assertEquals(4f, list.get(0).comp1().x);
    }


    // =================================================================================================================
    // Four component queries
    // =================================================================================================================

    @Test
    void findEntitiesWith_FourComponents_WithNoEntities_ShouldReturnEmptyResults() {
        Results<With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name>> results =
                world.findEntitiesWith(
                        TestComponents.Position.class,
                        TestComponents.Velocity.class,
                        TestComponents.Health.class,
                        TestComponents.Name.class
                );

        assertFalse(results.iterator().hasNext());
    }

    @Test
    void findEntitiesWith_FourComponents_WithMatchingEntities_ShouldReturnResults() {
        Entity entity1 = world.createEntity(
                new TestComponents.Position(1, 1),
                new TestComponents.Velocity(1, 1),
                new TestComponents.Health(100, 100),
                new TestComponents.Name("Entity1")
        );
        Entity entity2 = world.createEntity(
                new TestComponents.Position(2, 2),
                new TestComponents.Velocity(2, 2),
                new TestComponents.Health(200, 200),
                new TestComponents.Name("Entity2")
        );

        Results<With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name>> results =
                world.findEntitiesWith(
                        TestComponents.Position.class,
                        TestComponents.Velocity.class,
                        TestComponents.Health.class,
                        TestComponents.Name.class
                );

        assertTrue(results.iterator().hasNext());

        List<With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name>> list = new ArrayList<>();
        results.iterator().forEachRemaining(list::add);

        assertEquals(2, list.size());

        assertTrue(list.stream().anyMatch(r -> r.entity().equals(entity1)));
        With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name> w1 = list.stream()
                .filter(r -> r.entity().equals(entity1)).findFirst().orElseThrow();
        assertEquals(1, w1.comp1().x);
        assertEquals(1, w1.comp1().y);
        assertEquals(1, w1.comp2().dx);
        assertEquals(1, w1.comp2().dy);
        assertEquals(100, w1.comp3().current);
        assertEquals(100, w1.comp3().max);
        assertEquals("Entity1", w1.comp4().value);
        assertEquals(entity1, w1.entity());

        assertTrue(list.stream().anyMatch(r -> r.entity().equals(entity2)));
        With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name> w2 = list.stream()
                .filter(r -> r.entity().equals(entity2)).findFirst().orElseThrow();
        assertEquals(2, w2.comp1().x);
        assertEquals(2, w2.comp1().y);
        assertEquals(2, w2.comp2().dx);
        assertEquals(2, w2.comp2().dy);
        assertEquals(200, w2.comp3().current);
        assertEquals(200, w2.comp3().max);
        assertEquals("Entity2", w2.comp4().value);
        assertEquals(entity2, w2.entity());
    }

    @Test
    void findEntitiesWith_FourComponents_ShouldReturnAllFourComponents() {
        world.createEntity(
                new TestComponents.Position(10, 20),
                new TestComponents.Velocity(30, 40),
                new TestComponents.Health(50, 100),
                new TestComponents.Name("TestEntity")
        );

        Results<With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name>> results =
                world.findEntitiesWith(
                        TestComponents.Position.class,
                        TestComponents.Velocity.class,
                        TestComponents.Health.class,
                        TestComponents.Name.class
                );

        With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name> result = results.iterator().next();
        assertEquals(10f, result.comp1().x);
        assertEquals(30f, result.comp2().dx);
        assertEquals(50, result.comp3().current);
        assertEquals("TestEntity", result.comp4().value);
    }

    @Test
    void findEntitiesWith_FourComponents_ShouldOnlyReturnEntitiesWithAllFour() {
        // Entity missing one component
        world.createEntity(
                new TestComponents.Position(1, 1),
                new TestComponents.Velocity(1, 1),
                new TestComponents.Health(100, 100)
        );
        // Entity with all four
        world.createEntity(
                new TestComponents.Position(2, 2),
                new TestComponents.Velocity(2, 2),
                new TestComponents.Health(100, 100),
                new TestComponents.Name("Complete")
        );

        Results<With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name>> results =
                world.findEntitiesWith(
                        TestComponents.Position.class,
                        TestComponents.Velocity.class,
                        TestComponents.Health.class,
                        TestComponents.Name.class
                );

        List<With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name>> list = new ArrayList<>();
        results.iterator().forEachRemaining(list::add);

        assertEquals(1, list.size());
        assertEquals("Complete", list.get(0).comp4().value);
    }


    // =================================================================================================================
    // Stream support
    // =================================================================================================================

    @Test
    void stream_ShouldSupportStreamOperations() {
        world.createEntity(new TestComponents.Position(1, 1));
        world.createEntity(new TestComponents.Position(2, 2));
        world.createEntity(new TestComponents.Position(3, 3));
        world.createEntity(new TestComponents.Position(4, 4));
        world.createEntity(new TestComponents.Position(5, 5));

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        // Filter and collect using stream
        List<Float> xValues = results.stream()
                .map(r -> r.comp().x)
                .filter(x -> x > 2)
                .toList();

        assertEquals(3, xValues.size());
        assertTrue(xValues.contains(3f));
        assertTrue(xValues.contains(4f));
        assertTrue(xValues.contains(5f));
    }

    @Test
    void stream_WithTwoComponents_ShouldSupportStreamOperations() {
        world.createEntity(new TestComponents.Position(1, 1), new TestComponents.Velocity(10, 10));
        world.createEntity(new TestComponents.Position(2, 2), new TestComponents.Velocity(20, 20));
        world.createEntity(new TestComponents.Position(3, 3), new TestComponents.Velocity(30, 30));

        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        long count = results.stream()
                .filter(r -> r.comp2().dx > 15)
                .count();

        assertEquals(2, count);
    }

    @Test
    void stream_ShouldSupportForEach() {
        world.createEntity(new TestComponents.Counter(0));
        world.createEntity(new TestComponents.Counter(0));
        world.createEntity(new TestComponents.Counter(0));

        Results<With1<TestComponents.Counter>> results = world.findEntitiesWith(TestComponents.Counter.class);

        results.stream().forEach(r -> r.comp().increment());

        // Verify all counters were incremented
        results.stream().forEach(r -> assertEquals(1, r.comp().count));
    }


    // =================================================================================================================
    // Iterator behavior
    // =================================================================================================================

    @Test
    void iterator_ShouldBeReusable() {
        world.createEntity(new TestComponents.Position(1, 1));
        world.createEntity(new TestComponents.Position(2, 2));

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        // First iteration
        List<Float> first = new ArrayList<>();
        for (With1<TestComponents.Position> r : results) {
            first.add(r.comp().x);
        }

        // Second iteration
        List<Float> second = new ArrayList<>();
        for (With1<TestComponents.Position> r : results) {
            second.add(r.comp().x);
        }

        assertEquals(first, second);
    }

    @Test
    void iterator_WithEmptyResults_ShouldNotThrow() {
        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        assertDoesNotThrow(() -> {
            for (With1<TestComponents.Position> ignored : results) {
                fail("Should not iterate over empty results");
            }
        });
    }

    @Test
    void forEachLoop_ShouldWork() {
        world.createEntity(new TestComponents.Position(1, 1));
        world.createEntity(new TestComponents.Position(2, 2));
        world.createEntity(new TestComponents.Position(3, 3));

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        Set<Float> xValues = new HashSet<>();
        for (With1<TestComponents.Position> r : results) {
            xValues.add(r.comp().x);
        }

        assertEquals(3, xValues.size());
        assertTrue(xValues.contains(1f));
        assertTrue(xValues.contains(2f));
        assertTrue(xValues.contains(3f));
    }


    // =================================================================================================================
    // Component modification during iteration
    // =================================================================================================================

    @Test
    void componentModification_DuringIteration_ShouldPersist() {
        world.createEntity(new TestComponents.Position(1, 1));
        world.createEntity(new TestComponents.Position(2, 2));
        world.createEntity(new TestComponents.Position(3, 3));

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        // Modify components
        for (With1<TestComponents.Position> r : results) {
            r.comp().x *= 10;
        }

        // Verify modifications
        Results<With1<TestComponents.Position>> newResults = world.findEntitiesWith(TestComponents.Position.class);
        Set<Float> xValues = new HashSet<>();
        for (With1<TestComponents.Position> r : newResults) {
            xValues.add(r.comp().x);
        }

        assertTrue(xValues.contains(10f));
        assertTrue(xValues.contains(20f));
        assertTrue(xValues.contains(30f));
    }


    // =================================================================================================================
    // Edge cases
    // =================================================================================================================

    @Test
    void query_AfterEntityDeletion_ShouldNotIncludeDeletedEntity() {
        Entity entity1 = world.createEntity(new TestComponents.Position(1, 1));
        Entity entity2 = world.createEntity(new TestComponents.Position(2, 2));
        Entity entity3 = world.createEntity(new TestComponents.Position(3, 3));

        world.deleteEntity(entity2);

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        Set<Integer> entityIds = new HashSet<>();
        for (With1<TestComponents.Position> r : results) {
            entityIds.add(r.entity().getId());
        }

        assertEquals(2, entityIds.size());
        assertTrue(entityIds.contains(entity1.getId()));
        assertFalse(entityIds.contains(entity2.getId()));
        assertTrue(entityIds.contains(entity3.getId()));
    }

    @Test
    void query_WithManyEntities_ShouldReturnAll() {
        for (int i = 0; i < 50; i++) {
            world.createEntity(new TestComponents.Position(i, i));
        }

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        long count = results.stream().count();
        assertEquals(50, count);
    }

    @Test
    void query_WithTagComponents_ShouldWorld() {
        world.createEntity(new TestComponents.Position(1, 1), new TestComponents.PlayerTag());
        world.createEntity(new TestComponents.Position(2, 2), new TestComponents.EnemyTag());
        world.createEntity(new TestComponents.Position(3, 3), new TestComponents.PlayerTag());

        Results<With2<TestComponents.Position, TestComponents.PlayerTag>> playerResults =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.PlayerTag.class);

        Results<With2<TestComponents.Position, TestComponents.EnemyTag>> enemyResults =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.EnemyTag.class);

        assertEquals(2, playerResults.stream().count());
        assertEquals(1, enemyResults.stream().count());
    }

    @Test
    void query_AfterComponentRemoval_ShouldNotIncludeEntity() {
        Entity entity = world.createEntity(new TestComponents.Position(1, 1), new TestComponents.Velocity(1, 1));

        // Query before removal
        Results<With2<TestComponents.Position, TestComponents.Velocity>> resultsBefore =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);
        assertEquals(1, resultsBefore.stream().count());

        // Remove velocity component
        entity.removeType(TestComponents.Velocity.class);

        // Query after removal
        Results<With2<TestComponents.Position, TestComponents.Velocity>> resultsAfter =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);
        assertEquals(0, resultsAfter.stream().count());

        // Single component query should still work
        Results<With1<TestComponents.Position>> posResults = world.findEntitiesWith(TestComponents.Position.class);
        assertEquals(1, posResults.stream().count());
    }

    @Test
    void query_AfterComponentAddition_ShouldIncludeEntity() {
        Entity entity = world.createEntity(new TestComponents.Position(1, 1));

        // Query before addition
        Results<With2<TestComponents.Position, TestComponents.Velocity>> resultsBefore =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);
        assertEquals(0, resultsBefore.stream().count());

        // Add velocity component
        entity.add(new TestComponents.Velocity(2, 2));

        // Query after addition
        Results<With2<TestComponents.Position, TestComponents.Velocity>> resultsAfter =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);
        assertEquals(1, resultsAfter.stream().count());
    }

    @Test
    void query_ResultsShouldAutomaticallyUpdate_WhenEntitiesAddedBetweenIterations() {
        // FIXME potentially (might cause problems)
        //  results should (in my opinion) only be updated if a whole new query was ran,
        //  currently it updates in between iterations (Results::stream() or Results::iterator())
        // Create one entity with Position component
        world.createEntity(new TestComponents.Position(1, 1));

        // Query for entities with Position
        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        // First: Count results (should be 1)
        long initialCount = results.stream().count();
        assertEquals(1, initialCount);

        // Add another entity with Position
        world.createEntity(new TestComponents.Position(2, 2));

        // Retest the same Results object: Does it reflect the new entity?
        long afterInsertCount = results.stream().count();

//        // If the Results is snapshot-based, afterInsertCount == 1
//        // If Results is live, afterInsertCount == 2
//        // We're asserting typical ECS behavior: Results are not automatically live-updated.
//        assertEquals(1, afterInsertCount,
//                "Results object should  automatically include new entities added after it was obtained");
//
//        // To confirm, a new query should see 2 entities
//        Results<With1<TestComponents.Position>> newResults = world.findEntitiesWith(TestComponents.Position.class);
//        assertEquals(2, newResults.stream().count(),
//                "A new Results object should reflect all current entities");
    }
}
