package me.siebe.flux.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventPool;
import me.siebe.flux.api.event.traits.Pooled;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Supplier;

public final class DefaultEventPool<E extends Event & Pooled> implements EventPool<E> {
    private final ArrayDeque<E> pool = new ArrayDeque<>();
    private final Supplier<E> factory;

    // Keeps a Set of all events currently in the pool, this allows for O(1) lookups when trying to release an event.
    // If the event is already in the pool (and this set), it means this event was already released, calling
    // release() will not do anything
    private final Set<E> inPool = Collections.newSetFromMap(new IdentityHashMap<>());

    public DefaultEventPool(Supplier<E> factory) {
        this.factory = factory;
    }

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

    @Override
    public void release(E event) {
        if (!inPool.add(event)) return; // Already in pool, ignore duplicate releases
        event.reset();
        pool.addLast(event);
    }

    @Override
    public int getPoolSize() {
        return pool.size();
    }
}
