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
 * Base implementation of {@link Mouse} that tracks button state, cursor position (via {@link Delta}),
 * scroll, and double-click detection. Posts {@link MouseClickEvent}, {@link MouseReleaseEvent}, and
 * {@link DoubleClickEvent} to the event bus. Backend implementations (e.g. GLFW) should call
 * {@link #onButtonPress}, {@link #onButtonRelease}, {@link #onMouseMove}, and {@link #onScroll} from
 * their callbacks, and {@link #setInitialMousePos} when the cursor position is first known.
 * <p>
 * Scroll values are device-dependent: mouse wheels typically give discrete steps, trackpads may give
 * high-resolution fractional values and more frequent callbacks. See {@link #onScroll} for details.
 */
public abstract class AbstractMouse implements Mouse {
    // TODO could me narrowed down to 2 BitSets (same as AbstractKeyboard)
    /** Buttons currently held down. */
    private final BitSet buttonsDown;
    /** Buttons that were pressed this frame (cleared in {@link #nextFrame()}). */
    private final BitSet buttonsPressedThisFrame;
    /** Buttons that were released this frame (cleared in {@link #nextFrame()}). */
    private final BitSet buttonsReleasedThisFrame;

    /**
     * Normalized X position: 0 = left edge, 1 = right edge.
     * Uses {@link Delta} to track current vs previous value for mouse movement.
     */
    private Delta<Float> x;
    /**
     * Normalized Y position: 0 = top edge, 1 = bottom edge.
     * Uses {@link Delta} to track current vs previous value for mouse movement.
     */
    private Delta<Float> y;

    /**
     * Latest normalized position from move callbacks; committed to {@link #x} in {@link #nextFrame()}.
     */
    private float pendingNormalizedX;
    /**
     * Latest normalized position from move callbacks; committed to {@link #y} in {@link #nextFrame()}.
     */
    private float pendingNormalizedY;

    /**
     * Horizontal scroll offset this frame. Reset in {@link #nextFrame()}.
     * Magnitude and sign depend on the input device (mouse wheel vs trackpad) and platform — see the class documentation above.
     */
    private float scrollX;
    /**
     * Vertical scroll offset this frame: positive = scroll up, negative = scroll down. Reset in {@link #nextFrame()}.
     * Magnitude and sign depend on the input device (mouse wheel vs trackpad) and platform — see the class documentation above.
     */
    private float scrollY;

    /** Max time in seconds between two left clicks to count as a double click. */
    private static final float DOUBLE_CLICK_THRESHOLD_SECONDS = 0.3f;
    /** Timestamp of the last left click; used for double-click detection. Reset on cursor move. */
    private double lastLeftClickTime = 0;

    protected AbstractMouse() {
        int buttonCount = MouseButton.values().length;
        this.buttonsDown = new BitSet(buttonCount);
        this.buttonsPressedThisFrame = new BitSet(buttonCount);
        this.buttonsReleasedThisFrame = new BitSet(buttonCount);
    }

    /**
     * Sets the initial cursor position (e.g. when the window gains focus or the backend first reports position).
     * Initialises the position and delta state so the first frame does not produce a large bogus delta.
     *
     * @param initialRawX cursor X in window pixel coordinates
     * @param initialRawY cursor Y in window pixel coordinates
     */
    protected void setInitialMousePos(double initialRawX, double initialRawY) {
        this.pendingNormalizedX = (float) initialRawX / AppContext.get().getWindow().getWidth();
        this.pendingNormalizedY = (float) initialRawY / AppContext.get().getWindow().getHeight();
        this.x = new Delta<>(pendingNormalizedX);
        this.y = new Delta<>(pendingNormalizedY);
    }


    @Override
    public boolean isButtonDown(MouseButton button) {
        return buttonsDown.get(button.ordinal());
    }

    @Override
    public boolean isButtonPressed(MouseButton button) {
        return buttonsPressedThisFrame.get(button.ordinal());
    }

    @Override
    public boolean isButtonReleased(MouseButton button) {
        return buttonsReleasedThisFrame.get(button.ordinal());
    }

    @Override
    public float normalizedX() {
        return x.getCurrentValue();
    }

    @Override
    public float normalizedY() {
        return y.getCurrentValue();
    }

    @Override
    public float screenX() {
        return normalizedX() * AppContext.get().getWindow().getWidth();
    }

    @Override
    public float screenY() {
        return normalizedY() * AppContext.get().getWindow().getHeight();
    }

    @Override
    public float normalizedDeltaX() {
        return (float) x.getDelta();
    }

    @Override
    public float normalizedDeltaY() {
        return (float) y.getDelta();
    }

    @Override
    public float deltaX() {
        return normalizedDeltaX() * AppContext.get().getWindow().getWidth();
    }

    @Override
    public float deltaY() {
        return normalizedDeltaY() * AppContext.get().getWindow().getHeight() * -1;
    }

    @Override
    public float scrollX() {
        return scrollX;
    }

    @Override
    public float scrollY() {
        return scrollY;
    }


    /**
     * Called once per frame: commits pending position to {@link #x}/{@link #y}, clears per-frame button and scroll state.
     */
    @Override
    public void nextFrame() {
        x.updateValue(pendingNormalizedX);
        y.updateValue(pendingNormalizedY);

        buttonsPressedThisFrame.clear();
        buttonsReleasedThisFrame.clear();
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
     * they are committed in {@link #nextFrame()}. Resets double-click detection so a second click only
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
     * Values are stored for the current frame and reset in {@link #nextFrame()}.
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
