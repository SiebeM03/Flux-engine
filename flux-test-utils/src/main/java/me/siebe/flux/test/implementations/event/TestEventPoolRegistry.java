package me.siebe.flux.test.implementations.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventPool;
import me.siebe.flux.api.event.EventPoolRegistry;
import me.siebe.flux.api.event.traits.Pooled;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Minimal event pool registry for headless tests. Supports registering and acquiring pooled events.
 * <p>
 * If no pool is registered for a certain event when {@link EventPoolRegistry#get(Class)} is called,
 * it will fall back to the first no-arg constructor using reflection
 */
public final class TestEventPoolRegistry implements EventPoolRegistry {
    private final Map<Class<?>, TestEventPool<?>> pools = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event & Pooled> void register(Class<T> eventType, Supplier<T> factory) {
        pools.put(eventType, new TestEventPool<>(eventType, factory));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event & Pooled> EventPool<T> get(Class<T> eventType) {
        if (!hasPool(eventType)) {
            pools.put(eventType, new TestEventPool<>(eventType, () -> {
                try {
                    return eventType.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        return (EventPool<T>) pools.get(eventType);
    }

    @Override
    public <T extends Event & Pooled> boolean hasPool(Class<T> eventType) {
        return pools.containsKey(eventType);
    }

    private static final class TestEventPool<T extends Event & Pooled> implements EventPool<T> {
        private final Class<T> eventType;
        private final Supplier<T> factory;
        private final java.util.Queue<T> available = new java.util.LinkedList<>();

        TestEventPool(Class<T> eventType, Supplier<T> factory) {
            this.eventType = eventType;
            this.factory = factory;
        }

        @Override
        public T acquire() {
            T event = available.poll();
            if (event == null) {
                event = factory.get();
            }
            event.reset();
            return event;
        }

        @Override
        public void release(T event) {
            if (event == null) return;
            event.reset();
            available.offer(event);
        }

        @Override
        public int getPoolSize() {
            return available.size();
        }
    }
}
