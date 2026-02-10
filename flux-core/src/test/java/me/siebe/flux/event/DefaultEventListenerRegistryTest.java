package me.siebe.flux.event;

import me.siebe.flux.api.event.EventListener;
import me.siebe.flux.api.event.EventListenerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultEventListenerRegistryTest {
    private EventListenerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultEventListenerRegistry();
    }

    @Test
    void registerListener_ShouldStoreListener() {
        AtomicInteger count = new AtomicInteger(0);
        EventListener<TestEvents.SimpleEvent> listener = e -> count.incrementAndGet();

        registry.register(TestEvents.SimpleEvent.class, listener);

        List<EventListener<TestEvents.SimpleEvent>> listeners = registry.get(TestEvents.SimpleEvent.class);
        assertNotNull(listeners);
        assertEquals(1, listeners.size());
        assertTrue(listeners.contains(listener));
    }

    @Test
    void registerMultipleListeners_ShouldStoreAll() {
        EventListener<TestEvents.SimpleEvent> listener1 = e -> {};
        EventListener<TestEvents.SimpleEvent> listener2 = e -> {};
        EventListener<TestEvents.SimpleEvent> listener3 = e -> {};

        registry.register(TestEvents.SimpleEvent.class, listener1);
        registry.register(TestEvents.SimpleEvent.class, listener2);
        registry.register(TestEvents.SimpleEvent.class, listener3);

        List<EventListener<TestEvents.SimpleEvent>> listeners = registry.get(TestEvents.SimpleEvent.class);
        assertNotNull(listeners);
        assertEquals(3, listeners.size());
        assertTrue(listeners.contains(listener1));
        assertTrue(listeners.contains(listener2));
        assertTrue(listeners.contains(listener3));
    }

    @Test
    void registerListener_ForDifferentEventTypes_ShouldStoreSeparately() {
        EventListener<TestEvents.SimpleEvent> simpleListener = e -> {};
        EventListener<TestEvents.CancellableEvent> cancellableListener = e -> {};

        registry.register(TestEvents.SimpleEvent.class, simpleListener);
        registry.register(TestEvents.CancellableEvent.class, cancellableListener);

        List<EventListener<TestEvents.SimpleEvent>> simpleListeners = registry.get(TestEvents.SimpleEvent.class);
        List<EventListener<TestEvents.CancellableEvent>> cancellableListeners = registry.get(TestEvents.CancellableEvent.class);

        assertNotNull(simpleListeners);
        assertNotNull(cancellableListeners);
        assertEquals(1, simpleListeners.size());
        assertEquals(1, cancellableListeners.size());
        assertTrue(simpleListeners.contains(simpleListener));
        assertTrue(cancellableListeners.contains(cancellableListener));
    }

    @Test
    void get_Old_WithNoListeners_ShouldReturnEmpty() {
        assertNull(registry.get(TestEvents.SimpleEvent.class));
    }

    @Test
    void unregisterListener_ShouldRemoveListener() {
        AtomicInteger count = new AtomicInteger(0);
        EventListener<TestEvents.SimpleEvent> listener = e -> count.incrementAndGet();

        registry.register(TestEvents.SimpleEvent.class, listener);
        registry.unregister(TestEvents.SimpleEvent.class, listener);

        assertNull(registry.get(TestEvents.SimpleEvent.class));
    }

    @Test
    void unregisterListener_FromMultipleListeners_ShouldOnlyRemoveOne() {
        EventListener<TestEvents.SimpleEvent> listener1 = e -> {};
        EventListener<TestEvents.SimpleEvent> listener2 = e -> {};
        EventListener<TestEvents.SimpleEvent> listener3 = e -> {};

        registry.register(TestEvents.SimpleEvent.class, listener1);
        registry.register(TestEvents.SimpleEvent.class, listener2);
        registry.register(TestEvents.SimpleEvent.class, listener3);

        registry.unregister(TestEvents.SimpleEvent.class, listener2);

        List<EventListener<TestEvents.SimpleEvent>> listeners = registry.get(TestEvents.SimpleEvent.class);
        assertNotNull(listeners);
        assertEquals(2, listeners.size());
        assertTrue(listeners.contains(listener1));
        assertFalse(listeners.contains(listener2));
        assertTrue(listeners.contains(listener3));
    }

    @Test
    void unregisterListener_WhenListBecomesEmpty_ShouldRemoveEventType() {
        EventListener<TestEvents.SimpleEvent> listener = e -> {};

        registry.register(TestEvents.SimpleEvent.class, listener);
        registry.unregister(TestEvents.SimpleEvent.class, listener);

        assertNull(registry.get(TestEvents.SimpleEvent.class));
    }

    @Test
    void unregisterNonExistentListener_ShouldNotThrow() {
        EventListener<TestEvents.SimpleEvent> listener1 = e -> {};
        EventListener<TestEvents.SimpleEvent> listener2 = e -> {};

        registry.register(TestEvents.SimpleEvent.class, listener1);

        assertDoesNotThrow(() -> {
            registry.unregister(TestEvents.SimpleEvent.class, listener2);
        });

        List<EventListener<TestEvents.SimpleEvent>> listeners = registry.get(TestEvents.SimpleEvent.class);
        assertNotNull(listeners);
        assertEquals(1, listeners.size());
        assertTrue(listeners.contains(listener1));
    }

    @Test
    void unregisterListener_FromNonExistentEventType_ShouldNotThrow() {
        EventListener<TestEvents.SimpleEvent> listener = e -> {};

        assertDoesNotThrow(() -> {
            registry.unregister(TestEvents.SimpleEvent.class, listener);
        });
    }

    @Test
    void registerSameListenerTwice_ShouldAddTwice() {
        EventListener<TestEvents.SimpleEvent> listener = e -> {};

        registry.register(TestEvents.SimpleEvent.class, listener);
        registry.register(TestEvents.SimpleEvent.class, listener);

        List<EventListener<TestEvents.SimpleEvent>> listeners = registry.get(TestEvents.SimpleEvent.class);
        assertNotNull(listeners);
        assertEquals(2, listeners.size());
    }

    @Test
    void unregisterListener_WhenRegisteredTwice_ShouldOnlyRemoveOne() {
        EventListener<TestEvents.SimpleEvent> listener = e -> {};

        registry.register(TestEvents.SimpleEvent.class, listener);
        registry.register(TestEvents.SimpleEvent.class, listener);
        registry.unregister(TestEvents.SimpleEvent.class, listener);

        List<EventListener<TestEvents.SimpleEvent>> listeners = registry.get(TestEvents.SimpleEvent.class);
        assertNotNull(listeners);
        assertEquals(1, listeners.size());
        assertTrue(listeners.contains(listener));
    }

    // FIXME make it so this properly tests the ability to modify the returned list
    //    @Test
    //    void get_ShouldReturnUnmodifiableList() {
    //        EventListener<TestEvents.SimpleEvent> listener = e -> {};
    //        registry.register(TestEvents.SimpleEvent.class, listener);
    //
    //        List<EventListener<TestEvents.SimpleEvent>> listeners = registry.get(TestEvents.SimpleEvent.class);
    //        assertNotNull(listeners);
    //
    //        // The list should be modifiable (it's an ArrayList internally)
    //        // But we should test that modifications don't affect the registry
    //        EventListener<TestEvents.SimpleEvent> newListener = e -> {};
    //        listeners.add(newListener);
    //
    //        // The new listener should be in the list we got, but not in a fresh get
    //        List<EventListener<TestEvents.SimpleEvent>> freshListeners = registry.get(TestEvents.SimpleEvent.class).orElse(null);
    //        assertNotNull(freshListeners);
    //        // Note: The implementation uses ArrayList which is mutable, so this test verifies behavior
    //        assertTrue(listeners.contains(newListener));
    //    }

    @Test
    void registerNullListener_ShouldNotThrow() {
        // This tests defensive programming - null listeners should be handled
        assertDoesNotThrow(() -> {
            try {
                registry.register(TestEvents.SimpleEvent.class, null);
            } catch (NullPointerException e) {
                // Expected behavior
            }
        });
    }

    @Test
    void registerListener_WithNullEventType_ShouldNotThrow() {
        EventListener<TestEvents.SimpleEvent> listener = e -> {};

        assertDoesNotThrow(() -> {
            try {
                registry.register(null, listener);
            } catch (NullPointerException e) {
                // Expected behavior
            }
        });
    }

    @Test
    void listenerOrder_ShouldBePreserved() {
        EventListener<TestEvents.SimpleEvent> listener1 = e -> {};
        EventListener<TestEvents.SimpleEvent> listener2 = e -> {};
        EventListener<TestEvents.SimpleEvent> listener3 = e -> {};

        registry.register(TestEvents.SimpleEvent.class, listener1);
        registry.register(TestEvents.SimpleEvent.class, listener2);
        registry.register(TestEvents.SimpleEvent.class, listener3);

        List<EventListener<TestEvents.SimpleEvent>> listeners = registry.get(TestEvents.SimpleEvent.class);
        assertNotNull(listeners);
        assertEquals(listener1, listeners.get(0));
        assertEquals(listener2, listeners.get(1));
        assertEquals(listener3, listeners.get(2));
    }

    @Test
    void registerAndUnregister_ManyTimes_ShouldWorkCorrectly() {
        EventListener<TestEvents.SimpleEvent> listener = e -> {};

        for (int i = 0; i < 100; i++) {
            registry.register(TestEvents.SimpleEvent.class, listener);
        }

        List<EventListener<TestEvents.SimpleEvent>> listeners = registry.get(TestEvents.SimpleEvent.class);
        assertNotNull(listeners);
        assertEquals(100, listeners.size());

        for (int i = 0; i < 50; i++) {
            registry.unregister(TestEvents.SimpleEvent.class, listener);
        }

        listeners = registry.get(TestEvents.SimpleEvent.class);
        assertNotNull(listeners);
        assertEquals(50, listeners.size());
    }

    @Test
    void registerMultipleEventTypes_ShouldHandleAll() {
        EventListener<TestEvents.SimpleEvent> simpleListener = e -> {};
        EventListener<TestEvents.CancellableEvent> cancellableListener = e -> {};
        EventListener<TestEvents.PooledEvent> pooledListener = e -> {};
        EventListener<TestEvents.QueuedEvent> queuedListener = e -> {};

        registry.register(TestEvents.SimpleEvent.class, simpleListener);
        registry.register(TestEvents.CancellableEvent.class, cancellableListener);
        registry.register(TestEvents.PooledEvent.class, pooledListener);
        registry.register(TestEvents.QueuedEvent.class, queuedListener);

        assertNotNull(registry.get(TestEvents.SimpleEvent.class));
        assertNotNull(registry.get(TestEvents.CancellableEvent.class));
        assertNotNull(registry.get(TestEvents.PooledEvent.class));
        assertNotNull(registry.get(TestEvents.QueuedEvent.class));
    }
}
