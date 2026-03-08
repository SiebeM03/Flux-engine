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

import static me.siebe.flux.test.assertions.ECSTestAssertions.*;
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

        assertResultsEmpty(results);
    }

    @Test
    void findEntitiesWith_SingleComponent_WithMatchingEntities_ShouldReturnResults() {
        TestComponents.Position entity1Position = new TestComponents.Position(1, 1);
        Entity entity1 = world.createEntity(entity1Position);

        TestComponents.Position entity2Position = new TestComponents.Position(2, 2);
        Entity entity2 = world.createEntity(entity2Position);

        TestComponents.Position entity3Position = new TestComponents.Position(3, 3);
        TestComponents.Velocity entity3Velocity = new TestComponents.Velocity(3, 3);
        Entity entity3 = world.createEntity(entity3Position, entity3Velocity);

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);
        assertResultCount(results, 3);

        assertResultContainsEntity(results, entity1, r -> {
            assertEquals(entity1Position, r.comp());
            assertEquals(entity1, r.entity());
        });
        assertResultContainsEntity(results, entity2, r -> {
            assertEquals(entity2Position, r.comp());
            assertEquals(entity2, r.entity());
        });
        assertResultContainsEntity(results, entity3, r -> {
            assertEquals(entity3Position, r.comp());
            assertEquals(entity3, r.entity());
        });
    }

    @Test
    void findEntitiesWith_SingleComponentShouldReturnCorrectComponents() {
        world.createEntity(new TestComponents.Position(10, 20));

        assertOneEntityWith(world, TestComponents.Position.class, pos -> {
            assertEquals(10f, pos.x);
            assertEquals(20f, pos.y);
        });
    }

    @Test
    void findEntitiesWith_SingleComponent_ShouldIncludeEntityReference() {
        Entity entity = world.createEntity(new TestComponents.Position(1, 1));

        Results<With1<TestComponents.Position>> results = world.findEntitiesWith(TestComponents.Position.class);

        assertSingleResult(results, r -> {
            assertEquals(entity.getId(), r.entity().getId());
        });
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

        assertResultCount(results, 2);
    }


    // =================================================================================================================
    // Two component queries
    // =================================================================================================================

    @Test
    void findEntitiesWith_TwoComponents_WithNoEntities_ShouldReturnEmptyResults() {
        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        assertResultsEmpty(results);
    }

    @Test
    void findEntitiesWith_TwoComponents_WithMatchingEntities_ShouldReturnResults() {
        TestComponents.Position entity1Position = new TestComponents.Position(1, 1);
        TestComponents.Velocity entity1Velocity = new TestComponents.Velocity(2, 2);
        Entity entity1 = world.createEntity(entity1Position, entity1Velocity);

        TestComponents.Position entity2Position = new TestComponents.Position(3, 3);
        TestComponents.Velocity entity2Velocity = new TestComponents.Velocity(4, 4);
        Entity entity2 = world.createEntity(entity2Position, entity2Velocity);

        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        assertResultCount(results, 2);

        assertResultContainsEntity(results, entity1, r -> {
            assertEquals(entity1Position, r.comp1());
            assertEquals(entity1Velocity, r.comp2());
            assertEquals(entity1, r.entity());
        });
        assertResultContainsEntity(results, entity2, r -> {
            assertEquals(entity2Position, r.comp1());
            assertEquals(entity2Velocity, r.comp2());
            assertEquals(entity2, r.entity());
        });
    }

    @Test
    void findEntitiesWith_TwoComponents_ShouldReturnBothComponents() {
        world.createEntity(new TestComponents.Position(10, 20), new TestComponents.Velocity(30, 40));

        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        assertSingleResult(results, r -> {
            assertEquals(10f, r.comp1().x);
            assertEquals(20f, r.comp1().y);
            assertEquals(30f, r.comp2().dx);
            assertEquals(40f, r.comp2().dy);
        });
    }

    @Test
    void findEntitiesWith_TwoComponents_ShouldOnlyReturnEntitiesWithBoth() {
        world.createEntity(new TestComponents.Position(1, 1)); // Only Position
        world.createEntity(new TestComponents.Velocity(2, 2)); // Only Velocity
        world.createEntity(new TestComponents.Position(3, 3), new TestComponents.Velocity(3, 3)); // Both

        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        assertSingleResult(results, r -> {
            assertEquals(3f, r.comp1().x);
        });
    }

    @Test
    void findEntitiesWith_TwoComponents_WhenOneTypeNotRegistered_ShouldReturnEmpty() {
        world.createEntity(new TestComponents.Position(1, 1));

        // Velocity type has never been registered
        Results<With2<TestComponents.Position, TestComponents.Velocity>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);

        assertResultsEmpty(results);
    }


    // =================================================================================================================
    // Three component queries
    // =================================================================================================================

    @Test
    void findEntitiesWith_ThreeComponents_WithNoEntities_ShouldReturnEmptyResults() {
        Results<With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class, TestComponents.Health.class);

        assertResultsEmpty(results);
    }

    @Test
    void findEntitiesWith_ThreeComponents_WithMatchingEntities_ShouldReturnResults() {
        TestComponents.Position entity1Position = new TestComponents.Position(1, 1);
        TestComponents.Velocity entity1Velocity = new TestComponents.Velocity(1, 1);
        TestComponents.Health entity1Health = new TestComponents.Health(100, 100);
        Entity entity1 = world.createEntity(entity1Position, entity1Velocity, entity1Health);

        TestComponents.Position entity2Position = new TestComponents.Position(2, 2);
        TestComponents.Velocity entity2Velocity = new TestComponents.Velocity(2, 2);
        TestComponents.Health entity2Health = new TestComponents.Health(200, 200);
        Entity entity2 = world.createEntity(entity2Position, entity2Velocity, entity2Health);

        Results<With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class, TestComponents.Health.class);

        assertResultCount(results, 2);

        assertResultContainsEntity(results, entity1, r -> {
            assertEquals(entity1Position, r.comp1());
            assertEquals(entity1Velocity, r.comp2());
            assertEquals(entity1Health, r.comp3());
            assertEquals(entity1, r.entity());
        });
        assertResultContainsEntity(results, entity2, r -> {
            assertEquals(entity2Position, r.comp1());
            assertEquals(entity2Velocity, r.comp2());
            assertEquals(entity2Health, r.comp3());
            assertEquals(entity2, r.entity());
        });
    }

    @Test
    void findEntitiesWith_ThreeComponents_ShouldReturnAllThreeComponents() {
        TestComponents.Position position = new TestComponents.Position(10, 20);
        TestComponents.Velocity velocity = new TestComponents.Velocity(30, 40);
        TestComponents.Health health = new TestComponents.Health(50, 100);
        world.createEntity(position, velocity, health);

        Results<With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class, TestComponents.Health.class);

        assertSingleResult(results, r -> {
            assertEquals(position, r.comp1());
            assertEquals(velocity, r.comp2());
            assertEquals(health, r.comp3());
        });
    }

    @Test
    void findEntitiesWith_ThreeComponents_ShouldOnlyReturnEntitiesWithAllThree() {
        world.createEntity(new TestComponents.Position(1, 1), new TestComponents.Velocity(1, 1)); // Missing Health
        world.createEntity(new TestComponents.Position(2, 2), new TestComponents.Health(100, 100)); // Missing Velocity
        world.createEntity(new TestComponents.Velocity(3, 3), new TestComponents.Health(100, 100)); // Missing Position
        world.createEntity(new TestComponents.Position(4, 4), new TestComponents.Velocity(4, 4), new TestComponents.Health(100, 100)); // All three

        Results<With3<TestComponents.Position, TestComponents.Velocity, TestComponents.Health>> results =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class, TestComponents.Health.class);

        assertSingleResult(results, r -> {
            assertEquals(4f, r.comp1().x);
        });
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

        assertResultsEmpty(results);
    }

    @Test
    void findEntitiesWith_FourComponents_WithMatchingEntities_ShouldReturnResults() {
        TestComponents.Position entity1Position = new TestComponents.Position(1, 1);
        TestComponents.Velocity entity1Velocity = new TestComponents.Velocity(1, 1);
        TestComponents.Health entity1Health = new TestComponents.Health(100, 100);
        TestComponents.Name entity1Name = new TestComponents.Name("entity1");
        Entity entity1 = world.createEntity(entity1Position, entity1Velocity, entity1Health, entity1Name);

        TestComponents.Position entity2Position = new TestComponents.Position(2, 2);
        TestComponents.Velocity entity2Velocity = new TestComponents.Velocity(2, 2);
        TestComponents.Health entity2Health = new TestComponents.Health(200, 200);
        TestComponents.Name entity2Name = new TestComponents.Name("entity2");
        Entity entity2 = world.createEntity(entity2Position, entity2Velocity, entity2Health, entity2Name);

        Results<With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name>> results =
                world.findEntitiesWith(
                        TestComponents.Position.class,
                        TestComponents.Velocity.class,
                        TestComponents.Health.class,
                        TestComponents.Name.class
                );

        assertResultCount(results, 2);

        assertResultContainsEntity(results, entity1, r -> {
            assertEquals(entity1Position, r.comp1());
            assertEquals(entity1Velocity, r.comp2());
            assertEquals(entity1Health, r.comp3());
            assertEquals(entity1Name, r.comp4());
            assertEquals(entity1, r.entity());
        });
        assertResultContainsEntity(results, entity2, r -> {
            assertEquals(entity2Position, r.comp1());
            assertEquals(entity2Velocity, r.comp2());
            assertEquals(entity2Health, r.comp3());
            assertEquals(entity2Name, r.comp4());
            assertEquals(entity2, r.entity());
        });
    }

    @Test
    void findEntitiesWith_FourComponents_ShouldReturnAllFourComponents() {
        TestComponents.Position position = new TestComponents.Position(10, 20);
        TestComponents.Velocity velocity = new TestComponents.Velocity(30, 40);
        TestComponents.Health health = new TestComponents.Health(50, 100);
        TestComponents.Name name = new TestComponents.Name("TestEntity");
        world.createEntity(position, velocity, health, name);

        Results<With4<TestComponents.Position, TestComponents.Velocity, TestComponents.Health, TestComponents.Name>> results =
                world.findEntitiesWith(
                        TestComponents.Position.class,
                        TestComponents.Velocity.class,
                        TestComponents.Health.class,
                        TestComponents.Name.class
                );

        assertSingleResult(results, r -> {
            assertEquals(position, r.comp1());
            assertEquals(velocity, r.comp2());
            assertEquals(health, r.comp3());
            assertEquals(name, r.comp4());
        });
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

        assertSingleResult(results, r -> {
            assertEquals("Complete", r.comp4().value);
        });
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
        assertResultCount(results, 2);

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

        assertResultCount(results, 50);
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

        assertResultCount(playerResults, 2);
        assertResultCount(enemyResults, 1);
    }

    @Test
    void query_AfterComponentRemoval_ShouldNotIncludeEntity() {
        Entity entity = world.createEntity(new TestComponents.Position(1, 1), new TestComponents.Velocity(1, 1));

        // Query before removal
        Results<With2<TestComponents.Position, TestComponents.Velocity>> resultsBefore =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);
        assertResultCount(resultsBefore, 1);

        // Remove velocity component
        entity.removeType(TestComponents.Velocity.class);

        // Query after removal
        Results<With2<TestComponents.Position, TestComponents.Velocity>> resultsAfter =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);
        assertResultsEmpty(resultsAfter);

        // Single component query should still work
        Results<With1<TestComponents.Position>> posResults = world.findEntitiesWith(TestComponents.Position.class);
        assertResultCount(posResults, 1);
    }

    @Test
    void query_AfterComponentAddition_ShouldIncludeEntity() {
        Entity entity = world.createEntity(new TestComponents.Position(1, 1));

        // Query before addition
        Results<With2<TestComponents.Position, TestComponents.Velocity>> resultsBefore =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);
        assertResultsEmpty(resultsBefore);

        // Add velocity component
        entity.add(new TestComponents.Velocity(2, 2));

        // Query after addition
        Results<With2<TestComponents.Position, TestComponents.Velocity>> resultsAfter =
                world.findEntitiesWith(TestComponents.Position.class, TestComponents.Velocity.class);
        assertResultCount(resultsAfter, 1);
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
        assertResultCount(results, 1);

        // Add another entity with Position
        world.createEntity(new TestComponents.Position(2, 2));

        // Retest the same Results object: Does it reflect the new entity?
        assertResultCount(results, 2);

        // // If the Results is snapshot-based, afterInsertCount == 1
        // // If Results is live, afterInsertCount == 2
        // // We're asserting typical ECS behavior: Results are not automatically live-updated.
        // assertEquals(1, afterInsertCount, "Results object should  automatically include new entities added after it was obtained");
        //
        // // To confirm, a new query should see 2 entities
        // Results<With1<TestComponents.Position>> newResults = world.findEntitiesWith(TestComponents.Position.class);
        // assertEquals(2, newResults.stream().count(), "A new Results object should reflect all current entities");
    }
}
