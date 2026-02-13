package me.siebe.flux.api.input.keyboard.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.input.enums.Key;
import me.siebe.flux.api.input.enums.Modifier;

import java.util.Set;

abstract class KeyEvent extends Event implements Pooled {
    private Key key;
    private Set<Modifier> modifiers;

    public void set(Key key, Set<Modifier> modifiers) {
        this.key = key;
        this.modifiers = modifiers;
    }

    @Override
    public void reset() {
        key = null;
        modifiers = null;
    }

    public Key getKey() {
        return key;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    public boolean isKey(Key key) {
        if (this.key == null) return false;
        return this.key.equals(key);
    }

    public boolean hasModifier(Modifier modifier) {
        if (this.modifiers == null) return false;
        return modifiers.contains(modifier);
    }
}
