package me.siebe.flux.ui.builder;

import me.siebe.flux.api.ui.UIElement;
import me.siebe.flux.ui.components.UiContainer;
import me.siebe.flux.util.FluxColor;

import java.util.List;

public class UiBuilder {
    private final UIElement element;

    private UiBuilder(final UIElement element) {
        this.element = element;
    }

    public static UiBuilder container() {
        return new UiBuilder(new UiContainer());
    }

    public UiBuilder width(float width) {
        element.setWidth(width);
        return this;
    }
    public UiBuilder height(float height) {
        element.setHeight(height);
        return this;
    }
    public UiBuilder size(float width, float height) {
        element.setSize(width, height);
        return this;
    }
    public UiBuilder x(float x) {
        element.setPosition(x, element.getY());
        return this;
    }
    public UiBuilder y(float y) {
        element.setPosition(element.getX(), y);
        return this;
    }
    public UiBuilder position(float x, float y) {
        element.setPosition(x, y);
        return this;
    }
    public UiBuilder background(FluxColor color) {
        element.setBackground(color);
        return this;
    }
    public UiBuilder opacity(float opacity) {
        element.setOpacity(opacity);
        return this;
    }
    public UiBuilder child(UIElement child) {
        element.add(child);
        return this;
    }

    public void onClick(Runnable action) {

    }
    public void onHover(Runnable action) {

    }
    public void onMouseEnter(Runnable action) {

    }
    public void onMouseExit(Runnable action) {

    }
    public void onFocus(Runnable action) {

    }
    public void onBlur(Runnable action) {

    }

    public UIElement build() {
        return element;
    }
}
