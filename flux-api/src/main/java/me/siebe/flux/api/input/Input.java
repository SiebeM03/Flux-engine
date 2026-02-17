package me.siebe.flux.api.input;

import me.siebe.flux.api.event.EventPoolRegistry;
import me.siebe.flux.api.input.keyboard.Keyboard;
import me.siebe.flux.api.input.keyboard.event.KeyPressEvent;
import me.siebe.flux.api.input.keyboard.event.KeyReleaseEvent;
import me.siebe.flux.api.input.mouse.Mouse;
import me.siebe.flux.api.input.mouse.event.DoubleClickEvent;
import me.siebe.flux.api.input.mouse.event.MouseClickEvent;
import me.siebe.flux.api.input.mouse.event.MouseReleaseEvent;
import me.siebe.flux.core.AppContext;

/**
 * Central access point for keyboard and mouse input.
 * <p>
 * Call {@link #init(Mouse, Keyboard)} once at startup with backend-specific implementations (e.g. GLFW).
 * Call {@link #nextFrame()} once per frame so per-frame state (key press/release, mouse deltas, scroll) is updated.
 * Use {@link #keyboard()} and {@link #mouse()} to poll state or subscribe to input events on the event bus.
 */
public class Input {
    private static Mouse mouse;
    private static Keyboard keyboard;

    /**
     * Initialises the input system with the given keyboard and mouse implementations and registers
     * input event types with the event bus. Must be called once before using {@link #keyboard()} or {@link #mouse()}.
     *
     * @param mouse    backend-specific mouse implementation (e.g. GLFW)
     * @param keyboard backend-specific keyboard implementation (e.g. GLFW)
     */
    public static void init(Mouse mouse, Keyboard keyboard) {
        EventPoolRegistry eventPoolRegistry = AppContext.get().getEventBus().getEventPoolRegistry();

        Input.mouse = mouse;
        eventPoolRegistry.register(MouseClickEvent.class, MouseClickEvent::new);
        eventPoolRegistry.register(MouseReleaseEvent.class, MouseReleaseEvent::new);
        eventPoolRegistry.register(DoubleClickEvent.class, DoubleClickEvent::new);

        Input.keyboard = keyboard;
        eventPoolRegistry.register(KeyPressEvent.class, KeyPressEvent::new);
        eventPoolRegistry.register(KeyReleaseEvent.class, KeyReleaseEvent::new);
    /**
     * Advances input state to the next frame. Clears per-frame flags (key/mouse press/release, scroll, deltas).
     * Must be called once per frame, typically done by FluxApplication before the window is updated.
     */
    public static void endFrame() {
        keyboard.endFrame();
        mouse.endFrame();
    }

    /**
     * Update input states from the current frame.
     * Must be called once per frame, typically done by FluxApplication after the window is updated.
     */
    public static void nextFrame() {
        keyboard.nextFrame();
        mouse.nextFrame();
    public static void beginFrame() {
        keyboard.beginFrame();
        mouse.beginFrame();
    }

    /**
     * Returns the current keyboard instance. Valid after {@link #init(Mouse, Keyboard)}.
     *
     * @return the keyboard implementation
     */
    public static Keyboard keyboard() {
        return keyboard;
    }

    /**
     * Returns the current mouse instance. Valid after {@link #init(Mouse, Keyboard)}.
     *
     * @return the mouse implementation
     */
    public static Mouse mouse() {
        return mouse;
    }
}
