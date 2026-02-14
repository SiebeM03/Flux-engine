package me.siebe.flux.api.input.mouse;

import me.siebe.flux.api.input.enums.MouseButton;

/**
 * Read-only view of mouse state: buttons, cursor position (normalized and screen space), movement deltas, and scroll.
 * <p>
 * Position and deltas are updated when the backend calls into the implementation (e.g. {@link AbstractMouse})
 * and are committed at frame boundaries when {@link #nextFrame()} is called. Scroll values are device-dependent
 * (mouse wheel vs trackpad); see the implementation class for details.
 */
public interface Mouse {
    /**
     * Returns whether the given button is currently held down.
     *
     * @param button the mouse button to check
     * @return true if the button is down
     */
    boolean isButtonDown(MouseButton button);

    /**
     * Returns whether the given button is pressed this frame.
     *
     * @param button the mouse button to check
     * @return true if the button is pressed this frame
     */
    boolean isButtonPressed(MouseButton button);

    /**
     * Returns whether the given button is released this frame.
     *
     * @param button the mouse button to check
     * @return true if the button is released this frame
     */
    boolean isButtonReleased(MouseButton button);

    /**
     * Current cursor X in normalized coordinates: 0 = left edge of window, 1 = right edge.
     *
     * @return normalized X in [0, 1]
     */
    float normalizedX();

    /**
     * Current cursor Y in normalized coordinates: 0 = top edge of window, 1 = bottom edge.
     *
     * @return normalized Y in [0, 1]
     */
    float normalizedY();

    /**
     * Current cursor X in window pixel coordinates.
     *
     * @return X in screen/pixel space
     */
    float screenX();

    /**
     * Current cursor Y in window pixel coordinates.
     *
     * @return Y in screen/pixel space
     */
    float screenY();

    /**
     * Change in normalized X since the previous frame (current − previous).
     *
     * @return normalized delta X
     */
    float normalizedDeltaX();

    /**
     * Change in normalized Y since the previous frame (current − previous).
     *
     * @return normalized delta Y
     */
    float normalizedDeltaY();

    /**
     * Change in cursor X in pixel space since the previous frame.
     *
     * @return delta X in pixels
     */
    float deltaX();

    /**
     * Change in cursor Y in pixel space since the previous frame.
     *
     * @return delta Y in pixels
     */
    float deltaY();

    /**
     * Horizontal scroll offset this frame. Sign and magnitude are device- and platform-dependent.
     *
     * @return scroll X (positive = right, negative = left typically)
     */
    float scrollX();

    /**
     * Vertical scroll offset this frame. Sign and magnitude are device- and platform-dependent.
     *
     * @return scroll Y (positive = up, negative = down typically)
     */
    float scrollY();

    /**
     * Advances internal state to the next frame (commits position, clears per-frame scroll/deltas).
     * Called by {@link me.siebe.flux.api.input.Input#nextFrame()}; this should not be called manually.
     */
    void nextFrame();
}
