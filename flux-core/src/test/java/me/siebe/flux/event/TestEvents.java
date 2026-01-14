package me.siebe.flux.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.traits.Cancellable;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.event.traits.Queued;

/**
 * Test event classes for comprehensive testing of the event system.
 */
public class TestEvents {

    // Simple event with no traits
    public static class SimpleEvent extends Event {
        private String message;

        public SimpleEvent() {
        }

        public SimpleEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // Cancellable event
    public static class CancellableEvent extends Event implements Cancellable {
        private boolean cancelled = false;
        private String data;

        public CancellableEvent() {
        }

        public CancellableEvent(String data) {
            this.data = data;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    // Pooled event
    public static class PooledEvent extends Event implements Pooled {
        private int value;
        private boolean resetCalled = false;

        public PooledEvent() {
        }

        @Override
        public void reset() {
            value = 0;
            resetCalled = true;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public boolean wasResetCalled() {
            return resetCalled;
        }

        public void clearResetFlag() {
            resetCalled = false;
        }
    }

    // Queued event
    public static class QueuedEvent extends Event implements Queued {
        private int order;

        public QueuedEvent() {
        }

        public QueuedEvent(int order) {
            this.order = order;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }

    // Event with all traits
    public static class FullTraitEvent extends Event implements Cancellable, Pooled, Queued {
        private boolean cancelled = false;
        private int value;
        private int resetCount = 0;

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @Override
        public void reset() {
            value = 0;
            cancelled = false;
            resetCount++;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getResetCount() {
            return resetCount;
        }
    }

    // Pooled and Queued event
    public static class PooledQueuedEvent extends Event implements Pooled, Queued {
        private String data;
        private int resetCount = 0;

        @Override
        public void reset() {
            data = null;
            resetCount++;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public int getResetCount() {
            return resetCount;
        }
    }

    // Cancellable and Queued event
    public static class CancellableQueuedEvent extends Event implements Cancellable, Queued {
        private boolean cancelled = false;
        private String message;

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
