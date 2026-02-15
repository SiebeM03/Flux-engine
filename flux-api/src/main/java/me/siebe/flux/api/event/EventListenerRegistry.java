package me.siebe.flux.api.event;

import java.util.List;
import java.util.Optional;

/**
 * Registry for event listeners, keyed by event type.
 * <p>
 * Listeners registered for a given event class receive all events of that exact type (and not subclasses
 * unless the implementation supports it). Typically obtained from {@link EventBus#getListenerRegistry()}.
 *
 * @see EventBus
 * @see EventListener
 */
public interface EventListenerRegistry {
    /**
     * Registers a listener to receive events of the given type.
     *
     * @param eventType the event class to listen for
     * @param listener  the listener to invoke when such events are posted
     * @param <E>       the event type
     */
    <E extends Event> void register(Class<E> eventType, EventListener<E> listener);

    /**
     * Removes a previously registered listener for the given event type.
     *
     * @param eventType the event class
     * @param listener  the listener to remove
     * @param <E>       the event type
     */
    <E extends Event> void unregister(Class<E> eventType, EventListener<E> listener);

    /**
     * Returns the list of listeners registered for the given event type, or null if none registered.
     *
     * @param eventType the event class
     * @param <E>       the event type
     * @return a list of listeners, or null if none are registered
     */
    <E extends Event> List<EventListener<E>> get(Class<E> eventType);
}
