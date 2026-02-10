package me.siebe.flux.api.event;

/**
 * Base type for all events in the Flux event system.
 * <p>
 * Subclass this class to define application-specific events. Events can optionally implement
 * {@link me.siebe.flux.api.event.traits.Cancellable}, {@link me.siebe.flux.api.event.traits.Pooled}, and
 * {@link me.siebe.flux.api.event.traits.Queued} to enable cancellation, object pooling, or deferred delivery.
 * <p>
 * Events are posted to an {@link EventBus} and delivered to registered {@link EventListener EventListeners}.
 *
 * @see EventBus
 * @see EventListener
 * @see me.siebe.flux.api.event.traits.Cancellable
 * @see me.siebe.flux.api.event.traits.Pooled
 * @see me.siebe.flux.api.event.traits.Queued
 */
public abstract class Event {
}
