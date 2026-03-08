package me.siebe.flux.test.implementations.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.event.EventListenerRegistry;
import me.siebe.flux.api.event.EventPoolRegistry;
import me.siebe.flux.api.event.traits.Pooled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Wraps an {@link EventBus} and records every posted event (type and optional snapshot)
 * for test assertions. Use {@link #getRecordedEvents()} and {@link #clearRecordedEvents()}.
 */
public final class RecordingEventBus implements EventBus {

    private final EventBus delegate;
    private final List<RecordedEvent> recorded = new ArrayList<>();

    public RecordingEventBus(EventBus delegate) {
        this.delegate = delegate;
    }

    @Override
    public <E extends Event> void post(E event) {
        System.err.println("POSTED EVENT " + event.getClass().getSimpleName());
        if (event != null) {
            recorded.add(new RecordedEvent(event.getClass(), event.toString()));
        }
        delegate.post(event);
    }

    @Override
    public <E extends Event & Pooled> void post(Class<E> eventType, Consumer<E> consumer) {
        System.err.println("POSTED EVENT " + eventType.getSimpleName());
        recorded.add(new RecordedEvent(eventType, "[pooled]"));
        delegate.post(eventType, consumer);
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public EventListenerRegistry getListenerRegistry() {
        return delegate.getListenerRegistry();
    }

    @Override
    public EventPoolRegistry getEventPoolRegistry() {
        return delegate.getEventPoolRegistry();
    }

    /**
     * Returns an immutable copy of events recorded so far (in order).
     */
    public List<RecordedEvent> getRecordedEvents() {
        return Collections.unmodifiableList(new ArrayList<>(recorded));
    }

    /**
     * Clears the recorded list. Call between test runs or after setup to isolate assertions.
     */
    public void clearRecordedEvents() {
        recorded.clear();
    }

    /**
     * A single recorded event: type and snapshot string (e.g. {@link Object#toString()} or "[pooled]").
     */
    public record RecordedEvent(Class<? extends Event> eventType, String snapshot) {
        public boolean isType(Class<? extends Event> type) {
            return type != null && type.isAssignableFrom(eventType);
        }
    }
}
