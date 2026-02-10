package me.siebe.flux.api.event.traits;

// TODO potentially rename to Consumed since if a listener "cancels" the event, the changes from previous
//  event listeners will not be reverted

/**
 * Marker and behavior for events that can be cancelled.
 * <p>
 * When an event implements this interface, the event bus will stop delivering it to further
 * listeners as soon as one listener sets it as cancelled. Listeners that have already been
 * invoked will have had their side effects applied; cancelling does not revert prior handling.
 *
 * @see me.siebe.flux.api.event.Event
 * @see me.siebe.flux.api.event.EventBus
 */
public interface Cancellable {
    /**
     * Returns whether this event has been cancelled.
     *
     * @return true if cancelled
     */
    boolean isCancelled();

    /**
     * Sets the cancelled state of this event.
     *
     * @param cancelled true to cancel, false to leave uncancelled
     */
    void setCancelled(boolean cancelled);

    /**
     * Convenience method to cancel this event (equivalent to {@code setCancelled(true)}).
     */
    default void cancel() {
        setCancelled(true);
    }
}
