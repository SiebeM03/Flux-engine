package me.siebe.flux.event;

import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.event.EventPool;
import me.siebe.flux.api.event.traits.Cancellable;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.event.traits.Queued;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class EventTraitsTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new DefaultEventBus();
    }

    // ========== Cancellable Trait Tests ==========

    @Test
    void cancellable_DefaultState_ShouldNotBeCancelled() {
        TestEvents.CancellableEvent event = new TestEvents.CancellableEvent();

        assertFalse(event.isCancelled());
    }

    @Test
    void cancellable_SetCancelled_ShouldUpdateState() {
        TestEvents.CancellableEvent event = new TestEvents.CancellableEvent();

        event.setCancelled(true);
        assertTrue(event.isCancelled());

        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    void cancellable_CancelMethod_ShouldSetCancelled() {
        TestEvents.CancellableEvent event = new TestEvents.CancellableEvent();

        event.cancel();
        assertTrue(event.isCancelled());
    }

    @Test
    void cancellable_WhenCancelled_ShouldStopEventPropagation() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> {
            count.incrementAndGet();
            e.cancel();
        });

        eventBus.getListenerRegistry().register(TestEvents.CancellableEvent.class, e -> {
            count.incrementAndGet();
        });

        eventBus.post(new TestEvents.CancellableEvent());

        assertEquals(1, count.get());
    }

    @Test
    void cancellable_MultipleCancellations_ShouldWork() {
        TestEvents.CancellableEvent event = new TestEvents.CancellableEvent();

        event.cancel();
        assertTrue(event.isCancelled());

        event.setCancelled(false);
        assertFalse(event.isCancelled());

        event.cancel();
        assertTrue(event.isCancelled());
    }

    // ========== Pooled Trait Tests ==========

    @Test
    void pooled_Reset_ShouldResetState() {
        TestEvents.PooledEvent event = new TestEvents.PooledEvent();

        event.setValue(100);
        event.reset();

        assertEquals(0, event.getValue());
    }

    @Test
    void pooled_Reset_ShouldBeCalledOnAcquire() {
        eventBus.getEventPoolRegistry().register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        EventPool<TestEvents.PooledEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.PooledEvent.class);
        TestEvents.PooledEvent event1 = pool.acquire();
        event1.setValue(50);
        pool.release(event1);

        TestEvents.PooledEvent event2 = pool.acquire();
        assertEquals(0, event2.getValue());
    }

    @Test
    void pooled_Reset_ShouldBeCalledOnRelease() {
        eventBus.getEventPoolRegistry().register(TestEvents.PooledEvent.class, TestEvents.PooledEvent::new);

        me.siebe.flux.api.event.EventPool<TestEvents.PooledEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.PooledEvent.class);
        TestEvents.PooledEvent event = pool.acquire();
        event.setValue(75);
        event.clearResetFlag();

        pool.release(event);

        assertTrue(event.wasResetCalled());
        assertEquals(0, event.getValue());
    }

    @Test
    void pooled_MultipleResets_ShouldWork() {
        TestEvents.PooledEvent event = new TestEvents.PooledEvent();

        event.setValue(10);
        event.reset();
        assertEquals(0, event.getValue());

        event.setValue(20);
        event.reset();
        assertEquals(0, event.getValue());
    }

    // ========== Queued Trait Tests ==========

    @Test
    void queued_Event_ShouldBeQueuedNotFired() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> count.incrementAndGet());

        eventBus.post(new TestEvents.QueuedEvent());

        assertEquals(0, count.get());
    }

    @Test
    void queued_Event_ShouldFireOnFlush() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> count.incrementAndGet());

        eventBus.post(new TestEvents.QueuedEvent());
        eventBus.post(new TestEvents.QueuedEvent());

        eventBus.flush();

        assertEquals(2, count.get());
    }

    @Test
    void queued_Event_ShouldMaintainOrder() {
        AtomicInteger lastOrder = new AtomicInteger(-1);

        eventBus.getListenerRegistry().register(TestEvents.QueuedEvent.class, e -> {
            int currentOrder = e.getOrder();
            assertTrue(currentOrder > lastOrder.get(), "Events should be processed in order");
            lastOrder.set(currentOrder);
        });

        eventBus.post(new TestEvents.QueuedEvent(1));
        eventBus.post(new TestEvents.QueuedEvent(2));
        eventBus.post(new TestEvents.QueuedEvent(3));

        eventBus.flush();
    }

    // ========== Combined Traits Tests ==========

    @Test
    void fullTraitEvent_ShouldHaveAllTraits() {
        TestEvents.FullTraitEvent event = new TestEvents.FullTraitEvent();

        assertInstanceOf(Cancellable.class, event);
        assertInstanceOf(Pooled.class, event);
        assertInstanceOf(Queued.class, event);
    }

    @Test
    void fullTraitEvent_Reset_ShouldResetCancelledState() {
        TestEvents.FullTraitEvent event = new TestEvents.FullTraitEvent();

        event.cancel();
        assertTrue(event.isCancelled());

        event.reset();
        assertFalse(event.isCancelled());
    }

    @Test
    void fullTraitEvent_Reset_ShouldResetValue() {
        TestEvents.FullTraitEvent event = new TestEvents.FullTraitEvent();

        event.setValue(100);
        event.reset();

        assertEquals(0, event.getValue());
    }

    @Test
    void fullTraitEvent_Reset_ShouldIncrementResetCount() {
        TestEvents.FullTraitEvent event = new TestEvents.FullTraitEvent();

        assertEquals(0, event.getResetCount());

        event.reset();
        assertEquals(1, event.getResetCount());

        event.reset();
        assertEquals(2, event.getResetCount());
    }

    @Test
    void fullTraitEvent_ShouldBeQueued() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getEventPoolRegistry().register(TestEvents.FullTraitEvent.class, TestEvents.FullTraitEvent::new);
        eventBus.getListenerRegistry().register(TestEvents.FullTraitEvent.class, e -> count.incrementAndGet());

        eventBus.post(TestEvents.FullTraitEvent.class, e -> e.setValue(1));

        assertEquals(0, count.get());

        eventBus.flush();

        assertEquals(1, count.get());
    }

    @Test
    void fullTraitEvent_ShouldBeCancellable() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getEventPoolRegistry().register(TestEvents.FullTraitEvent.class, TestEvents.FullTraitEvent::new);
        eventBus.getListenerRegistry().register(TestEvents.FullTraitEvent.class, e -> {
            count.incrementAndGet();
            e.cancel();
        });
        eventBus.getListenerRegistry().register(TestEvents.FullTraitEvent.class, e -> {
            count.incrementAndGet();
        });

        eventBus.post(TestEvents.FullTraitEvent.class, e -> e.setValue(1));
        eventBus.flush();

        assertEquals(1, count.get());
    }

    @Test
    void fullTraitEvent_ShouldBePooled() throws Exception {
        eventBus.getEventPoolRegistry().register(TestEvents.FullTraitEvent.class, TestEvents.FullTraitEvent::new);

        me.siebe.flux.api.event.EventPool<TestEvents.FullTraitEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.FullTraitEvent.class);
        TestEvents.FullTraitEvent event1 = pool.acquire();
        event1.setValue(50);
        pool.release(event1);

        TestEvents.FullTraitEvent event2 = pool.acquire();
        assertSame(event1, event2);
        assertEquals(0, event2.getValue());
        assertFalse(event2.isCancelled());
    }

    @Test
    void pooledQueuedEvent_ShouldBePooledAndQueued() throws Exception {
        eventBus.getEventPoolRegistry().register(TestEvents.PooledQueuedEvent.class, TestEvents.PooledQueuedEvent::new);

        AtomicInteger count = new AtomicInteger(0);
        eventBus.getListenerRegistry().register(TestEvents.PooledQueuedEvent.class, e -> count.incrementAndGet());

        eventBus.post(TestEvents.PooledQueuedEvent.class, e -> e.setData("test"));

        assertEquals(0, count.get());

        eventBus.flush();

        assertEquals(1, count.get());

        // Verify pooling
        me.siebe.flux.api.event.EventPool<TestEvents.PooledQueuedEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.PooledQueuedEvent.class);
        TestEvents.PooledQueuedEvent pooled = pool.acquire();
        assertNull(pooled.getData());
    }

    @Test
    void cancellableQueuedEvent_ShouldBeCancellableAndQueued() {
        AtomicInteger count = new AtomicInteger(0);

        eventBus.getListenerRegistry().register(TestEvents.CancellableQueuedEvent.class, e -> {
            count.incrementAndGet();
            e.cancel();
        });
        eventBus.getListenerRegistry().register(TestEvents.CancellableQueuedEvent.class, e -> {
            count.incrementAndGet();
        });

        eventBus.post(new TestEvents.CancellableQueuedEvent());

        assertEquals(0, count.get());

        eventBus.flush();

        assertEquals(1, count.get());
    }

    @Test
    void pooledEvent_WithQueuedTrait_ShouldQueueAndPool() throws Exception {
        eventBus.getEventPoolRegistry().register(TestEvents.PooledQueuedEvent.class, TestEvents.PooledQueuedEvent::new);

        me.siebe.flux.api.event.EventPool<TestEvents.PooledQueuedEvent> pool = eventBus.getEventPoolRegistry().get(TestEvents.PooledQueuedEvent.class);
        TestEvents.PooledQueuedEvent event1 = pool.acquire();
        event1.setData("first");
        pool.release(event1);

        AtomicInteger count = new AtomicInteger(0);
        eventBus.getListenerRegistry().register(TestEvents.PooledQueuedEvent.class, e -> {
            count.incrementAndGet();
            assertEquals("second", e.getData());
        });

        eventBus.post(TestEvents.PooledQueuedEvent.class, e -> e.setData("second"));

        assertEquals(0, count.get());

        eventBus.flush();

        assertEquals(1, count.get());

        // Verify it was pooled
        TestEvents.PooledQueuedEvent pooled = pool.acquire();
        assertNull(pooled.getData());
    }
}
