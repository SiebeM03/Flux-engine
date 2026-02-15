package me.siebe.flux.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventListener;
import me.siebe.flux.api.event.EventListenerRegistry;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static me.siebe.flux.util.exceptions.Validator.notNull;

/**
 * Default implementation of {@link EventListenerRegistry} using a concurrent map keyed by event
 * type and copy-on-write lists for thread-safe registration and iteration.
 */
public class DefaultEventListenerRegistry implements EventListenerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DefaultEventListenerRegistry.class);

    private final Map<Class<? extends Event>, List<? extends EventListener<? extends Event>>> listenersMap = new ConcurrentHashMap<>();

    @Override
    public <E extends Event> void register(Class<E> eventType, EventListener<E> listener) {
        notNull(eventType, () -> "Event type");
        notNull(listener, () -> "Event Listener");
        logger.debug("Registering event listener {} for event {}", listener, eventType.getName());

        List<EventListener<E>> listeners = get(eventType);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            listenersMap.put(eventType, listeners);
        }
        listeners.add(listener);
    }

    @Override
    public <E extends Event> void unregister(Class<E> eventType, EventListener<E> listener) {
        logger.debug("Unregistering event listener {} for event {}", listener, eventType.getName());

        var listeners = listenersMap.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listenersMap.remove(eventType);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> List<EventListener<E>> get(Class<E> eventType) {
        var listenerList = listenersMap.get(eventType);
        if (listenerList != null) {
            return (List<EventListener<E>>) listenerList;
        } else {
            return null;
        }
    }
}
