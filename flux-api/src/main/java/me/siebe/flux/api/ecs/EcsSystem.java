package me.siebe.flux.api.ecs;

import java.util.HashMap;
import java.util.Map;

public final class EcsSystem {
    private static final EcsSystem INSTANCE = new EcsSystem();
    private final Map<Integer, World> worlds = new HashMap<>();


    public static World getWorld(int id) {
        return INSTANCE.worlds.get(id);
    }

    public static void registerWorld(World world) {
        INSTANCE.worlds.put(world.getId(), world);
    }

    public static World getWorldByEntityId(int id) {
        for (World world : INSTANCE.worlds.values()) {
            if (world.getEntity(id) != null) {
                return world;
            }
        }
        return null;
    }
}
