package me.siebe.flux.api.ecs;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Represents the results of an entity query operation.
 * <p>
 * Results provide an iterable collection of query matches, where each match
 * contains the components and entity that matched the query criteria.
 * Results can be iterated directly or converted to a Stream for functional
 * operations.
 *
 * @param <T> the type of results record ({@link Results.With1 With1}, {@link Results.With2 With2},
 *            {@link Results.With3 With3}, or {@link Results.With4 With4})
 */
public interface Results<T> extends Iterable<T> {
    /**
     * Returns an iterator from the query results.
     *
     * @return an Iterator over the results
     */
    @Override
    Iterator<T> iterator();

    /**
     * Returns a Stream of the query results.
     * <p>
     * This allows for functional-style operations on th results, such as
     * filtering, mapping, and collecting.
     *
     * @return a Stream of the results
     */
    Stream<T> stream();

    /**
     * A result record containing a single component and its entity.
     *
     * @param <T>    the component type
     * @param comp   the component instance
     * @param entity the entity that has this component
     */
    record With1<T>(T comp, Entity entity) {
    }


    /**
     * A result record containing two components and their entity.
     *
     * @param <T1>   the first component type
     * @param <T2>   the second component type
     * @param comp1  the first component instance
     * @param comp2  the second component instance
     * @param entity the entity that has both components
     */
    record With2<T1, T2>(T1 comp1, T2 comp2, Entity entity) {
    }

    /**
     * A result record containing three components and their entity.
     *
     * @param <T1>   the first component type
     * @param <T2>   the second component type
     * @param <T3>   the third component type
     * @param comp1  the first component instance
     * @param comp2  the second component instance
     * @param comp3  the third component instance
     * @param entity the entity that has all three components
     */
    record With3<T1, T2, T3>(T1 comp1, T2 comp2, T3 comp3, Entity entity) {
    }

    /**
     * A result record containing four components and their entity.
     *
     * @param <T1>   the first component type
     * @param <T2>   the second component type
     * @param <T3>   the third component type
     * @param <T4>   the fourth component type
     * @param comp1  the first component instance
     * @param comp2  the second component instance
     * @param comp3  the third component instance
     * @param comp4  the fourth component instance
     * @param entity the entity that has all four components
     */
    record With4<T1, T2, T3, T4>(T1 comp1, T2 comp2, T3 comp3, T4 comp4, Entity entity) {
    }
}
