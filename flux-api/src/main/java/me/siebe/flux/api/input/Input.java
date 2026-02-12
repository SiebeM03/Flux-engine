package me.siebe.flux.api.input;

import me.siebe.flux.api.event.EventPoolRegistry;
import me.siebe.flux.api.input.keyboard.Key;
import me.siebe.flux.api.input.keyboard.Keyboard;
import me.siebe.flux.api.input.keyboard.event.KeyPressEvent;
import me.siebe.flux.api.input.keyboard.event.KeyReleaseEvent;
import me.siebe.flux.core.AppContext;

public class Input {
    private static Keyboard keyboard;

    public static void init(Keyboard keyboard) {
        Input.keyboard = keyboard;

        EventPoolRegistry eventPoolRegistry = AppContext.get().getEventBus().getEventPoolRegistry();
        eventPoolRegistry.register(KeyPressEvent.class, KeyPressEvent::new);
        eventPoolRegistry.register(KeyReleaseEvent.class, KeyReleaseEvent::new);
    }

    public static void update() {
        keyboard.update();
    }

    public static Keyboard keyboard() {
        return keyboard;
    }
}
