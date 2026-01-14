package me.siebe.flux.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventPool;
import me.siebe.flux.api.event.traits.Pooled;

import java.util.ArrayDeque;
import java.util.function.Supplier;

public final class DefaultEventPool<E extends Event & Pooled> implements EventPool<E> {
    private final ArrayDeque<E> pool = new ArrayDeque<>();
    private final Supplier<E> factory;

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
        return event;
    }

    @Override
    public void release(E event) {
        // TODO prevent event from being released multiple times and thus also added to the queue multiple times
        event.reset();
        pool.addLast(event);
    }

    @Override
    public int getPoolSize() {
        return pool.size();
    }
}
