package me.siebe.flux.test.harness;

/**
 * Callback invoked each frame after input beginFrame and before event flush and system update.
 * Use to script input (e.g. press key at frame 5) or assert state mid-simulation.
 */
@FunctionalInterface
public interface PerFrameCallback {
    /**
     * @param frameIndex 0-based index of the current frame
     * @param harness    the test harness (e.g. to call getFakeKeyboard().press(...))
     */
    void onFrame(int frameIndex, FluxTestHarness harness);
}