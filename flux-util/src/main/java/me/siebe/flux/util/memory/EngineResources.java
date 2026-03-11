package me.siebe.flux.util.memory;

import java.util.ArrayDeque;
import java.util.Deque;

public final class EngineResources {
    private static final Deque<AutoCloseable> resources = new ArrayDeque<>();

    private EngineResources() {}

    public static <T extends AutoCloseable> T register(T resource) {
        if (resource != null) {
            resources.push(resource);
        }
        return resource;
    }

    public static void add(Runnable runnable) {
        resources.push(runnable::run);
    }

    public static void cleanup() {
        while (!resources.isEmpty()) {
            try {
                resources.pop().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
