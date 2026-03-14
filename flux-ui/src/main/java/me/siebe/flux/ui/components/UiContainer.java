package me.siebe.flux.ui.components;

import me.siebe.flux.api.ui.UIElement;
import me.siebe.flux.util.FluxColor;

import java.util.ArrayList;
import java.util.List;

public class UiContainer implements UIElement {
    protected float width;
    protected float height;
    protected float x = 0;
    protected float y = 0;
    protected FluxColor color = FluxColor.WHITE.copy();
    protected List<UIElement> children = new ArrayList<>();

    @Override
    public void setWidth(float width) {
        this.width = width;
    }
    @Override
    public void setHeight(float height) {
        this.height = height;
    }
    @Override
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
    @Override
    public float getWidth() {
        return width;
    }
    @Override
    public float getHeight() {
        return height;
    }
    @Override
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    @Override
    public void setAbsolute(boolean isAbsolute) {

    }
    @Override
    public float getX() {
        return x;
    }
    @Override
    public float getY() {
        return y;
    }
    @Override
    public void setVisible(boolean visible) {

    }
    @Override
    public boolean isVisible() {
        return true;
    }
    @Override
    public void setEnabled(boolean enabled) {

    }
    @Override
    public boolean isEnabled() {
        return false;
    }
    @Override
    public void setBackground(FluxColor color) {
        this.color = color;
    }
    @Override
    public FluxColor getBackground() {
        return color;
    }
    @Override
    public void setOpacity(float opacity) {
        this.color.setA(opacity);
    }
    @Override
    public UIElement getParent() {
        return null;
    }
    @Override
    public void add(UIElement child) {
        children.add(child);
    }
    @Override
    public void addAll(UIElement... children) {
        for (UIElement child : children) {
            add(child);
        }
    }
    @Override
    public void remove(UIElement child) {

    }
    @Override
    public void clear() {

    }
    @Override
    public List<UIElement> getChildren() {
        return children;
    }
    @Override
    public void onClick(Runnable action) {

    }
    @Override
    public void onHover(Runnable action) {

    }
    @Override
    public void onMouseEnter(Runnable action) {

    }
    @Override
    public void onMouseExit(Runnable action) {

    }
    @Override
    public void onFocus(Runnable action) {

    }
    @Override
    public void onBlur(Runnable action) {

    }
}
