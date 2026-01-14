package me.siebe.flux.event;

import me.siebe.flux.api.event.EventBus;
import me.siebe.flux.api.event.EventBusProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EventBusProviderTest {

    @BeforeEach
    @AfterEach
    void resetProvider() throws Exception {
        // Reset the singleton instance using reflection
        Field instanceField = EventBusProvider.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void init_ShouldStoreEventBus() {
        EventBus eventBus = new DefaultEventBus();

        EventBusProvider.init(eventBus);

        EventBus retrieved = EventBusProvider.get();
        assertSame(eventBus, retrieved);
    }

    @Test
    void get_AfterInit_ShouldReturnSameInstance() {
        EventBus eventBus = new DefaultEventBus();

        EventBusProvider.init(eventBus);

        EventBus retrieved1 = EventBusProvider.get();
        EventBus retrieved2 = EventBusProvider.get();

        assertSame(retrieved1, retrieved2);
        assertSame(eventBus, retrieved1);
    }

    @Test
    void init_WhenAlreadyInitialized_ShouldThrowException() {
        EventBus eventBus1 = new DefaultEventBus();
        EventBus eventBus2 = new DefaultEventBus();

        EventBusProvider.init(eventBus1);

        assertThrows(IllegalStateException.class, () -> EventBusProvider.init(eventBus2));
    }

    @Test
    void get_BeforeInit_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, EventBusProvider::get);
    }

    @Test
    void init_WithNullEventBus_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> EventBusProvider.init(null));
    }

    @Test
    void init_ShouldOnlyAllowOneInitialization() {
        EventBus eventBus1 = new DefaultEventBus();
        EventBus eventBus2 = new DefaultEventBus();

        EventBusProvider.init(eventBus1);

        try {
            EventBusProvider.init(eventBus2);
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }

        // Should still return the first event bus
        EventBus retrieved = EventBusProvider.get();
        assertSame(eventBus1, retrieved);
    }
}

