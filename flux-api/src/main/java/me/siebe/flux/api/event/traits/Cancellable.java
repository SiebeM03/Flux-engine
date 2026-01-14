package me.siebe.flux.api.event.traits;

// Events that can be cancelled, this stops the event from being sent to other listeners
// TODO potentially rename to Consumed since if a listener "cancels" the event, the changes from previous
//  event listeners will not be reverted
public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean cancelled);

    default void cancel() {
        setCancelled(true);
    }
}
