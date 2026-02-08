package me.siebe.flux.event;

import me.siebe.flux.api.event.EventPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultEventPoolTest {
    private EventPool<TestEvents.PooledEvent> pool;

    @BeforeEach
    void setUp() {
        pool = new DefaultEventPool<>(TestEvents.PooledEvent::new);
    }

    @Test
    void acquire_WhenPoolEmpty_ShouldCreateNewEvent() {
        TestEvents.PooledEvent event = pool.acquire();

        assertNotNull(event);
        assertInstanceOf(TestEvents.PooledEvent.class, event);
    }

    @Test
    void acquire_ShouldCallReset() {
        TestEvents.PooledEvent event = pool.acquire();

        assertTrue(event.wasResetCalled());
    }

    @Test
    void release_ShouldCallReset() {
        TestEvents.PooledEvent event = pool.acquire();
        event.setValue(999);
        event.clearResetFlag();

        pool.release(event);

        assertTrue(event.wasResetCalled());
        assertEquals(0, event.getValue());
    }

    @Test
    void release_ShouldReturnEventToPool() {
        TestEvents.PooledEvent event = pool.acquire();
        pool.release(event);

        TestEvents.PooledEvent event2 = pool.acquire();

        assertSame(event, event2);
    }

    @Test
    void acquire_AfterRelease_ShouldReuseEvent() {
        TestEvents.PooledEvent event1 = pool.acquire();
        event1.setValue(42);
        pool.release(event1);

        TestEvents.PooledEvent event2 = pool.acquire();

        assertSame(event1, event2);
        assertEquals(0, event2.getValue()); // Should be reset
    }

    @Test
    void multipleAcquireRelease_ShouldReuseEvents() {
        TestEvents.PooledEvent event1 = pool.acquire();
        TestEvents.PooledEvent event2 = pool.acquire();
        TestEvents.PooledEvent event3 = pool.acquire();

        pool.release(event1);
        pool.release(event2);
        pool.release(event3);

        TestEvents.PooledEvent reused1 = pool.acquire();
        TestEvents.PooledEvent reused2 = pool.acquire();
        TestEvents.PooledEvent reused3 = pool.acquire();

        // Should reuse from pool
        assertTrue(reused1 == event1 || reused1 == event2 || reused1 == event3);
        assertTrue(reused2 == event1 || reused2 == event2 || reused2 == event3);
        assertTrue(reused3 == event1 || reused3 == event2 || reused3 == event3);
    }

    @Test
    void acquire_WhenPoolHasEvents_ShouldReturnPooledFirst() {
        TestEvents.PooledEvent event1 = pool.acquire();
        TestEvents.PooledEvent event2 = pool.acquire();

        pool.release(event1);
        pool.release(event2);

        TestEvents.PooledEvent reused = pool.acquire();

        // Should be one of the released events
        assertTrue(reused == event1 || reused == event2);
    }

    @Test
    void release_WithNullEvent_ShouldThrow() {
        // DefaultEventPool.release() doesn't handle null, it will throw NPE
        assertThrows(NullPointerException.class, () -> {
            pool.release(null);
        });
    }

    @Test
    void release_WhenReleasedTwice_ShouldIgnoreSecondRelease() {
        TestEvents.PooledEvent event = pool.acquire();
        pool.release(event);
        pool.release(event); // duplicate release, should be ignored

        // Only one event in pool, so the first acquire will return the pooled event and the second will create a new one
        TestEvents.PooledEvent first = pool.acquire();
        TestEvents.PooledEvent second = pool.acquire();

        assertSame(event, first);
        assertNotSame(event, second);
    }

    @Test
    void acquire_AfterManyReleases_ShouldReuseOldestFirst() {
        // FIFO behavior - first released should be first acquired
        TestEvents.PooledEvent event1 = pool.acquire();
        TestEvents.PooledEvent event2 = pool.acquire();
        TestEvents.PooledEvent event3 = pool.acquire();

        pool.release(event1);
        pool.release(event2);
        pool.release(event3);

        TestEvents.PooledEvent first = pool.acquire();
        assertSame(event1, first); // FIFO - first released should be first acquired
    }

    @Test
    void pool_ShouldHandleManyCycles() {
        for (int i = 0; i < 10000; i++) {
            TestEvents.PooledEvent event = pool.acquire();
            event.setValue(i);
            pool.release(event);
        }

        TestEvents.PooledEvent finalEvent = pool.acquire();
        assertEquals(0, finalEvent.getValue());
    }

    @Test
    void acquire_ShouldAlwaysReturnResetEvent() {
        for (int i = 0; i < 10; i++) {
            TestEvents.PooledEvent event = pool.acquire();
            event.setValue(100 + i);
            pool.release(event);

            TestEvents.PooledEvent next = pool.acquire();
            assertEquals(0, next.getValue());
        }
    }

    @Test
    void pool_WithFullTraitEvent_ShouldWork() {
        EventPool<TestEvents.FullTraitEvent> fullPool = new DefaultEventPool<>(TestEvents.FullTraitEvent::new);

        TestEvents.FullTraitEvent event = fullPool.acquire();
        event.setValue(50);
        event.cancel();
        fullPool.release(event);

        TestEvents.FullTraitEvent reused = fullPool.acquire();
        assertEquals(0, reused.getValue());
        assertFalse(reused.isCancelled());
    }
}
