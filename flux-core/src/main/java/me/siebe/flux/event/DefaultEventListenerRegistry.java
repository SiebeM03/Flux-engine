package me.siebe.flux.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventListener;
import me.siebe.flux.api.event.EventListenerRegistry;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultEventListenerRegistry implements EventListenerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DefaultEventListenerRegistry.class);

    private final Map<Class<? extends Event>, List<? extends EventListener<? extends Event>>> listeners = new ConcurrentHashMap<>();

    @Override
    public <E extends Event> void register(Class<E> eventType, EventListener<E> listener) {
        // TODO assert not null using Validator
        logger.debug("Registering event listener {} for event {}", listener, eventType.getName());

        get(eventType).ifPresentOrElse(
                list -> list.add(listener),
                () -> {
                    List<EventListener<E>> newList = new CopyOnWriteArrayList<>();
                    newList.add(listener);
                    listeners.put(eventType, newList);
                }
        );
    }

    @Override
    public <E extends Event> void unregister(Class<E> eventType, EventListener<E> listener) {
        logger.debug("Unregistering event listener {} for event {}", listener, eventType.getName());

        get(eventType).ifPresent(
                list -> {
                    list.remove(listener);
                    if (list.isEmpty()) {
                        listeners.remove(eventType);
                    }
                }
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> Optional<List<EventListener<E>>> get(Class<E> eventType) {
        return Optional.ofNullable((List<EventListener<E>>) listeners.get(eventType));
    }
}
