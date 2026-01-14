package me.siebe.flux.api.event;

/**
 * Functional interface for handling events of a specific type.
 * <p>
 * Listeners are registered with an {@link EventBus} to receive notifications when events
 * of their registered type are posted. This is a functional interface, so it can be used
 * with lambda expressions or method references.
 * <p>
 * <h3>Usage Examples</h3>
 * <p>
 * Using a lambda expression:
 * <pre>{@code
 * EventBus bus = EventBusProvider.get();
 * bus.register(WindowResizeEvent.class, event -> {
 *     System.out.println("Window resized!");
 * });
 * }</pre>
 * <p>
 * Using a method reference:
 * <pre>{@code
 * public class MyComponent {
 *     public void handleResize(WindowResizeEvent event) {
 *         // Handle resize
 *     }
 * }
 *
 * MyComponent component = new MyComponent();
 * bus.register(WindowResizeEvent.class, component::handleResize);
 * }</pre>
 * <p>
 * <h3>Exception Handling</h3>
 * <p>
 * If an exception is thrown from {@link #onEvent(Object)}, it will be caught by the event bus,
 * logged, and processing will continue for other listeners. It is recommended
 * that listeners handle their own exceptions internally when possible.
 * <p>
 * <h3>Thread Safety</h3>
 * <p>
 * Listeners may be invoked from different threads depending on the event bus implementation
 * and when events are posted. Listeners should be thread-safe if they access shared state.
 *
 * @param <E> the type of event this listener handles
 * @see EventBus
 * @see Event
 */
@FunctionalInterface
public interface EventListener<E extends Event> {
    /**
     * Called when an event of type {@code E} is posted to the event bus.
     * <p>
     * This method should handle the event appropriately. Any exceptions thrown from this
     * method will be caught and logged by the event bus, and will not prevent other listeners
     * from receiving the event.
     *
     * @param event the event that was posted
     */
    void onEvent(E event);
}
