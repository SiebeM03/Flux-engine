package me.siebe.flux.api.event;

import me.siebe.flux.api.event.traits.Pooled;

public interface EventPool<T extends Event & Pooled> {
    T acquire();

    void release(T event);
}
