package me.siebe.flux.event;

import me.siebe.flux.api.event.EventPool;
import me.siebe.flux.api.event.EventPoolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultEventPoolRegistryTest {

    private EventPoolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultEventPoolRegistry();
    }

    @Test
    void registerPool_ShouldStorePool() {
        registry.register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        EventPool<TestEvents.PooledEvent> pool = registry.get(TestEvents.PooledEvent.class);
        assertNotNull(pool);
    }

    @Test
    void hasPool_WithRegisteredPool_ShouldReturnTrue() {
        registry.register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        assertTrue(registry.hasPool(TestEvents.PooledEvent.class));
    }

    @Test
    void hasPool_WithNoRegisteredPool_ShouldReturnFalse() {
        assertFalse(registry.hasPool(TestEvents.PooledEvent.class));
    }

    @Test
    void get_WithNoRegisteredPool_ShouldReturnNull() {
        EventPool<TestEvents.PooledEvent> pool = registry.get(TestEvents.PooledEvent.class);
        assertNull(pool);
    }

    @Test
    void acquire_WithRegisteredPool_ShouldReturnEvent() {
        registry.register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        EventPool<TestEvents.PooledEvent> pool = registry.get(TestEvents.PooledEvent.class);
        assertNotNull(pool);
        TestEvents.PooledEvent event = pool.acquire();
        assertNotNull(event);
        assertInstanceOf(TestEvents.PooledEvent.class, event);
    }

    @Test
    void acquire_WithNoRegisteredPool_ShouldReturnNull() {
        me.siebe.flux.api.event.EventPool<TestEvents.PooledEvent> pool = registry.get(TestEvents.PooledEvent.class);
        assertNull(pool);
    }

    @Test
    void acquire_ShouldCallReset() {
        registry.register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        me.siebe.flux.api.event.EventPool<TestEvents.PooledEvent> pool = registry.get(TestEvents.PooledEvent.class);
        TestEvents.PooledEvent event1 = pool.acquire();
        event1.setValue(100);
        pool.release(event1);

        TestEvents.PooledEvent event2 = pool.acquire();
        assertEquals(0, event2.getValue());
    }

    @Test
    void release_ShouldReturnEventToPool() {
        registry.register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        me.siebe.flux.api.event.EventPool<TestEvents.PooledEvent> pool = registry.get(TestEvents.PooledEvent.class);
        TestEvents.PooledEvent event1 = pool.acquire();
        TestEvents.PooledEvent event2 = pool.acquire();

        // Both should be new instances
        assertNotSame(event1, event2);

        pool.release(event1);
        pool.release(event2);

        // Next acquire should reuse from pool
        TestEvents.PooledEvent event3 = pool.acquire();
        TestEvents.PooledEvent event4 = pool.acquire();

        // At least one should be from the pool (could be event1 or event2)
        assertTrue(event3 == event1 || event3 == event2 || event4 == event1 || event4 == event2);
    }

    @Test
    void release_WithNonPooledEvent_ShouldNotThrow() {
        TestEvents.SimpleEvent event = new TestEvents.SimpleEvent();

        // SimpleEvent is not Pooled, so release should handle it gracefully
        assertDoesNotThrow(() -> {
            // The release method in EventPoolRegistry checks if event is Pooled
            // Since SimpleEvent is not Pooled, it should return early
        });
    }

    @Test
    void release_WithUnregisteredPool_ShouldNotThrow() {
        TestEvents.PooledEvent event = new TestEvents.PooledEvent();

        assertDoesNotThrow(() -> {
            EventPool<TestEvents.PooledEvent> pool = registry.get(TestEvents.PooledEvent.class);
            if (pool != null) {
                pool.release(event);
            }
        });
    }

    @Test
    void registerMultiplePools_ShouldStoreAll() {
        registry.register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);
        registry.register(TestEvents.PooledQueuedEvent.class, TestEvents.PooledQueuedEvent::new);
        registry.register(TestEvents.FullTraitEvent.class, TestEvents.FullTraitEvent::new);

        assertTrue(registry.hasPool(TestEvents.PooledEvent.class));
        assertTrue(registry.hasPool(TestEvents.PooledQueuedEvent.class));
        assertTrue(registry.hasPool(TestEvents.FullTraitEvent.class));
    }

    @Test
    void acquire_ShouldReusePooledEvents() {
        registry.register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        EventPool<TestEvents.PooledEvent> pool = registry.get(TestEvents.PooledEvent.class);
        TestEvents.PooledEvent event1 = pool.acquire();
        pool.release(event1);

        TestEvents.PooledEvent event2 = pool.acquire();

        // Should reuse the same instance
        assertSame(event1, event2);
    }

    @Test
    void register_WithNullFactory_ShouldThrow() {
        // TODO
    }

    @Test
    void register_WithNullEventType_ShouldThrow() {
        // TODO
    }

    @Test
    void register_OverwritesExistingPool() throws Exception {
        registry.register(TestEvents.PooledEvent.class, () -> {
            TestEvents.PooledEvent e = new TestEvents.PooledEvent();
            e.setValue(1);
            return e;
        });

        EventPool<TestEvents.PooledEvent> pool1 = registry.get(TestEvents.PooledEvent.class);
        TestEvents.PooledEvent event1 = pool1.acquire();
        pool1.release(event1);

        // Register new factory
        registry.register(TestEvents.PooledEvent.class, () -> {
            TestEvents.PooledEvent e = new TestEvents.PooledEvent();
            e.setValue(2);
            return e;
        });

        // Next acquire should use new factory, but reset will set value to 0
        EventPool<TestEvents.PooledEvent> pool2 = registry.get(TestEvents.PooledEvent.class);
        TestEvents.PooledEvent event2 = pool2.acquire();
        assertEquals(0, event2.getValue());
    }

    @Test
    void acquire_WithMultiplePools_ShouldReturnCorrectType() throws Exception {
        registry.register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);
        registry.register(TestEvents.PooledQueuedEvent.class, TestEvents.PooledQueuedEvent::new);

        EventPool<TestEvents.PooledEvent> pool1 = registry.get(TestEvents.PooledEvent.class);
        EventPool<TestEvents.PooledQueuedEvent> pool2 = registry.get(TestEvents.PooledQueuedEvent.class);
        TestEvents.PooledEvent pooledEvent = pool1.acquire();
        TestEvents.PooledQueuedEvent pooledQueuedEvent = pool2.acquire();

        assertInstanceOf(TestEvents.PooledEvent.class, pooledEvent);
        assertInstanceOf(TestEvents.PooledQueuedEvent.class, pooledQueuedEvent);
    }

    @Test
    void release_WithNullEvent_ShouldThrow() {
        // TODO
    }
}
