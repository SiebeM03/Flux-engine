package me.siebe.flux.util.logging.config;

/**
 * Shared category constants that the engine modules can rely on.
 * <p>
 * Because this type is declared as an interface, you can extend it in your own projects to add
 * additional constants, e.g.:
 *
 * <pre>{@code
 * public interface MyCategories extends LoggingCategories {
 *     String PHYSICS = "physics";
 * }
 * }</pre>
 */
public interface LoggingCategories {
    String GENERAL = "general";
    String APPLICATION = "application";
    String ENGINE = "engine";

    // OpenGL related
    String RENDERER = "renderer";
    String SHADER = "shader";

    // File related
    String GLTF = "gltf";
    String ASSETS = "assets";

    String EVENT = "event";
    String WINDOW = "window";
}

