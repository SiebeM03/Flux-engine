package me.siebe.flux.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventPool;
import me.siebe.flux.api.event.traits.Pooled;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Default implementation of {@link EventPool} using an {@link ArrayDeque} for storage and
 * identity-based tracking to avoid duplicate releases. New instances are created via the supplied
 * factory when the pool is empty.
 *
 * @param <E> the pooled event type
 */
public final class DefaultEventPool<E extends Event & Pooled> implements EventPool<E> {
    /** Stores released events available for reuse. */
    private final ArrayDeque<E> pool = new ArrayDeque<>();

    /**
     * Tracks which events are currently in the pool for O(1) duplicate-release checks
     * (instead of O(N) with {@link ArrayDeque#contains(Object)}).
     */
    private final Set<E> inPool = Collections.newSetFromMap(new IdentityHashMap<>());

    /** Creates new event instances when the pool is empty. */
    private final Supplier<E> factory;

    /**
     * Creates a new pool that uses the given factory to allocate events when the pool is empty.
     *
     * @param factory supplier for new event instances; must not return null
     */
    public DefaultEventPool(Supplier<E> factory) {
        this.factory = factory;
    }

    /** {@inheritDoc} */
    @Override
    public E acquire() {
        E event = pool.pollFirst();
        if (event != null) {
            inPool.remove(event);
        } else {
            event = factory.get();
        }
        event.reset();
        return event;
    }

    /** {@inheritDoc} Duplicate releases for the same instance are ignored. */
    @Override
    public void release(E event) {
        if (!inPool.add(event)) return; // Already in pool, ignore duplicate releases
        event.reset();
        pool.addLast(event);
    }

    /** {@inheritDoc} */
    @Override
    public int getPoolSize() {
        return pool.size();
    }
}
