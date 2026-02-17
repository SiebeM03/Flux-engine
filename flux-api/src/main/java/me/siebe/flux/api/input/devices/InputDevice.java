package me.siebe.flux.api.input.devices;

public interface InputDevice {
    /**
     * Called once per frame right before the window polls for events.
     */
    void endFrame();

    /**
     * Called once per frame right after the window polls for events.
     */
    void beginFrame();
}
