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

public class Input {
    private static Mouse mouse;
    private static Keyboard keyboard;

    public static void init(Mouse mouse, Keyboard keyboard) {
        EventPoolRegistry eventPoolRegistry = AppContext.get().getEventBus().getEventPoolRegistry();

        Input.mouse = mouse;
        eventPoolRegistry.register(MouseClickEvent.class, MouseClickEvent::new);
        eventPoolRegistry.register(MouseReleaseEvent.class, MouseReleaseEvent::new);
        eventPoolRegistry.register(DoubleClickEvent.class, DoubleClickEvent::new);

        Input.keyboard = keyboard;
        eventPoolRegistry.register(KeyPressEvent.class, KeyPressEvent::new);
        eventPoolRegistry.register(KeyReleaseEvent.class, KeyReleaseEvent::new);
    }

    public static void update() {
        keyboard.update();
        mouse.update();
    }

    public static Keyboard keyboard() {
        return keyboard;
    }

    public static Mouse mouse() {
        return mouse;
    }
}
