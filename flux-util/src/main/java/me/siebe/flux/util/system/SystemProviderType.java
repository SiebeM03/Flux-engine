package me.siebe.flux.util.system;

import java.util.function.Predicate;

/**
 * The type of implementations to provide. Used by the {@link SystemProvider} to determine which implementations to provide.
 * @see SystemProvider#provideAll(Class, SystemProviderType)
 * @see SystemProvider#provide(Class, SystemProviderType, Predicate)
 * @see SystemProvider#provide(Class, SystemProviderType)
 */
public enum SystemProviderType {
    /**
     * Provide all implementations, both custom and engine.
     */
    ALL,
    /**
     * Provide only custom implementations.
     */
    CUSTOM_ONLY,
    /**
     * Provide only engine implementations.
     */
    ENGINE_ONLY
}