package me.siebe.flux.ui;

import me.siebe.flux.api.ui.UIScene;
import me.siebe.flux.api.ui.Ui;

import java.util.ArrayDeque;
import java.util.Queue;

public class FluxUi implements Ui {
    private ArrayDeque<UIScene> scenes = new ArrayDeque<>();

    @Override
    public UIScene createScene() {
        UIScene scene = new FluxUiScene();
        scenes.add(scene);
        return scene;
    }

    @Override
    public void pushScene(UIScene scene) {
        scenes.push(scene);
    }

    @Override
    public UIScene popScene() {
        return scenes.pop();
    }

    @Override
    public Queue<UIScene> getScenes() {
        return scenes;
    }
}
