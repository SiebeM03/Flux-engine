package me.siebe.flux.api.ui.api;

import me.siebe.flux.api.ui.impl.UiContainer;

import java.util.ArrayDeque;
import java.util.Queue;

public class Ui {
    public static final UiContainer CONTAINER = new UiContainer();

    private static final Queue<UiMutation> mutationQueue = new ArrayDeque<>();

    public static void queueMutation(UiMutation mutation) {
        mutationQueue.add(mutation);
    }

    public void flushMutations() {
        while (!mutationQueue.isEmpty()) {
            mutationQueue.poll().apply();
        }
    }

    public void update() {
        CONTAINER.update();

        flushMutations();
    }
}
