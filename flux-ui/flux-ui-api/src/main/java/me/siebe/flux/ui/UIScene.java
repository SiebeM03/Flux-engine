package me.siebe.flux.ui;

public interface UIScene {
    void setRoot(UIElement root);
    UIElement getRoot();

    void update(float deltaTime);

    void setViewport(int width, int height);
}
