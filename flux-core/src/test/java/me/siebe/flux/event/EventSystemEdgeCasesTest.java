package me.siebe.flux.event;

import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.event.EventListener;
import me.siebe.flux.api.event.EventPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class EventSystemEdgeCasesTest {
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new DefaultEventBus();
    }

    @Test
    void postEvent_WithListenerThatRegistersNewListener_ShouldNotInvokeNewListener() {
        List<Integer> order = new ArrayList<>();

        // Registering a new listener during iteration may cause ConcurrentModificationException
        // depending on the implementation. The test verifies basic behavior.
        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> {
            order.add(1);
            eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e2 -> {
                order.add(2);
            });
        });

        eventBus.post(new TestEvents.SimpleEvent());

        // At least the first listener should be invoked
        assertEquals(1, order.size());
        assertEquals(1, order.getFirst());
    }

    @Test
    void postEvent_WithListenerThatUnregistersSelf_ShouldNotAffectCurrentInvocation() {
        AtomicInteger count = new AtomicInteger(0);
        AtomicReference<EventListener<TestEvents.SimpleEvent>> listenerReference = new AtomicReference<>();

        EventListener<TestEvents.SimpleEvent> listener = e -> {
            count.incrementAndGet();
            // Unregistering during iteration may cause ConcurrentModificationException
            // This is an edge case that may not be fully supported
            eventBus.getListenerRegistry().unregister(TestEvents.SimpleEvent.class, listenerReference.get());
        };

        listenerReference.set(listener);
        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, listener);

        eventBus.post(new TestEvents.SimpleEvent());

        assertTrue(count.get() >= 1);
    }

    @Test
    void postEvent_WithListenerThatUnregistersOtherListener_ShouldWork() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);
        AtomicReference<EventListener<TestEvents.SimpleEvent>> listener2Ref = new AtomicReference<>();

        EventListener<TestEvents.SimpleEvent> listener1 = e -> {
            count1.incrementAndGet();
            // Unregister listener2 - this modifies the registry during iteration
            // We'll test that listener1 is called, and listener2 should not be called
            eventBus.getListenerRegistry().unregister(TestEvents.SimpleEvent.class, listener2Ref.get());
        };

        EventListener<TestEvents.SimpleEvent> listener2 = e -> {
            count2.incrementAndGet();
        };

        listener2Ref.set(listener2);

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, listener1);
        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, listener2);

        eventBus.post(new TestEvents.SimpleEvent());

        // Listener1 should be called
        assertEquals(1, count1.get());

        // Next post - listener2 should definitely not be called if unregister succeeded
        int count2Before = count2.get();
        eventBus.post(new TestEvents.SimpleEvent());

        // Listener1 should be called again
        assertEquals(2, count1.get());
        // Listener2 should not be called and thus count2 should not have changed
        assertEquals(count2Before, count2.get());
    }

    @Test
    void postEvent_WithListenerThatPostsNewEvent_ShouldProcessImmediately() {
        List<String> order = new ArrayList<>();

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> {
            order.add("first");
            eventBus.post(new TestEvents.CancellableEvent("nested"));
        });

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> {
            order.add(e.getData());
        });

        eventBus.post(new TestEvents.SimpleEvent());
        eventBus.post(new TestEvents.CancellableEvent("unnested"));

        assertEquals(3, order.size());
        assertEquals("first", order.get(0));
        assertEquals("nested", order.get(1));
        assertEquals("unnested", order.get(2));
    }


    @Test
    void postEvent_WithListenerThatPostsQueuedEvent_ShouldQueueIt() {
        AtomicInteger simpleCount = new AtomicInteger(0);
        AtomicInteger queuedCount = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> {
            simpleCount.incrementAndGet();
            eventBus.post(new TestEvents.QueuedEvent(1));
        });

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> {
            queuedCount.incrementAndGet();
        });

        eventBus.post(new TestEvents.SimpleEvent());

        assertEquals(1, simpleCount.get());
        assertEquals(0, queuedCount.get());

        eventBus.flush();

        assertEquals(1, queuedCount.get());
    }

    @Test
    void flush_WithListenerThatPostsNewQueuedEvent_ShouldDeferToNextFlush() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> {
            count.incrementAndGet();
            if (count.get() < 5) {
                eventBus.post(new TestEvents.QueuedEvent(count.get() + 1));
            }
        });

        eventBus.post(new TestEvents.QueuedEvent(1));

        // First flush: Only the initial event is processed
        // Events posted during processing are deferred to the next flush/frame
        eventBus.flush();
        assertEquals(1, count.get());

        // Second flush: Process events that were queued during the first flush
        eventBus.flush();
        assertEquals(2, count.get());

        // Continue flushing until all events are processed
        eventBus.flush();
        assertEquals(3, count.get());
        eventBus.flush();
        assertEquals(4, count.get());
        eventBus.flush();
        assertEquals(5, count.get());
    }

    @Test
    void postPooledEvent_WithListenerThatAcquiresSameType_ShouldWork() {
        eventBus.getEventPoolRegistry().register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        AtomicInteger count = new AtomicInteger(0);
        eventBus.getListenerRegistry().register(TestEvents.PooledEvent.class, e -> {
            count.incrementAndGet();
            try {
                EventPool<TestEvents.PooledEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.PooledEvent.class);
                if (pool != null) {
                    TestEvents.PooledEvent nested = pool.acquire();
                    nested.setValue(999);
                    pool.release(nested);
                }
            } catch (Exception ex) {
                fail("Should not throw exception");
            }
        });

        eventBus.post(TestEvents.PooledEvent.class, e -> e.setValue(100));

        EventPool<TestEvents.PooledEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.PooledEvent.class);
        // The pool has 2 events: 1 created by the post call and one created by the acquire call inside the listener
        assertEquals(2, pool.getPoolSize());
    }

    @Test
    void postEvent_WithManyListeners_ShouldInvokeAll() {
        AtomicInteger count = new AtomicInteger(0);

        for (int i = 0; i < 1000; i++) {
            eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> {
                count.incrementAndGet();
            });
        }

        eventBus.post(new TestEvents.SimpleEvent());

        assertEquals(1000, count.get());
    }

    @Test
    void postManyEvents_ShouldHandleAll() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> count.incrementAndGet());

        for (int i = 0; i < 1000; i++) {
            eventBus.post(new TestEvents.SimpleEvent());
        }

        assertEquals(1000, count.get());
    }

    @Test
    void postManyQueuedEvents_ShouldFlushAll() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> count.incrementAndGet());

        for (int i = 0; i < 1000; i++) {
            eventBus.post(new TestEvents.QueuedEvent(i));
        }

        assertEquals(0, count.get());

        eventBus.flush();

        assertEquals(1000, count.get());
    }

    @Test
    void cancellableEvent_CancelledInMiddle_ShouldStopPropagation() {
        List<Integer> invoked = new ArrayList<>();

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> {
            invoked.add(1);
        });

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> {
            invoked.add(2);
            e.cancel();
        });

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> {
            invoked.add(3);
        });

        eventBus.post(new TestEvents.CancellableEvent());

        // Only the 1st and 2nd listener are reached
        assertEquals(2, invoked.size());
        assertEquals(1, invoked.get(0));
        assertEquals(2, invoked.get(1));
    }

    @Test
    void pooledEvent_ReleasedMultipleTimes_ShouldNotThrow() {
        eventBus.getEventPoolRegistry().register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        EventPool<TestEvents.PooledEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.PooledEvent.class);
        TestEvents.PooledEvent event = pool.acquire();

        pool.release(event);

        assertDoesNotThrow(() -> {
            pool.release(event);
        });
    }

    @Test
    void pooledEvent_AcquiredButNotReleased_ShouldCreateNewOnNextAcquire() {
        eventBus.getEventPoolRegistry().register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        EventPool<TestEvents.PooledEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.PooledEvent.class);
        TestEvents.PooledEvent event1 = pool.acquire();
        TestEvents.PooledEvent event2 = pool.acquire();

        // Acquired events should not be inside the pool
        assertEquals(0, pool.getPoolSize());

        pool.release(event1);
        pool.release(event2);

        // Pool should contain 2 instances
        assertEquals(2, pool.getPoolSize());
        // Should be different instances
        assertNotSame(event1, event2);
    }

    @Test
    void event_WithInheritance_ShouldWork() {
        // Test that events can be subclassed
        class ExtendedEvent extends TestEvents.SimpleEvent {
            private int extra;

            public int getExtra() {
                return extra;
            }

            public void setExtra(int extra) {
                this.extra = extra;
            }
        }

        AtomicInteger count = new AtomicInteger(0);
        eventBus.getListenerRegistry().register(ExtendedEvent.class, e -> {
            count.incrementAndGet();
            assertEquals(42, e.getExtra());
        });

        ExtendedEvent event = new ExtendedEvent();
        event.setExtra(42);
        eventBus.post(event);

        assertEquals(1, count.get());
    }


}
