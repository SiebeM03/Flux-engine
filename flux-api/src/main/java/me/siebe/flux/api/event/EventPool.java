package me.siebe.flux.api.event;

import me.siebe.flux.api.event.traits.Pooled;

/**
 * Object pool for a single pooled event type.
 * <p>
 * Acquired events are reset before being returned. Released events are reset and returned to the
 * pool for reuse. Used by {@link EventPoolRegistry} to manage pooled events.
 *
 * @param <T> the event type (must extend Event and implement Pooled)
 * @see EventPoolRegistry
 * @see Pooled
 */
public interface EventPool<T extends Event & Pooled> {
    /**
     * Returns an event instance from the pool, or creates a new one if the pool is empty.
     * The event is {@link Pooled#reset() reset} before being returned.
     *
     * @return an event instance ready for use
     */
    T acquire();

    /**
     * Returns an event to the pool after use. The event is reset before being stored.
     *
     * @param event the event to release
     */
    void release(T event);

    /**
     * Returns the number of event instances currently available in the pool.
     *
     * @return the current pool size
     */
    int getPoolSize();
}
