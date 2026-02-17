package me.siebe.flux.api.input.enums;

/**
 * Modifier keys (shift, control, alt, etc.) that can be held during keyboard or mouse events.
 */
public enum Modifier {
    SHIFT(Key.KEY_LEFT_SHIFT, Key.KEY_RIGHT_SHIFT),
    CONTROL(Key.KEY_LEFT_CONTROL, Key.KEY_RIGHT_CONTROL),
    ALT(Key.KEY_LEFT_ALT, Key.KEY_RIGHT_ALT),
    SUPER(Key.KEY_LEFT_SUPER, Key.KEY_RIGHT_SUPER),
    CAPS_LOCK(Key.KEY_CAPS_LOCK),
    NUM_LOCK(Key.KEY_NUM_LOCK);

    private final Key[] keys;

    Modifier(Key... keys) {
        this.keys = keys;
    }

    public Key[] getKeys() {
        return keys;
    }
}
