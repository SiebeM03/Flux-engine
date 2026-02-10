package me.siebe.flux.event;

import me.siebe.flux.api.event.*;
import me.siebe.flux.api.event.traits.Cancellable;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.event.traits.Queued;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Default implementation of {@link EventBus}.
 * <p>
 * Dispatches events to listeners registered via {@link #getListenerRegistry()}. Events implementing
 * {@link Queued} are enqueued until {@link #flush()} is called; others are delivered immediately.
 * Pooled events are acquired from and released to {@link #getEventPoolRegistry()}. For
 * {@link Cancellable} events, delivery stops once a listener cancels the event.
 */
public class DefaultEventBus implements EventBus {
    private static final Logger logger = LoggerFactory.getLogger(DefaultEventBus.class, LoggingCategories.EVENT);

    private final EventListenerRegistry listenerRegistry;
    private final Queue<Event> eventQueue;
    private final EventPoolRegistry poolRegistry;

    /**
     * Creates a new event bus with a default listener registry, event queue, and pool registry.
     */
    public DefaultEventBus() {
        listenerRegistry = new DefaultEventListenerRegistry();
        eventQueue = new LinkedList<>();
        poolRegistry = new DefaultEventPoolRegistry();
    }

    @Override
    public <E extends Event> void post(E event) {
        if (event == null) return;

        if (event instanceof Queued) {
            logger.trace("Queued event {}", event);
            eventQueue.offer(event);
        } else {
            fire(event);
        }
    }

    @Override
    public <E extends Event & Pooled> void post(Class<E> eventType, Consumer<E> consumer) {
        try {
            E event = poolRegistry.acquire(eventType);
            consumer.accept(event); // Allows users to set event values
            post(event);
        } catch (Exception e) {
            logger.error("Failed to post pooled event for event type {}", eventType.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Event> void fire(E event) {
        if (event == null) return;
        Class<E> eventType = (Class<E>) event.getClass();

        try {
            List<EventListener<E>> listeners = listenerRegistry.get(eventType);
            if (listeners == null || listeners.isEmpty()) return;

            for (EventListener<E> listener : listeners) {
                if (event instanceof Cancellable cancellable && cancellable.isCancelled()) break;

                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    logger.error("Exception in listener {} while handling event {}", listener, eventType.getName(), e);
                }
            }
        } catch (ConcurrentModificationException e) {
            logger.error("Concurrent modification error occurred while firing event {} to the listeners. \n" +
                    "This is likely due to an event listener registering/unregistering a listener", eventType.getName(), e);
        } catch (Exception e) {
            logger.error("Exception while handling event {}", eventType.getName(), e);
        }

        if (event instanceof Pooled) {
            poolRegistry.release(event);
        }
    }

    @Override
    public void flush() {
        if (eventQueue.isEmpty()) return;

        logger.trace("Flushing {} events in queue", eventQueue.size());
        int batchSize = eventQueue.size();

        for (int i = 0; i < batchSize; i++) {
            Event event = eventQueue.poll();
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
}
