package me.siebe.flux.test.implementations.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.event.EventListener;
import me.siebe.flux.api.event.EventListenerRegistry;
import me.siebe.flux.api.event.EventPoolRegistry;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.event.traits.Queued;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Minimal event bus implementation for headless tests. Supports immediate and queued delivery
 * and pooled events so that {@link me.siebe.flux.api.input.Input#init} can be used in tests.
 */
public final class TestEventBus implements EventBus {
    private final EventListenerRegistry listenerRegistry;
    private final EventPoolRegistry poolRegistry;
    private final Queue<Event> queue = new LinkedList<>();

    public TestEventBus() {
        this.listenerRegistry = new TestEventListenerRegistry();
        this.poolRegistry = new TestEventPoolRegistry();
    }

    @Override
    public <E extends Event> void post(E event) {
        if (event == null) return;
        if (event instanceof Queued) {
            queue.offer(event);
        } else {
            fire(event);
        }
    }

    @Override
    public <E extends Event & Pooled> void post(Class<E> eventType, Consumer<E> consumer) {
        try {
            E event = poolRegistry.acquire(eventType);
            consumer.accept(event);
            post(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to post pooled event: " + eventType.getSimpleName(), e);
        }
    }

    @Override
    public void flush() {
        while (!queue.isEmpty()) {
            Event event = queue.poll();
            fire(event);
        }
    }

    @Override
    public EventListenerRegistry getListenerRegistry() {
        return listenerRegistry;
    }

    @Override
    public EventPoolRegistry getEventPoolRegistry() {
        return poolRegistry;
    }

    @SuppressWarnings("unchecked")
    private <E extends Event> void fire(E event) {
        if (event == null) return;
        Class<E> eventType = (Class<E>) event.getClass();
        List<EventListener<E>> listeners = listenerRegistry.get(eventType);
        if (listeners != null) {
            for (EventListener<E> listener : listeners) {
                listener.onEvent(event);
            }
        }
        if (event instanceof Pooled) {
            poolRegistry.release(event);
        }
    }
}
