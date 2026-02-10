package me.siebe.flux.api.event.common;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.event.traits.Queued;

/**
 * Event fired when the framebuffer (e.g. back buffer or render target) size changes.
 * <p>
 * This may differ from the window size on high-DPI displays. The event is {@link Pooled} and
 * {@link Queued}: use {@link #set(int, int, int, int)} to set dimensions before posting.
 *
 * @see me.siebe.flux.api.event.EventBus
 * @see me.siebe.flux.api.event.traits.Pooled
 * @see me.siebe.flux.api.event.traits.Queued
 */
public class FramebufferResizeEvent extends Event implements Pooled, Queued {
    private int oldWidth;
    private int oldHeight;
    private int newWidth;
    private int newHeight;

    @Override
    public void reset() {
    }

    /**
     * Sets the old and new framebuffer dimensions for this event.
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

    /** Returns the framebuffer width before the resize. */
    public int getOldWidth() {
        return oldWidth;
    }

    /** Returns the framebuffer height before the resize. */
    public int getOldHeight() {
        return oldHeight;
    }

    /** Returns the framebuffer width after the resize. */
    public int getNewWidth() {
        return newWidth;
    }

    /** Returns the framebuffer height after the resize. */
    public int getNewHeight() {
        return newHeight;
    }
}
