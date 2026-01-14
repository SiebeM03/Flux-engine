package me.siebe.flux.api.event;

import java.util.List;
import java.util.Optional;

public interface EventListenerRegistry {
    <E extends Event> void register(Class<E> eventType, EventListener<E> listener);

    <E extends Event> void unregister(Class<E> eventType, EventListener<E> listener);

    <E extends Event> Optional<List<EventListener<E>>> get(Class<E> eventType);
}
