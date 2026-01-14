package me.siebe.flux.event;

import me.siebe.flux.api.event.*;
import me.siebe.flux.api.event.traits.Cancellable;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.event.traits.Queued;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

public class DefaultEventBus implements EventBus {
    private static final Logger logger = LoggerFactory.getLogger(DefaultEventBus.class, LoggingCategories.EVENT);

    // Data variables
    private final EventListenerRegistry listenerRegistry;
    private final Queue<Event> eventQueue;
    private final EventPoolRegistry poolRegistry;

    public DefaultEventBus() {
        listenerRegistry = new DefaultEventListenerRegistry();
        eventQueue = new LinkedList<>();
        poolRegistry = new DefaultEventPoolRegistry();
    }

    @Override
    public <E extends Event> void post(E event) {
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
            logger.error("Failed to acquire event for event type {}", eventType.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Event> void fire(E event) {
        Class<E> eventType = (Class<E>) event.getClass();

        Optional<List<EventListener<E>>> optListeners = listenerRegistry.get(eventType);
        if (optListeners.isPresent()) {
            List<EventListener<E>> listeners = optListeners.get();
            logger.trace("Firing event {} to {} listeners", eventType.getName(), listeners.size());

            for (EventListener<E> listener : listeners) {
                if (event instanceof Cancellable cancellable && cancellable.isCancelled()) break;

                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    logger.error("Exception while handling event " + eventType.getName(), e);
                }
            }
        }

        if (event instanceof Pooled) {
            poolRegistry.release(event);
        }
    }

    @Override
    public void flush() {
        if (eventQueue.isEmpty()) return;

        logger.trace("Flushing {} events in queue", eventQueue.size());
        while (!eventQueue.isEmpty()) {
            fire(eventQueue.poll());
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
