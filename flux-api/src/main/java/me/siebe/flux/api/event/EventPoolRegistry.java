package me.siebe.flux.api.event;

import me.siebe.flux.api.event.traits.Pooled;

import java.util.function.Supplier;

/**
 * Registry of object pools for pooled event types.
 * <p>
 * Pooled events are reused to reduce allocations for high-frequency events (e.g. input or window resize).
 * Each event type is associated with a {@link EventPool} and a factory for creating new instances
 * when the pool is empty. Typically obtained from {@link EventBus#getEventPoolRegistry()}.
 *
 * @see EventPool
 * @see Pooled
 * @see EventBus
 */
public interface EventPoolRegistry {
    /**
     * Registers a pool for the given event type using the supplied factory to create new instances.
     *
     * @param eventType the event class (must implement {@link Pooled})
     * @param factory   supplier for new event instances when the pool is empty
     * @param <T>       the event type
     */
    <T extends Event & Pooled> void register(Class<T> eventType, Supplier<T> factory);

    /**
     * Returns the pool for the given event type, or null if none is registered.
     *
     * @param eventType the event class
     * @param <T>       the event type
     * @return the pool, or null
     */
    <T extends Event & Pooled> EventPool<T> get(Class<T> eventType);

    /**
     * Acquires an event instance from the pool for the given type.
     * <p>
     * The event will have been {@link Pooled#reset() reset} before being returned. The caller (or
     * the event bus after posting) is responsible for releasing it via {@link #release(Event)}.
     *
     * @param eventType the event class
     * @param <T>       the event type
     * @return a pooled (or newly created) event instance
     * @throws Exception if no pool is registered or acquisition fails
     */
    default <T extends Event & Pooled> T acquire(Class<T> eventType) throws Exception {
        EventPool<T> pool = get(eventType);
        if (pool == null) throw new Exception("No event pool registered for event type type " + eventType);
        T event = pool.acquire();
        if (event == null) throw new Exception("Failed to acquire event for event type " + eventType);
        return event;
    }

    /**
     * Returns a pooled event to its pool after use.
     * <p>
     * No-op if the event is not pooled or no pool is registered for its type.
     *
     * @param event the event to release (must implement {@link Pooled})
     * @param <T>   the event type
     */
    @SuppressWarnings("unchecked")
    default <T extends Event & Pooled> void release(Event event) {
        if (!(event instanceof Pooled)) return;
        EventPool<T> pool = get((Class<T>) event.getClass());
        if (pool == null) return;
        pool.release((T) event);
    }

    /**
     * Returns whether a pool is registered for the given event type.
     *
     * @param eventType the event class
     * @param <T>       the event type
     * @return true if a pool exists for this type
     */
    <T extends Event & Pooled> boolean hasPool(Class<T> eventType);
}
