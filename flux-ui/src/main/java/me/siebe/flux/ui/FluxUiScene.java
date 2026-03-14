package me.siebe.flux.ui;

import me.siebe.flux.api.ui.UIElement;
import me.siebe.flux.api.ui.UIScene;

public class FluxUiScene implements UIScene {
    private UIElement root;

    @Override
    public void setRoot(UIElement root) {
        this.root = root;
    }

    @Override
    public UIElement getRoot() {
        return root;
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void setViewport(int width, int height) {
        root.setSize(width, height);
    }
}
