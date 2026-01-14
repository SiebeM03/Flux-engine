package me.siebe.flux.api.event;

public class EventBusProvider {
    private static EventBusProvider instance;
    private final EventBus eventBus;

    private EventBusProvider(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public static void init(EventBus eventBus) {
        if (instance != null) {
            throw new IllegalStateException("EventBus instance has already been initialized.");
        }
        instance = new EventBusProvider(eventBus);
    }

    public static EventBus get() {
        return instance.eventBus;
    }
}
