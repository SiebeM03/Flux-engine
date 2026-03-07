package me.siebe.flux.test.assertions;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.test.implementations.event.RecordingEventBus;

import java.util.List;

/**
 * Assertion helpers events.
 * Throw {@link AssertionError} on failure.
 */
public final class EventTestAssertions {

    private EventTestAssertions() {}

    public static <T extends Event> void assertListenerRegistered(Class<T> type) {
        var listeners = AppContext.get().getEventBus().getListenerRegistry().get(type);
        if (listeners == null || listeners.isEmpty()) {
            throw new AssertionError("Expected a listener to be registered for type " + type);
        }
    }

    public static <T extends Event> void assertNoListenerRegistered(Class<T> type) {
        var listeners = AppContext.get().getEventBus().getListenerRegistry().get(type);
        if (listeners != null && !listeners.isEmpty()) {
            throw new AssertionError("Expected no listener to be registered for type " + type);
        }
    }

    /**
     * Asserts that the recorded events list has the given size.
     *
     * @param recorded list from {@link RecordingEventBus#getRecordedEvents()}
     * @param expected expected number of events
     * @throws AssertionError if the count does not match
     */
    public static void assertRecordedEventCount(List<RecordingEventBus.RecordedEvent> recorded, int expected) {
        if (recorded.size() != expected) {
            throw new AssertionError("Expected " + expected + " recorded event(s), but got " + recorded.size());
        }
    }

    /**
     * Asserts that at least one recorded event has the given type.
     *
     * @param recorded list from {@link RecordingEventBus#getRecordedEvents()}
     * @param eventType expected event class (e.g. KeyPressEvent.class)
     * @throws AssertionError if no event of that type is found
     */
    public static void assertRecordedEventsContain(List<RecordingEventBus.RecordedEvent> recorded, Class<? extends Event> eventType) {
        boolean found = recorded.stream().anyMatch(e -> e.isType(eventType));
        if (!found) {
            throw new AssertionError("Expected at least one event of type " + eventType.getSimpleName() + ", but found none. Recorded: " + recorded.size() + " event(s)");
        }
    }

    /**
     * Asserts that the number of recorded events of the given type equals the expected count.
     *
     * @param recorded list from {@link RecordingEventBus#getRecordedEvents()}
     * @param eventType event class to count
     * @param expected expected count of that type
     * @throws AssertionError if the count does not match
     */
    public static void assertRecordedEventCountByType(List<RecordingEventBus.RecordedEvent> recorded, Class<? extends Event> eventType, int expected) {
        long count = recorded.stream().filter(e -> e.isType(eventType)).count();
        if (count != expected) {
            throw new AssertionError("Expected " + expected + " event(s) of type " + eventType.getSimpleName() + ", but got " + count);
        }
    }

    /**
     * Asserts that the first recorded event has the given type.
     *
     * @param recorded list from {@link RecordingEventBus#getRecordedEvents()}
     * @param eventType expected type of the first event
     * @throws AssertionError if the list is empty or the first event is not of that type
     */
    public static void assertFirstRecordedEventIs(List<RecordingEventBus.RecordedEvent> recorded, Class<? extends Event> eventType) {
        if (recorded.isEmpty()) {
            throw new AssertionError("Expected first event to be " + eventType.getSimpleName() + ", but no events were recorded");
        }
        if (!recorded.getFirst().isType(eventType)) {
            throw new AssertionError("Expected first event to be " + eventType.getSimpleName() + ", but got " + recorded.get(0).eventType().getSimpleName());
        }
    }
}
