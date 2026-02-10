package me.siebe.flux.api.event;

import me.siebe.flux.api.event.traits.Pooled;

import java.util.function.Consumer;

/**
 * Central hub for posting events and delivering them to registered listeners.
 * <p>
 * Use {@link #getListenerRegistry()} to register and unregister {@link EventListener EventListeners} for event types.
 * Use {@link #post(Event)} to dispatch an event, or {@link #post(Class, Consumer)} for pooled events.
 * Events implementing {@link me.siebe.flux.api.event.traits.Queued} are enqueued until {@link #flush()} is called.
 *
 * @see Event
 * @see EventListener
 * @see EventListenerRegistry
 * @see EventPoolRegistry
 * @see me.siebe.flux.api.event.traits.Queued
 */
public interface EventBus {
    /**
     * Posts an event to all listeners registered for its type.
     * <p>
     * If the event implements {@link me.siebe.flux.api.event.traits.Queued}, it is enqueued and
     * delivered when {@link #flush()} is called. Otherwise, it is delivered immediately.
     *
     * @param event the event to post
     * @param <E>   the event type
     */
    <E extends Event> void post(E event);

    /**
     * Acquires a pooled event instance, configures it via the consumer, and posts it.
     * <p>
     * The event type must be registered with {@link EventPoolRegistry}. After delivery (or on error),
     * the event is released back to the pool.
     *
     * @param eventType the class of the pooled event
     * @param consumer  used to set event fields before posting; must not hold a reference to the event
     * @param <E>       the event type (must extend Event and implement Pooled)
     */
    <E extends Event & Pooled> void post(Class<E> eventType, Consumer<E> consumer);

    /**
     * Delivers all queued events to their listeners in order.
     * <p>
     * Only events that implement {@link me.siebe.flux.api.event.traits.Queued} are affected.
     */
    void flush();

    /**
     * Returns the registry used to register and unregister event listeners.
     *
     * @return the listener registry
     */
    EventListenerRegistry getListenerRegistry();

    /**
     * Returns the registry used to register event pools for pooled event types.
     *
     * @return the pool registry
     */
    EventPoolRegistry getEventPoolRegistry();
}
