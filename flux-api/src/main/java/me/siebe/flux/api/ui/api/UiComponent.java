package me.siebe.flux.api.ui.api;

import me.siebe.flux.api.renderer.data.Renderable;
import me.siebe.flux.util.FluxColor;
import me.siebe.flux.util.data.LockableArrayList;
import org.joml.Vector2f;

import java.util.List;

public abstract class UiComponent implements Renderable {
    protected String name;
    protected Vector2f position;
    protected Vector2f size;
    protected FluxColor color;
    protected LockableArrayList<UiComponent> children;

    public UiComponent(String name, Vector2f position, Vector2f size, FluxColor color) {
        this.name = name;
        this.position = position;
        this.size = size;
        this.color = color;
        this.children = new LockableArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getSize() {
        return size;
    }

    public FluxColor getColor() {
        return color;
    }

    public List<UiComponent> getChildren() {
        return children;
    }

    public void update() {
        children.lock();
        updateSelf();
        for (UiComponent child : children) {
            child.update();
        }
        children.unlock();
    }

    protected abstract void updateSelf();


    @Override
    public void render() {

    }
}
