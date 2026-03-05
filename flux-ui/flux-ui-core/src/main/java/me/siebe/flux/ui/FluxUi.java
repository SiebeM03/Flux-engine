package me.siebe.flux.ui;

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
    public Queue<UIScene> getScenes() {
        return scenes;
    }
}
