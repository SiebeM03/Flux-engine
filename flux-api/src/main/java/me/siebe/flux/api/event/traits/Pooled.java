package me.siebe.flux.api.event.traits;

/**
 * Marker and behavior for events that are managed by an {@link me.siebe.flux.api.event.EventPool}.
 * <p>
 * Pooled events are reused to reduce allocations for high-frequency events (e.g. window resize,
 * mouse input). Implementations must provide {@link #reset()} so the event can be cleared before
 * being reused. The event bus acquires instances from the pool when posting via
 * {@link me.siebe.flux.api.event.EventBus#post(Class, java.util.function.Consumer)} and releases
 * them after delivery.
 *
 * @see me.siebe.flux.api.event.EventPool
 * @see me.siebe.flux.api.event.EventPoolRegistry
 * @see me.siebe.flux.api.event.EventBus
 */
public interface Pooled {
    /**
     * Resets this event to a clean state so it can be reused from the pool.
     * <p>
     * Called by the pool when the event is acquired and when it is released.
     */
    void reset();
}
