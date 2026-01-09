package me.siebe.flux.api.event.traits;

// Pooled events, useful for high frequency events (window resize, mouse input, ...)
public interface Pooled {
    void reset();
}
