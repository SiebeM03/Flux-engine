package me.siebe.flux.api.event;

import me.siebe.flux.api.event.traits.Pooled;

import java.util.function.Supplier;

public interface EventPoolRegistry {
    <T extends Event & Pooled> void register(Class<T> eventType, Supplier<T> factory);

    <T extends Event & Pooled> EventPool<T> get(Class<T> eventType);

    default <T extends Event & Pooled> T acquire(Class<T> eventType) throws Exception {
        EventPool<T> pool = get(eventType);
        if (pool == null) throw new Exception("No event pool registered for event type type " + eventType);
        T event = pool.acquire();
        if (event == null) throw new Exception("Failed to acquire event for event type " + eventType);
        return event;
    }

    @SuppressWarnings("unchecked")
    default <T extends Event & Pooled> void release(Event event) {
        if (!(event instanceof Pooled)) return;
        EventPool<T> pool = get((Class<T>) event.getClass());
        if (pool == null) return;
        pool.release((T) event);
    }

    <T extends Event & Pooled> boolean hasPool(Class<T> eventType);
}
