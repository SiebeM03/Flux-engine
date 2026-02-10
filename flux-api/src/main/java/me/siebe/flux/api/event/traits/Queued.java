package me.siebe.flux.api.event.traits;

import me.siebe.flux.api.event.Event;

/**
 * Marker for events that should be queued instead of delivered immediately.
 * <p>
 * When an event implements this interface and is posted via {@link me.siebe.flux.api.event.EventBus#post(Event)},
 * it is added to an internal queue. Delivery to listeners occurs when {@link me.siebe.flux.api.event.EventBus#flush()}
 * is called, typically at a controlled point in the frame (e.g. end of update). This allows batching
 * and ordering of events like input or resize.
 *
 * @see me.siebe.flux.api.event.EventBus#post(me.siebe.flux.api.event.Event)
 * @see me.siebe.flux.api.event.EventBus#flush()
 */
public interface Queued {
}
