package me.siebe.flux.api.input.keyboard;

import me.siebe.flux.api.input.enums.Key;
import me.siebe.flux.api.input.enums.Modifier;
import me.siebe.flux.api.input.keyboard.event.KeyPressEvent;
import me.siebe.flux.api.input.keyboard.event.KeyReleaseEvent;
import me.siebe.flux.core.AppContext;

import java.util.BitSet;
import java.util.Set;

public abstract class Keyboard {
    private final BitSet keysPressedThisFrame;
    private final BitSet keysRepeatedThisFrame;
    private final BitSet keysReleasedThisFrame;
    private final BitSet keysDown;

    protected Keyboard() {
        int keyCount = Key.values().length;
        this.keysPressedThisFrame = new BitSet(keyCount);
        this.keysRepeatedThisFrame = new BitSet(keyCount);
        this.keysReleasedThisFrame = new BitSet(keyCount);
        this.keysDown = new BitSet(keyCount);
    }

    public boolean isKeyDown(Key key) {
        return keysDown.get(key.ordinal());
    }

    public void update() {
        keysReleasedThisFrame.clear();
        keysPressedThisFrame.clear();
        keysRepeatedThisFrame.clear();
    }

    protected void onKeyPress(Key key, Set<Modifier> modifiers) {
        if (key == null) return;

        keysDown.set(key.ordinal());
        keysPressedThisFrame.set(key.ordinal());

        AppContext.get().getEventBus().post(KeyPressEvent.class, e -> e.set(key, modifiers));
    }

    protected void onKeyRelease(Key key, Set<Modifier> modifiers) {
        if (key == null) return;

        keysDown.clear(key.ordinal());
        keysReleasedThisFrame.set(key.ordinal());

        AppContext.get().getEventBus().post(KeyReleaseEvent.class, e -> e.set(key, modifiers));
    }

    protected void onKeyRepeat(Key key, Set<Modifier> modifiers) {
        if (key == null) return;

        keysRepeatedThisFrame.set(key.ordinal());
    }
}
