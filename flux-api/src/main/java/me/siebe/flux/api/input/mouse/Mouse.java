package me.siebe.flux.api.input.mouse;

import me.siebe.flux.api.input.enums.Modifier;
import me.siebe.flux.api.input.enums.MouseButton;
import me.siebe.flux.api.input.mouse.event.DoubleClickEvent;
import me.siebe.flux.api.input.mouse.event.MouseClickEvent;
import me.siebe.flux.api.input.mouse.event.MouseReleaseEvent;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.util.Delta;

import java.util.BitSet;
import java.util.Set;

/**
 * Tracks mouse state: button presses/releases, cursor position, scroll, and double-click detection.
 * <p>
 * Scroll values are provided by the backend (e.g. GLFW's scroll callback). Behaviour depends on the input device:
 * a mouse wheel typically yields discrete steps (e.g. ±1 per notch), while a trackpad may yield
 * high-resolution fractional offsets and more frequent callbacks. Do not assume a fixed scale or event rate.
 */
public class Mouse {

    /** Buttons currently held down. */
    private final BitSet buttonsDown;
    /** Buttons that were pressed this frame (cleared in {@link #update()}). */
    private final BitSet buttonsPressedThisFrame;
    /** Buttons that were released this frame (cleared in {@link #update()}). */
    private final BitSet buttonsReleasedThisFrame;

    /**
     * Normalized X position: 0 = left edge, 1 = right edge.
     * Uses {@link Delta} to track current vs previous value for mouse movement.
     */
    private final Delta<Float> x;
    /**
     * Normalized Y position: 0 = top edge, 1 = bottom edge.
     * Uses {@link Delta} to track current vs previous value for mouse movement.
     */
    private final Delta<Float> y;

    /**
     * Latest normalized position from move callbacks; committed to {@link #x} in {@link #update()}.
     */
    private float pendingNormalizedX;
    /**
     * Latest normalized position from move callbacks; committed to {@link #y} in {@link #update()}.
     */
    private float pendingNormalizedY;

    /**
     * Horizontal scroll offset this frame. Reset in {@link #update()}.
     * Magnitude and sign depend on the input device (mouse wheel vs trackpad) and platform — see the class documentation above.
     */
    private float scrollX;
    /**
     * Vertical scroll offset this frame: positive = scroll up, negative = scroll down. Reset in {@link #update()}.
     * Magnitude and sign depend on the input device (mouse wheel vs trackpad) and platform — see the class documentation above.
     */
    private float scrollY;

    /** Max time in seconds between two left clicks to count as a double click. */
    private static final float DOUBLE_CLICK_THRESHOLD_SECONDS = 0.3f;
    /** Timestamp of the last left click; used for double-click detection. Reset on cursor move. */
    private double lastLeftClickTime = 0;

    protected Mouse() {
        int buttonCount = MouseButton.values().length;
        this.buttonsDown = new BitSet(buttonCount);
        this.buttonsPressedThisFrame = new BitSet(buttonCount);
        this.buttonsReleasedThisFrame = new BitSet(buttonCount);

        this.x = new Delta<>(0f);
        this.y = new Delta<>(0f);
    }

    /**
     * Returns whether the given button is currently held down.
     *
     * @param button the mouse button to check
     * @return true if the button is down
     */
    public boolean isButtonDown(MouseButton button) {
        return buttonsDown.get(button.ordinal());
    }

    public float normalizedX() {
        return x.getCurrentValue();
    }

    public float normalizedY() {
        return y.getCurrentValue();
    }

    public float screenX() {
        return normalizedX() * AppContext.get().getWindow().getWidth();
    }

    public float screenY() {
        return normalizedY() * AppContext.get().getWindow().getHeight();
    }

    public float normalizedDeltaX() {
        // Skip first frame since x.lastValue() will be 0 while x.currentValue() is the actual cursor position.
        // This would return an incorrect delta value on frame 1
        if (AppContext.get().getTimer().getFrameCount() == 0) return 0;
        return (float) x.getDelta();
    }

    public float normalizedDeltaY() {
        // Skip first frame since x.lastValue() will be 0 while x.currentValue() is the actual cursor position.
        // This would return an incorrect delta value on frame 1
        if (AppContext.get().getTimer().getFrameCount() == 0) return 0;
        return (float) y.getDelta();
    }

    public float deltaX() {
        return normalizedDeltaX() * AppContext.get().getWindow().getWidth();
    }

    public float deltaY() {
        return normalizedDeltaY() * AppContext.get().getWindow().getHeight() * -1;
    }

    public float scrollX() {
        return scrollX;
    }

    public float scrollY() {
        return scrollY;
    }


    /**
     * Called once per frame: commits pending position to {@link #x}/{@link #y}, clears per-frame button and scroll state.
     */
    public void update() {
        buttonsPressedThisFrame.clear();
        buttonsReleasedThisFrame.clear();

        x.updateValue(pendingNormalizedX);
        y.updateValue(pendingNormalizedY);
        scrollX = 0;
        scrollY = 0;
    }

    /**
     * Called by the backend when a mouse button is pressed. Posts {@link MouseClickEvent} and possibly
     * {@link DoubleClickEvent} for left button.
     *
     * @param button    the button that was pressed
     * @param modifiers active modifier keys at the time of the press
     */
    protected void onButtonPress(MouseButton button, Set<Modifier> modifiers) {
        if (button == null) return;

        buttonsDown.set(button.ordinal());
        buttonsPressedThisFrame.set(button.ordinal());

        AppContext.get().getEventBus().post(MouseClickEvent.class, e -> e.set(button, modifiers, x.getCurrentValue(), y.getCurrentValue()));

        if (button == MouseButton.MOUSE_LEFT) {
            tryFireDoubleClick(button, modifiers);
        }
    }

    /**
     * If the time since the last left click is within {@link #DOUBLE_CLICK_THRESHOLD_SECONDS}, posts a {@link DoubleClickEvent}.
     */
    private void tryFireDoubleClick(MouseButton button, Set<Modifier> modifiers) {
        double now = AppContext.get().getTimer().getTotalTime();
        if (now - lastLeftClickTime <= DOUBLE_CLICK_THRESHOLD_SECONDS) {
            AppContext.get().getEventBus().post(DoubleClickEvent.class, e -> e.set(button, modifiers, x.getCurrentValue(), y.getCurrentValue()));
        }
        lastLeftClickTime = now;
    }

    /**
     * Called by the backend when a mouse button is released. Posts {@link MouseReleaseEvent}.
     *
     * @param button    the button that was released
     * @param modifiers active modifier keys at the time of the release
     */
    protected void onButtonRelease(MouseButton button, Set<Modifier> modifiers) {
        if (button == null) return;

        buttonsDown.clear(button.ordinal());
        buttonsReleasedThisFrame.set(button.ordinal());

        AppContext.get().getEventBus().post(MouseReleaseEvent.class, e -> e.set(button, modifiers, x.getCurrentValue(), y.getCurrentValue()));
    }

    /**
     * Called by the backend when the cursor moves. Stores normalized position (0..1) in pending fields;
     * they are committed in {@link #update()}. Resets double-click detection so a second click only
     * counts as double-click if the cursor has not moved in-between clicks.
     *
     * @param rawX cursor X in window pixel coordinates
     * @param rawY cursor Y in window pixel coordinates
     */
    protected void onMouseMove(double rawX, double rawY) {
        float width = AppContext.get().getWindow().getWidth();
        float height = AppContext.get().getWindow().getHeight();
        this.pendingNormalizedX = (float) (rawX / width);
        this.pendingNormalizedY = (float) (rawY / height);
        this.lastLeftClickTime = 0;
    }

    /**
     * Called by the backend when scroll input is received (e.g. from GLFW's scroll callback).
     * Values are stored for the current frame and reset in {@link #update()}.
     * <p>
     * Under GLFW, scroll is callback-only (no scroll position, only instantaneous offsets). Behaviour differs by device:
     * a mouse wheel usually produces discrete steps (often ±1 per notch), while a trackpad may produce
     * smooth, high-resolution fractional values and many callbacks per gesture. Scale and event rate are
     * not guaranteed to be consistent across devices or platforms.
     *
     * @param scrollX horizontal scroll offset (positive = right, negative = left)
     * @param scrollY vertical scroll offset (positive = up, negative = down)
     */
    protected void onScroll(double scrollX, double scrollY) {
        this.scrollX = (float) scrollX;
        this.scrollY = (float) scrollY;
    }
}
