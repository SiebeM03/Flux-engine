package me.siebe.flux.lwjgl.opengl.shader;

import me.siebe.flux.api.application.AppContext;
import me.siebe.flux.util.assets.AssetPool;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.nio.file.Path;

public class ShaderLoader extends AssetPool<ShaderProgram> {
    private static final Logger logger = LoggerFactory.getLogger(ShaderLoader.class, LoggingCategories.SHADER);
    private static ShaderLoader instance;

    static {
        try {
            startHotReloadFromSystemProperties();
        } catch (Exception e) {
            logger.error("Error starting shader hot-reloading", e);
        }
    }

    private ShaderLoader() {}

    public static ShaderLoader get() {
        if (instance == null) instance = new ShaderLoader();
        return instance;
    }

    @Override
    protected ShaderProgram create(String filepath) {
        return new ShaderProgram(filepath, null);
    }

    /**
     * Reloads a shader from the filesystem, replacing the cached instance.
     * The old shader is destroyed. If compilation fails, the cache is left empty
     * and the next {@link #load(String)} will load from the classpath again.
     * <p>
     * Must be called on the OpenGL thread.
     *
     * @param basePath     the shader base path (e.g. "shaders/gltf")
     * @param resourceRoot the filesystem path to the resource root containing the shader files
     * @return true if reload succeeded, false if the shader was not cached or compilation failed
     */
    boolean reload(String basePath, Path resourceRoot) {
        try {
            ShaderProgram oldProgram = load(basePath);
            if (oldProgram != null) {
                oldProgram.delete();
            }
            ShaderProgram newProgram = new ShaderProgram(basePath, resourceRoot);
            putAsset(basePath, newProgram);
            logger.info("Hot-reloaded shader '{}'", basePath);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to hot-reload shader '{}': {}", basePath, e.getMessage());
            return false;
        }
    }

    /**
     * Starts the hot reload system from system properties.
     * The system property is `flux.shader.hotreload.paths` and it is a comma-separated list of paths to watch.
     * The paths are watched for changes and when a change is detected, the shader is reloaded.
     */
    private static void startHotReloadFromSystemProperties() {
        final String SYSTEM_PROPERTY_PATHS = "flux.shader.hotreload.paths";
        logger.debug("Checking if system property {} is set", SYSTEM_PROPERTY_PATHS);
        String values = System.getProperty(SYSTEM_PROPERTY_PATHS);
        if (values == null) {
            logger.debug("System property {} is not set, not automatically starting Shader hot-reloading", SYSTEM_PROPERTY_PATHS);
            return;
        }

        ShaderHotReloader hotReloader = new ShaderHotReloader(values.split(","));
        logger.debug("System property {} is set, initializing ShaderHotReloader system and registering it to the application", SYSTEM_PROPERTY_PATHS);
        AppContext.get().getSystemManager().registerEngineSystem(hotReloader);
    }
}
