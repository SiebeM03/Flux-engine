package me.siebe.flux.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventPool;
import me.siebe.flux.api.event.traits.Pooled;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public final class DefaultEventPool<E extends Event & Pooled> implements EventPool<E> {
    private final ArrayDeque<E> pool = new ArrayDeque<>();
    private final Supplier<E> factory;

    // Keeps a Map of all events currently in the pool, this allows for O(1) lookups when trying to reset an event
    // If the event is already in the pool (and this map), it means this event was already releaded so calling
    // release() will not do anything
    private final Map<UUID, Event> eventsInPool = new HashMap<>();

    public DefaultEventPool(Supplier<E> factory) {
        this.factory = factory;
    }

    @Override
    public E acquire() {
        E event = pool.pollFirst();
        if (event == null) {
            event = factory.get();
        }
        event.reset();
        eventsInPool.remove(event.uuid);
        return event;
    }

    @Override
    public void release(E event) {
        if (eventsInPool.containsKey(event.uuid)) return;

        event.reset();
        eventsInPool.put(event.uuid, event);
        pool.addLast(event);
    }

    @Override
    public int getPoolSize() {
        return pool.size();
    }
}
