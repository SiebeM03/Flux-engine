package me.siebe.flux.util.system;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import me.siebe.flux.util.exceptions.FluxException;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Provides system implementations using ServiceLoader and automatic classpath
 * discovery.
 * <p>
 * This provider automatically discovers custom implementations from game
 * projects (classes not in engine packages)
 * and prioritizes them over default engine implementations. Game developers
 * only need to create a class implementing
 * the desired interface - no registration or configuration is required.
 */
public class SystemProvider {
    private static final Logger logger = LoggerFactory.getLogger(SystemProvider.class, "system-provider");
    private static final Map<Class<? extends ProvidableSystem>, List<ProvidableSystem>> cache = new ConcurrentHashMap<>();
    private static final String ENGINE_PACKAGE_PREFIX = "me.siebe.flux";

    /**
     * Provide all implementations of the given system interface.
     *
     * @param clazz the system interface class
     * @param type  the type of implementations to provide
     * @param <T>   the system type
     * @return a list of all implementations, with custom implementations first
     */
    public static <T extends ProvidableSystem> List<T> provideAll(Class<T> clazz, SystemProviderType type) {
        List<T> implementations = new ArrayList<>();
        if (type == SystemProviderType.ALL || type == SystemProviderType.CUSTOM_ONLY) {
            implementations.addAll(findCustomImplementations(clazz));
        }

        if (type == SystemProviderType.ALL || type == SystemProviderType.ENGINE_ONLY) {
            implementations.addAll(findEngineImplementations(clazz));
        }

        return implementations;
    }

    /**
     * Provide the first implementation of the given system interface that matches
     * the selector.
     *
     * @param clazz    the system interface class
     * @param type     the type of implementations to provide
     * @param selector the selector to filter the implementations
     * @param <T>      the system type
     * @return the first implementation that matches the selector, with custom
     * implementations taking precedence over engine implementations
     * @throws FluxException if no matching implementation is found
     */
    public static <T extends ProvidableSystem> T provide(Class<T> clazz, SystemProviderType type, Predicate<T> selector) {
        return provideAll(clazz, type)
                .stream()
                .filter(selector)
                .findFirst()
                .orElseThrow(() -> new FluxException("No matching implementation for " + clazz.getSimpleName()));
    }

    /**
     * Provide the first implementation of the given system interface.
     *
     * @param clazz the system interface class
     * @param type  the type of implementations to provide
     * @param <T>   the system type
     * @return the first implementation, with custom implementations taking
     * precedence over engine implementations
     * @throws FluxException if no matching implementation is found
     */
    public static <T extends ProvidableSystem> T provide(Class<T> clazz, SystemProviderType type) {
        return provide(clazz, type, c -> true);
    }

    /**
     * Finds all engine implementations of the given system interface.
     * <p>
     * This method uses the Java ServiceLoader to find all implementations of the
     * given interface. This only finds implementations that are defined in a
     * META-INF/services file.
     *
     * @param clazz the system interface class
     * @param <T>   the system type
     * @return a list of all engine implementations
     */
    private static <T extends ProvidableSystem> List<T> findEngineImplementations(Class<T> clazz) {
        return ServiceLoader.load(clazz).stream().map(ServiceLoader.Provider::get).toList();
    }

    /**
     * Finds all custom implementations of the given system interface.
     * <p>
     * This method scans the classpath for all classes that implement the given
     * interface or are a subclass of the given class and are not in the engine
     * package.
     *
     * @param clazz the system interface class
     * @param <T>   the system type
     * @return a list of all custom implementations
     */
    @SuppressWarnings("unchecked")
    private static <T extends ProvidableSystem> List<T> findCustomImplementations(Class<T> clazz) {
        List<T> customImplementations = new ArrayList<>();

        try (ScanResult scan = new ClassGraph().enableClassInfo().scan()) {
            List<Class<?>> classes;
            if (Modifier.isInterface(clazz.getModifiers())) {
                classes = scan.getClassesImplementing(clazz).loadClasses();
            } else {
                classes = scan.getSubclasses(clazz).loadClasses();
            }

            for (Class<?> implClass : classes) {
                // Skip implementations in engine packages
                if (implClass.getPackageName().startsWith(ENGINE_PACKAGE_PREFIX))
                    continue;
                // Skip abstract classes since they are not actual implementations
                if (Modifier.isAbstract(implClass.getModifiers()))
                    continue;
                // Skip interfaces since they are not actual implementations
                if (Modifier.isInterface(implClass.getModifiers()))
                    continue;

                try {
                    customImplementations.add((T) implClass.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    logger.warn("Could not instantiate {} using {} ({})", implClass.getSimpleName(), clazz.getSimpleName(), e.getClass().getName());
                }
            }
        }
        return customImplementations;
    }
}
