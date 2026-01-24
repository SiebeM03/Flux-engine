package me.siebe.flux.api.ecs;

import me.siebe.flux.api.ecs.Results.With1;
import me.siebe.flux.api.ecs.Results.With2;
import me.siebe.flux.api.ecs.Results.With3;
import me.siebe.flux.api.ecs.Results.With4;
import me.siebe.flux.util.system.ProvidableSystem;
import me.siebe.flux.util.system.SystemProvider;
import me.siebe.flux.util.system.SystemProviderType;

public interface World {
    String getName();

    // =================================================================================================================
    // World creation methods
    // =================================================================================================================
    static World create() {
        return factory().create();
    }

    static World create(String name) {
        return factory().create(name);
    }

    static World.Factory factory() {
        return SystemProvider.provide(World.Factory.class, SystemProviderType.ALL);
    }

    interface Factory extends ProvidableSystem {
        World create();

        World create(String name);
    }


    // =================================================================================================================
    // Entity managing method
    // =================================================================================================================
    Entity createEntity(Object... components);

    Entity getEntity(int id);

    boolean deleteEntity(Entity entity);


    // =================================================================================================================
    // Entity searching methods
    // =================================================================================================================
    <T> Results<With1<T>> findEntitiesWith(Class<T> type);

    <T1, T2> Results<With2<T1, T2>> findEntitiesWith(Class<T1> type1, Class<T2> type2);

    <T1, T2, T3> Results<With3<T1, T2, T3>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3);

    <T1, T2, T3, T4> Results<With4<T1, T2, T3, T4>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4);
}
