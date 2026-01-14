package me.siebe.flux.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.EventPool;
import me.siebe.flux.api.event.EventPoolRegistry;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultEventPoolRegistry implements EventPoolRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DefaultEventPoolRegistry.class, LoggingCategories.EVENT);

    private final Map<Class<? extends Pooled>, DefaultEventPool<? extends Pooled>> pools = new HashMap<>();

    @Override
    public <T extends Event & Pooled> void register(
            Class<T> eventType,
            Supplier<T> factory
    ) {
        if (!(Event.class.isAssignableFrom(eventType))) {
            logger.warn("Event type {} is not a Pooled event type", eventType);
        }
        if (pools.containsKey(eventType)) {
            logger.warn("Event type {} has already been registered, overriding...", eventType);
        }
        pools.put(eventType, new DefaultEventPool<>(factory));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event & Pooled> EventPool<T> get(Class<T> eventType) {
        return (EventPool<T>) pools.get(eventType);
    }

    @Override
    public <T extends Event & Pooled> boolean hasPool(Class<T> eventType) {
        return pools.containsKey(eventType);
    }
}
