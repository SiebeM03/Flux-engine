package me.siebe.flux.test.implementations.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventListener;
import me.siebe.flux.api.event.EventListenerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal listener registry for headless tests.
 */
public final class TestEventListenerRegistry implements EventListenerRegistry {
    private final Map<Class<?>, List<EventListener<?>>> listenersByType = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> void register(Class<E> eventType, EventListener<E> listener) {
        listenersByType
                .computeIfAbsent(eventType, k -> new ArrayList<>())
                .add((EventListener<Event>) listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> void unregister(Class<E> eventType, EventListener<E> listener) {
        List<EventListener<?>> list = listenersByType.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> List<EventListener<E>> get(Class<E> eventType) {
        List<EventListener<?>> list = listenersByType.get(eventType);
        return list == null ? null : (List<EventListener<E>>) (List<?>) list;
    }
}
