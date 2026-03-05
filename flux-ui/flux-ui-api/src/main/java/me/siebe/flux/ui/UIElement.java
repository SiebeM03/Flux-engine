package me.siebe.flux.ui;

import me.siebe.flux.util.FluxColor;

import java.util.List;

public interface UIElement {
    // --- Layout ---
    void setWidth(float width);
    void setHeight(float height);
    void setSize(float width, float height);
    float getWidth();
    float getHeight();

    // --- Positioning ---
    void setPosition(float x, float y);
    void setAbsolute(boolean isAbsolute);
    float getX();
    float getY();

    // --- Visibility ---
    void setVisible(boolean visible);
    boolean isVisible();

    void setEnabled(boolean enabled);
    boolean isEnabled();

    void setBackground(FluxColor color);
    FluxColor getBackground();
    void setOpacity(float opacity);

    // --- Hierarchy ---
    UIElement getParent();
    void add(UIElement child);
    void addAll(UIElement... children);
    void remove(UIElement child);
    void clear();
    List<UIElement> getChildren();

    // --- Event Handling ---
    void onClick(Runnable action);
    void onHover(Runnable action);
    void onMouseEnter(Runnable action);
    void onMouseExit(Runnable action);
    void onFocus(Runnable action);
    void onBlur(Runnable action);

}
