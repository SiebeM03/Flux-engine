package me.siebe.flux.api.input.mouse.event;

import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.input.enums.Modifier;
import me.siebe.flux.api.input.enums.MouseButton;

import java.util.Set;

abstract class MouseButtonEvent extends Event implements Pooled {
    private MouseButton button;
    private Set<Modifier> modifiers;
    private float x;
    private float y;

    public void set(MouseButton button, Set<Modifier> modifiers, float x, float y) {
        this.button = button;
        this.modifiers = modifiers;
        this.x = x;
        this.y = y;
    }

    @Override
    public void reset() {
        this.button = null;
        this.modifiers = null;
        this.x = 0;
        this.y = 0;
    }

    public MouseButton getButton() {
        return button;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    public boolean isButton(MouseButton button) {
        if (this.button == null) return false;
        return this.button.equals(button);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean hasModifier(Modifier modifier) {
        if (this.modifiers == null) return false;
        return this.modifiers.contains(modifier);
    }
}
