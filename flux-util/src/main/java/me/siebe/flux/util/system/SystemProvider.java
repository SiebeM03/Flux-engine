package me.siebe.flux.util.system;

import me.siebe.flux.util.exceptions.FluxException;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class SystemProvider {
    private static final Map<Class<? extends ProvidableSystem>, List<ProvidableSystem>> cache = new ConcurrentHashMap<>();

    public static <T extends ProvidableSystem> List<T> provideAll(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        List<T> list = (List<T>) cache.computeIfAbsent(clazz, c -> (List<ProvidableSystem>)
                ServiceLoader.load(clazz)
                        .stream()
                        .map(ServiceLoader.Provider::get)
                        .toList()
        );
        return list;
    }

    public static <T extends ProvidableSystem> T provide(Class<T> clazz, Predicate<T> selector) {
        return provideAll(clazz).stream()
                .filter(selector)
                .findFirst()
                .orElseThrow(() -> new FluxException("No matching implementation for " + clazz.getSimpleName()));
    }

    public static <T extends ProvidableSystem> T provide(Class<T> clazz) {
        return provideAll(clazz).stream()
                .findFirst()
                .orElseThrow(() -> new FluxException("No matching implementation for " + clazz.getSimpleName()));
    }
}
