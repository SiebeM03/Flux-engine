package me.siebe.flux.api.event.common;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.event.traits.Queued;

/**
 * Event fired when the window's logical size (in pixels or units) changes.
 * <p>
 * This event is {@link Pooled} and {@link Queued}: it is typically acquired from a pool and
 * delivered when the event bus is flushed. Use {@link #set(int, int, int, int)} to configure
 * dimensions before posting.
 *
 * @see me.siebe.flux.api.event.EventBus
 * @see me.siebe.flux.api.event.traits.Pooled
 * @see me.siebe.flux.api.event.traits.Queued
 */
public class WindowResizeEvent extends Event implements Pooled, Queued {
    private int oldWidth;
    private int oldHeight;
    private int newWidth;
    private int newHeight;

    @Override
    public void reset() {
    }

    /**
     * Sets the old and new window dimensions for this event.
     *
     * @param oldWidth  width before resize
     * @param oldHeight height before resize
     * @param newWidth  width after resize
     * @param newHeight height after resize
     */
    public void set(int oldWidth, int oldHeight, int newWidth, int newHeight) {
        this.oldWidth = oldWidth;
        this.oldHeight = oldHeight;
        this.newWidth = newWidth;
        this.newHeight = newHeight;
    }

    /** Returns the window width before the resize. */
    public int getOldWidth() {
        return oldWidth;
    }

    /** Returns the window height before the resize. */
    public int getOldHeight() {
        return oldHeight;
    }

    /** Returns the window width after the resize. */
    public int getNewWidth() {
        return newWidth;
    }

    /** Returns the window height after the resize. */
    public int getNewHeight() {
        return newHeight;
    }
}
