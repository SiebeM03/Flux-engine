package me.siebe.flux.api.ecs;

import java.util.Iterator;
import java.util.stream.Stream;

public interface Results<T> extends Iterable<T> {
    @Override
    Iterator<T> iterator();

    Stream<T> stream();

    record With1<T>(T comp, Entity entity) {
    }

    record With2<T1, T2>(T1 comp1, T2 comp2, Entity entity) {
    }

    record With3<T1, T2, T3>(T1 comp1, T2 comp2, T3 comp3, Entity entity) {
    }

    record With4<T1, T2, T3, T4>(T1 comp1, T2 comp2, T3 comp3, T4 comp4, Entity entity) {
    }
}
