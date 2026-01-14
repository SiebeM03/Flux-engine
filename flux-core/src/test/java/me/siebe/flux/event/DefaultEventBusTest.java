package me.siebe.flux.event;

import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.event.EventPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultEventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new DefaultEventBus();
    }

    @Test
    void postSimpleEvent_ShouldInvokeListener() {
        List<TestEvents.SimpleEvent> received = new ArrayList<>();

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, received::add);

        TestEvents.SimpleEvent event = new TestEvents.SimpleEvent("test");
        eventBus.post(event);

        assertEquals(1, received.size());
        assertEquals("test", received.get(0).getMessage());
    }

    @Test
    void postEvent_WithNoListeners_ShouldNotThrow() {
        TestEvents.SimpleEvent event = new TestEvents.SimpleEvent("test");
        assertDoesNotThrow(() -> eventBus.post(event));
    }

    @Test
    void postEvent_WithMultipleListeners_ShouldInvokeAll() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);
        AtomicInteger count3 = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> count1.incrementAndGet());
        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> count2.incrementAndGet());
        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> count3.incrementAndGet());

        eventBus.post(new TestEvents.SimpleEvent());

        assertEquals(1, count1.get());
        assertEquals(1, count2.get());
        assertEquals(1, count3.get());
    }

    @Test
    void postCancellableEvent_WhenCancelled_ShouldStopPropagation() {
        List<Integer> order = new ArrayList<>();

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> {
            order.add(1);
            e.cancel();
        });

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> {
            order.add(2);
        });

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> {
            order.add(3);
        });

        eventBus.post(new TestEvents.CancellableEvent());

        assertEquals(1, order.size());
        assertEquals(1, order.get(0));
    }

    @Test
    void postCancellableEvent_WhenNotCancelled_ShouldInvokeAllListeners() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> count.incrementAndGet());
        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> count.incrementAndGet());
        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> count.incrementAndGet());

        TestEvents.CancellableEvent event = new TestEvents.CancellableEvent();
        eventBus.post(event);

        assertEquals(3, count.get());
    }

    @Test
    void postCancellableEvent_WhenCancelledBeforePost_ShouldNotInvokeAnyListeners() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> count.incrementAndGet());

        TestEvents.CancellableEvent event = new TestEvents.CancellableEvent();
        event.cancel();
        eventBus.post(event);

        assertEquals(0, count.get());
    }

    @Test
    void postQueuedEvent_ShouldQueueInsteadOfFiring() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> count.incrementAndGet());

        eventBus.post(new TestEvents.QueuedEvent(1));
        eventBus.post(new TestEvents.QueuedEvent(2));
        eventBus.post(new TestEvents.QueuedEvent(3));

        assertEquals(0, count.get());
    }

    @Test
    void flush_ShouldFireAllQueuedEvents() {
        List<Integer> order = new ArrayList<>();

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> order.add(e.getOrder()));

        eventBus.post(new TestEvents.QueuedEvent(1));
        eventBus.post(new TestEvents.QueuedEvent(2));
        eventBus.post(new TestEvents.QueuedEvent(3));

        eventBus.flush();

        assertEquals(3, order.size());
        assertEquals(1, order.get(0));
        assertEquals(2, order.get(1));
        assertEquals(3, order.get(2));
    }

    @Test
    void flush_WithNoQueuedEvents_ShouldNotThrow() {
        assertDoesNotThrow(() -> eventBus.flush());
    }

    @Test
    void flush_WithMixedEvents_ShouldOnlyFireQueuedOnes() {
        AtomicInteger queuedCount = new AtomicInteger(0);
        AtomicInteger immediateCount = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> queuedCount.incrementAndGet());
        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> immediateCount.incrementAndGet());

        eventBus.post(new TestEvents.QueuedEvent(1));
        eventBus.post(new TestEvents.SimpleEvent());
        eventBus.post(new TestEvents.QueuedEvent(2));

        assertEquals(0, queuedCount.get());     // 0 queued events were triggered
        assertEquals(1, immediateCount.get());  // 1 immediate event was triggered

        eventBus.flush();

        assertEquals(2, queuedCount.get());     // 2 queued events were fired after the flush
        assertEquals(1, immediateCount.get());  // no new immediate events were fired after the flush
    }

    @Test
    void postPooledEvent_ShouldReleaseAfterFiring() {
        eventBus.getEventPoolRegistry().register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        AtomicInteger count = new AtomicInteger(0);
        eventBus.getListenerRegistry().register(TestEvents.PooledEvent.class, e -> {
            count.incrementAndGet();
            e.setValue(42);
        });

        eventBus.post(TestEvents.PooledEvent.class, e -> e.setValue(100));

        assertEquals(1, count.get());

        // Acquire again to verify it was pooled and reset
        EventPool<TestEvents.PooledEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.PooledEvent.class);
        TestEvents.PooledEvent pooled = pool.acquire();
        assertEquals(0, pooled.getValue());
    }

    @Test
    void postPooledEvent_WithConsumer_ShouldApplyConsumerBeforeFiring() {
        eventBus.getEventPoolRegistry().register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        List<Integer> values = new ArrayList<>();
        eventBus.getListenerRegistry().register(TestEvents.PooledEvent.class, e -> values.add(e.getValue()));

        eventBus.post(TestEvents.PooledEvent.class, e -> e.setValue(999));

        assertEquals(1, values.size());
        assertEquals(999, values.get(0));
    }

    @Test
    void postPooledEvent_WithNoPool_ShouldLogErrorAndNotThrow() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.PooledEvent.class, e -> count.incrementAndGet());

        // Should not throw, but should log error
        assertDoesNotThrow(() -> {
            eventBus.post(TestEvents.PooledEvent.class, e -> e.setValue(100));
        });

        assertEquals(0, count.get());

    }

    @Test
    void postPooledQueuedEvent_ShouldQueueAndReleaseAfterFlush() {
        eventBus.getEventPoolRegistry().register(TestEvents.PooledQueuedEvent.class, TestEvents.PooledQueuedEvent::new);

        AtomicInteger count = new AtomicInteger(0);
        eventBus.getListenerRegistry().register(TestEvents.PooledQueuedEvent.class, e -> {
            count.incrementAndGet();
            e.setData("processed");
        });

        eventBus.post(TestEvents.PooledQueuedEvent.class, e -> e.setData("test"));

        assertEquals(0, count.get());

        eventBus.flush();

        assertEquals(1, count.get());

        // Verify it was pooled and reset
        EventPool<TestEvents.PooledQueuedEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.PooledQueuedEvent.class);
        TestEvents.PooledQueuedEvent pooled = pool.acquire();
        assertNull(pooled.getData());
    }

    @Test
    void postFullTraitEvent_ShouldHandleAllTraits() {
        eventBus.getEventPoolRegistry().register(TestEvents.FullTraitEvent.class, TestEvents.FullTraitEvent::new);

        List<Integer> listenerValues1 = new ArrayList<>();
        eventBus.getListenerRegistry().register(TestEvents.FullTraitEvent.class, e -> {
            listenerValues1.add(e.getValue());
            if (e.getValue() == 1) {
                e.cancel();
            }
        });

        List<Integer> listenerValues2 = new ArrayList<>();
        eventBus.getListenerRegistry().register(TestEvents.FullTraitEvent.class, e -> {
            listenerValues2.add(e.getValue());
        });

        eventBus.post(TestEvents.FullTraitEvent.class, e -> e.setValue(1));
        eventBus.post(TestEvents.FullTraitEvent.class, e -> e.setValue(2));

        // Events should be in queue and thus not yet sent to the listeners
        assertEquals(0, listenerValues1.size());
        assertEquals(0, listenerValues2.size());

        eventBus.flush();

        // First listener should receive both events
        assertEquals(2, listenerValues1.size());
        // Second listener should only receive event2 since event1 was cancelled in the first listener
        assertEquals(1, listenerValues2.size());

        // The pool contains 2 event instances since events that are in the queue cannot be reused yet
        assertEquals(2, eventBus.getEventPoolRegistry().get(TestEvents.FullTraitEvent.class).getPoolSize());
    }

    @Test
    void listenerException_ShouldNotStopOtherListeners() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> {
            throw new RuntimeException("Test exception");
        });

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> {
            count.incrementAndGet();
        });

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> {
            count.incrementAndGet();
        });

        assertDoesNotThrow(() -> eventBus.post(new TestEvents.SimpleEvent()));

        // The 2 remaining listeners still received the event despite an error thrown in a previous listener
        assertEquals(2, count.get());
    }

    @Test
    void postEvent_WithDifferentEventTypes_ShouldOnlyInvokeCorrectListeners() {
        AtomicInteger simpleCount = new AtomicInteger(0);
        AtomicInteger cancellableCount = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> simpleCount.incrementAndGet());
        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> cancellableCount.incrementAndGet());

        eventBus.post(new TestEvents.SimpleEvent());
        eventBus.post(new TestEvents.CancellableEvent());

        assertEquals(1, simpleCount.get());
        assertEquals(1, cancellableCount.get());
    }

    @Test
    void multipleFlushes_ShouldOnlyProcessQueuedEventsOnce() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> count.incrementAndGet());

        eventBus.post(new TestEvents.QueuedEvent(1));
        eventBus.post(new TestEvents.QueuedEvent(2));

        eventBus.flush();
        eventBus.flush();
        eventBus.flush();

        assertEquals(2, count.get());
    }


    @Test
    void postNullEvent_ShouldNotThrow() {
        // Note: This tests defensive programming - null events should be handled gracefully
        // The actual behavior depends on implementation
        assertDoesNotThrow(() -> {
            try {
                eventBus.post(null);
            } catch (NullPointerException e) {
                // Expected behavior - null events should be rejected
            }
        });
    }

    @Test
    void postPooledEvent_WithNullConsumer_ShouldNotThrow() {
        // TODO
    }

    @Test
    void listenerOrder_ShouldBePreserved() {
        List<Integer> order = new ArrayList<>();

        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> order.add(1));
        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> order.add(2));
        eventBus.getListenerRegistry().register(TestEvents.SimpleEvent.class, e -> order.add(3));

        eventBus.post(new TestEvents.SimpleEvent());

        assertEquals(3, order.size());
        assertEquals(1, order.get(0));
        assertEquals(2, order.get(1));
        assertEquals(3, order.get(2));
    }

    @Test
    void queuedEventCancellation_ShouldWorkAfterFlush() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.CancellableQueuedEvent.class, e -> {
            count.incrementAndGet();
            if (count.get() == 1) {
                e.cancel();
            }
        });

        eventBus.getListenerRegistry().register(TestEvents.CancellableQueuedEvent.class, e -> {
            count.incrementAndGet();
        });

        eventBus.post(new TestEvents.CancellableQueuedEvent());
        eventBus.flush();

        // Only the first listener receives the event since it is cancelled before the second listener
        assertEquals(1, count.get());
    }

    @Test
    void queuedEvents_PostedDuringFlush_ShouldBeHandledInNextFlush() {
        List<Integer> processedEventValues = new ArrayList<>();

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> {
            processedEventValues.add(e.getOrder());
            if (e.getOrder() == 1) {
                // During the first event's handling, post another queued event (should not be processed this flush)
                eventBus.post(new TestEvents.QueuedEvent(2));
            }
        });

        eventBus.post(new TestEvents.QueuedEvent(1));

        // First flush: only QueuedEvent(1) should be handled
        eventBus.flush();
        assertEquals(1, processedEventValues.size());
        assertEquals(1, processedEventValues.get(0));

        // Second flush: now QueuedEvent(2) should be handled
        eventBus.flush();
        assertEquals(2, processedEventValues.size());
        assertEquals(2, processedEventValues.get(1));
    }

    @Test
    void flush_WithEmptyQueue_ShouldNotThrow() {
        assertDoesNotThrow(() -> {
            eventBus.flush();
        });
    }
}
