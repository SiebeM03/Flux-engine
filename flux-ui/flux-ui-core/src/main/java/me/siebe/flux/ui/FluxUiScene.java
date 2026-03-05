package me.siebe.flux.ui;

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
