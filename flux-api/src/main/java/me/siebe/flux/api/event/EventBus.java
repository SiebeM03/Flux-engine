package me.siebe.flux.api.event;

import me.siebe.flux.api.event.traits.Pooled;

import java.util.function.Consumer;

public interface EventBus {
    <E extends Event> void post(E event);

    <E extends Event & Pooled> void post(Class<E> eventType, Consumer<E> consumer);

    void flush();

    EventListenerRegistry getListenerRegistry();

    EventPoolRegistry getEventPoolRegistry();
}
