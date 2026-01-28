package me.siebe.flux.ecs;

import me.siebe.flux.api.ecs.Results;
import me.siebe.flux.api.ecs.Results.With1;
import me.siebe.flux.api.ecs.Results.With2;
import me.siebe.flux.api.ecs.Results.With3;
import me.siebe.flux.api.ecs.Results.With4;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides query functionality for finding entities with specific component combinations.
 * <p>
 * This class implements efficient entity queries by iterating over component stores
 * and finding entities that match the query criteria. Queries support finding entities
 * with 1, 2, 3, or 4 component types (for now).
 * <p>
 * This is an internal utility class and should not be used directly
 */
final class WorldQuery {
    /**
     * Finds all entities that have a component of the specified type.
     *
     * @param <T>   the component type
     * @param world the world to query
     * @param type  the component class to search for
     * @return a Results object containing all matching entities
     */
    static <T> Results<With1<T>> findEntitiesWith(
            final SimpleWorld world,
            Class<T> type
    ) {
        ComponentStore<T> store = world.getComponentRegistry().getComponentStore(type);
        if (store == null) {
            return new ResultSet<>(EmptyIterator::new);
        }
        return new ResultSet<>(() -> new SingleComponentIterator<>(world, store));
    }

    /**
     * Finds all entities that have both of the specified component types.
     *
     * @param <T1>  the first component type
     * @param <T2>  the second component type
     * @param world the world to query
     * @param type1 the first component class
     * @param type2 the second component class
     * @return a Results object containing all matching entities
     */
    static <T1, T2> Results<With2<T1, T2>> findEntitiesWith(
            final SimpleWorld world,
            Class<T1> type1,
            Class<T2> type2
    ) {
        ComponentStore<T1> store1 = world.getComponentRegistry().getComponentStore(type1);
        ComponentStore<T2> store2 = world.getComponentRegistry().getComponentStore(type2);
        if (store1 == null || store2 == null) {
            return new ResultSet<>(EmptyIterator::new);
        }
        return new ResultSet<>(() -> new TwoComponentIterator<>(world, store1, store2));
    }

    /**
     * Finds all entities that have all three of the specified component types.
     *
     * @param <T1>  the first component type
     * @param <T2>  the second component type
     * @param <T3>  the third component type
     * @param world the world to query
     * @param type1 the first component class
     * @param type2 the second component class
     * @param type3 the third component class
     * @return a Results object containing all matching entities
     */
    static <T1, T2, T3> Results<With3<T1, T2, T3>> findEntitiesWith(
            final SimpleWorld world,
            Class<T1> type1,
            Class<T2> type2,
            Class<T3> type3
    ) {
        ComponentStore<T1> store1 = world.getComponentRegistry().getComponentStore(type1);
        ComponentStore<T2> store2 = world.getComponentRegistry().getComponentStore(type2);
        ComponentStore<T3> store3 = world.getComponentRegistry().getComponentStore(type3);
        if (store1 == null || store2 == null || store3 == null) {
            return new ResultSet<>(EmptyIterator::new);
        }
        return new ResultSet<>(() -> new ThreeComponentIterator<>(world, store1, store2, store3));
    }

    /**
     * Finds all entities that have all four of the specified component types.
     *
     * @param <T1>  the first component type
     * @param <T2>  the second component type
     * @param <T3>  the third component type
     * @param <T4>  the fourth component type
     * @param world the world to query
     * @param type1 the first component class
     * @param type2 the second component class
     * @param type3 the third component class
     * @param type4 the fourth component class
     * @return a Results object containing all matching entities
     */
    static <T1, T2, T3, T4> Results<With4<T1, T2, T3, T4>> findEntitiesWith(
            final SimpleWorld world,
            Class<T1> type1,
            Class<T2> type2,
            Class<T3> type3,
            Class<T4> type4
    ) {
        ComponentStore<T1> store1 = world.getComponentRegistry().getComponentStore(type1);
        ComponentStore<T2> store2 = world.getComponentRegistry().getComponentStore(type2);
        ComponentStore<T3> store3 = world.getComponentRegistry().getComponentStore(type3);
        ComponentStore<T4> store4 = world.getComponentRegistry().getComponentStore(type4);
        if (store1 == null || store2 == null || store3 == null || store4 == null) {
            return new ResultSet<>(EmptyIterator::new);
        }
        return new ResultSet<>(() -> new FourComponentIterator<>(world, store1, store2, store3, store4));
    }


    // =================================================================================================================
    // ResultSet class
    // =================================================================================================================

    /**
     * Internal implementation of Results that provides lazy iteration over query results.
     *
     * @param <T> the result type
     */
    private static final class ResultSet<T> implements Results<T> {
        private final ResultSetIteratorFactory<T> iteratorFactory;

        /**
         * Factory interface for creating iterators over query results.
         *
         * @param <T> the result type
         */
        interface ResultSetIteratorFactory<T> {
            /**
             * Creates a new iterator for the query results.
             *
             * @return a new Iterator instance
             */
            Iterator<T> createIterator();
        }

        /**
         * Creates a new ResultSet with the specified iterator factory.
         *
         * @param iteratorFactory the factory for creating iterators
         */
        ResultSet(ResultSetIteratorFactory<T> iteratorFactory) {
            this.iteratorFactory = iteratorFactory;
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<T> iterator() {
            return iteratorFactory.createIterator();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<T> stream() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
        }
    }

    // =================================================================================================================
    // Iterator classes
    // =================================================================================================================

    /**
     * An empty iterator that returns no results.
     *
     * @param <T> the result type
     */
    private static final class EmptyIterator<T> implements Iterator<T> {
        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @throws NoSuchElementException always
         */
        @Override
        public T next() {
            throw new NoSuchElementException();
        }
    }

    /**
     * Iterator for query results with a single component type.
     *
     * @param <T> the component type
     */
    private static final class SingleComponentIterator<T> implements Iterator<Results.With1<T>> {
        private final SimpleWorld world;
        private final ComponentStore<T> store;
        private int index = 0;

        /**
         * Creates a new iterator for a single component type query.
         *
         * @param store the component store to iterate over
         */
        SingleComponentIterator(SimpleWorld world, ComponentStore<T> store) {
            this.world = world;
            this.store = store;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return index < store.size();
        }

        /**
         * {@inheritDoc}
         *
         * @throws NoSuchElementException if there are no more elements
         */
        @Override
        public Results.With1<T> next() {
            if (!hasNext()) throw new NoSuchElementException();

            int entity = store.getEntityAt(index);
            T comp = store.getComponentAt(index);
            index++;
            return new Results.With1<>(comp, new SimpleEntity(entity, world));
        }
    }

    /**
     * Iterator for query results with two component types.
     *
     * @param <T1> the first component type
     * @param <T2> the second component type
     */
    private static final class TwoComponentIterator<T1, T2> implements Iterator<Results.With2<T1, T2>> {
        private final SimpleWorld world;
        private final ComponentStore<T1> store1;
        private final ComponentStore<T2> store2;
        private int index = 0;

        /**
         * Creates a new iterator for a two component type query.
         *
         * @param store1 the first component store
         * @param store2 the second component store
         */
        TwoComponentIterator(SimpleWorld world, ComponentStore<T1> store1, ComponentStore<T2> store2) {
            this.world = world;
            this.store1 = store1;
            this.store2 = store2;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Skips entities that don't have both components.
         */
        @Override
        public boolean hasNext() {
            while (index < store1.size()) {
                int entity = store1.getEntityAt(index);
                if (store2.has(entity)) {
                    return true;
                }
                index++;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @throws NoSuchElementException if there are no more elements
         */
        @Override
        public Results.With2<T1, T2> next() {
            if (!hasNext()) throw new NoSuchElementException();

            int entity = store1.getEntityAt(index);
            T1 comp1 = store1.getComponentAt(index);
            T2 comp2 = store2.get(entity);
            index++;
            return new Results.With2<>(comp1, comp2, new SimpleEntity(entity, world));
        }
    }

    /**
     * Iterator for query results with three component types.
     *
     * @param <T1> the first component type
     * @param <T2> the second component type
     * @param <T3> the third component type
     */
    private static final class ThreeComponentIterator<T1, T2, T3> implements Iterator<Results.With3<T1, T2, T3>> {
        private final SimpleWorld world;
        private final ComponentStore<T1> store1;
        private final ComponentStore<T2> store2;
        private final ComponentStore<T3> store3;
        private int index = 0;

        /**
         * Creates a new iterator for a three component type query.
         *
         * @param store1 the first component store
         * @param store2 the second component store
         * @param store3 the third component store
         */
        ThreeComponentIterator(SimpleWorld world, ComponentStore<T1> store1, ComponentStore<T2> store2, ComponentStore<T3> store3) {
            this.world = world;
            this.store1 = store1;
            this.store2 = store2;
            this.store3 = store3;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Skips entities that don't have all three components.
         */
        @Override
        public boolean hasNext() {
            while (index < store1.size()) {
                int entity = store1.getEntityAt(index);
                if (store2.has(entity) && store3.has(entity)) {
                    return true;
                }
                index++;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @throws NoSuchElementException if there are no more elements
         */
        @Override
        public Results.With3<T1, T2, T3> next() {
            if (!hasNext()) throw new NoSuchElementException();

            int entity = store1.getEntityAt(index);
            T1 comp1 = store1.getComponentAt(index);
            T2 comp2 = store2.get(entity);
            T3 comp3 = store3.get(entity);
            index++;
            return new Results.With3<>(comp1, comp2, comp3, new SimpleEntity(entity, world));
        }
    }

    /**
     * Iterator for query results with four component types.
     *
     * @param <T1> the first component type
     * @param <T2> the second component type
     * @param <T3> the third component type
     * @param <T4> the fourth component type
     */
    private static final class FourComponentIterator<T1, T2, T3, T4> implements Iterator<Results.With4<T1, T2, T3, T4>> {
        private final SimpleWorld world;
        private final ComponentStore<T1> store1;
        private final ComponentStore<T2> store2;
        private final ComponentStore<T3> store3;
        private final ComponentStore<T4> store4;
        private int index = 0;

        /**
         * Creates a new iterator for a four component type query.
         *
         * @param store1 the first component store
         * @param store2 the second component store
         * @param store3 the third component store
         * @param store4 the fourth component store
         */
        FourComponentIterator(SimpleWorld world, ComponentStore<T1> store1, ComponentStore<T2> store2, ComponentStore<T3> store3, ComponentStore<T4> store4) {
            this.world = world;
            this.store1 = store1;
            this.store2 = store2;
            this.store3 = store3;
            this.store4 = store4;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Skips entities that don't have all four components.
         */
        @Override
        public boolean hasNext() {
            while (index < store1.size()) {
                int entity = store1.getEntityAt(index);
                if (store2.has(entity) && store3.has(entity) && store4.has(entity)) {
                    return true;
                }
                index++;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @throws NoSuchElementException if there are no more elements
         */
        @Override
        public Results.With4<T1, T2, T3, T4> next() {
            if (!hasNext()) throw new NoSuchElementException();

            int entity = store1.getEntityAt(index);
            T1 comp1 = store1.getComponentAt(index);
            T2 comp2 = store2.get(entity);
            T3 comp3 = store3.get(entity);
            T4 comp4 = store4.get(entity);
            index++;
            return new Results.With4<>(comp1, comp2, comp3, comp4, new SimpleEntity(entity, world));
        }
    }
}
