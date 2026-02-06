package me.siebe.flux.lwjgl.opengl.shader;

import me.siebe.flux.api.application.EngineSystem;
import me.siebe.flux.api.application.SystemManager;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Watches shader files on the filesystem and triggers reloads when they change.
 * <p>
 * Add resource root directories (e.g. {@code module/src/main/resources}) to watch.
 * When a {@code .vert} or {@code .frag} file is modified, the corresponding shader
 * is reloaded from disk on the next call to {@link #processPendingReloads()}.
 * <p>
 * {@link #processPendingReloads()} must be called on the OpenGL thread (e.g. at
 * the start of each frame in the game loop).
 * <p>
 * This can be configured and implemented in 2 ways:
 * <ul>
 * <li>
 *     <b>VM options</b>: if option {@code flux.shader.hotreload.paths} is set, the values (comma-separated) are
 *     passed as strings to {@link #ShaderHotReloader(String...)} and it is automatically registered via
 *     {@link SystemManager#registerEngineSystem(EngineSystem)}
 * </li>
 * <li>
 *     <b>Manually</b>: you can also manually register a ShaderHotReloader from within your game code:
 *     <pre>{@code
 *     AppContext.get()
 *          .getSystemManager
 *          .registerEngineSystem(new ShaderHotReloader(
 *              "src/main/resources"    // Add all folders you want to check
 *           );
 *     }</pre>
 * </li>
 * </ul>
 */
public class ShaderHotReloader implements EngineSystem {
    private static final Logger logger = LoggerFactory.getLogger(ShaderHotReloader.class, LoggingCategories.SHADER);

    private final List<Path> watchRoots;
    private final ConcurrentLinkedQueue<PendingReload> pendingReloads = new ConcurrentLinkedQueue<>();
    private volatile Thread watchThread;
    private volatile boolean running;

    public ShaderHotReloader(String... watchRootFolderNames) {
        Path cwd = Paths.get(".").toAbsolutePath().normalize();
        this.watchRoots = new ArrayList<>();
        for (String watchRootFolderName : watchRootFolderNames) {
            Path watchRoot = cwd.resolve(watchRootFolderName);
            if (!Files.exists(watchRoot)) {
                logger.warn("Watch root {} does not exist", watchRoot);
            }
            if (watchRoot.toFile().isDirectory()) {
                watchRoots.add(watchRoot);
            }
        }
    }

    public ShaderHotReloader(List<Path> watchRoots) {
        this.watchRoots = List.copyOf(watchRoots);
    }

    /**
     * Starts watching the configured resource roots for shader file changes.
     * Idempotent: does nothing if already started.
     */
    public void start() {
        if (running) return;
        if (watchRoots.isEmpty()) {
            logger.error("No watch roots were found, not starting ShaderHotReloader.");
            return;
        }
        running = true;
        watchThread = new Thread(this::watchLoop, "shader-hot-reload");
        watchThread.setDaemon(true);
        watchThread.start();
        logger.info("Shader hot reload started watching {} root(s)", watchRoots.size());
        for (Path watchRoot : watchRoots) {
            logger.trace("Watching {}", watchRoot);
        }
    }

    /**
     * Stops the watch thread. Pending reloads may still be processed.
     */
    public void stop() {
        running = false;
        if (watchThread != null) {
            watchThread.interrupt();
            watchThread = null;
        }
    }

    /**
     * Processes any pending shader reloads. Must be called on the OpenGL thread
     * (e.g. at the start of each frame).
     */
    public void processPendingReloads() {
        PendingReload pending;
        while ((pending = pendingReloads.poll()) != null) {
            ShaderLoader.get().reload(pending.basePath, pending.resourceRoot);
        }
    }

    private void watchLoop() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            for (Path root : watchRoots) {
                if (!Files.isDirectory(root)) {
                    logger.warn("Shader watch root is not a directory, skipping: {}", root);
                    continue;
                }
                registerRecursive(watchService, root);
            }

            while (running) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                Path eventDir = (Path) key.watchable();
                Path resourceRoot = findResourceRoot(eventDir);
                if (resourceRoot == null) {
                    key.reset();
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) continue;

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path name = ev.context();
                    Path fullPath = eventDir.resolve(name);
                    if (Files.isDirectory(fullPath)) continue;
                    String relative = resourceRoot.relativize(fullPath).toString().replace('\\', '/');
                    String basePath = shaderBasePath(relative);
                    if (basePath != null) {
                        // Check if a PendingReload value is already queued with the same basePath and resourceRoot
                        boolean alreadyInQueue = pendingReloads.stream().anyMatch(p -> p.basePath.equals(basePath) && p.resourceRoot.equals(resourceRoot));
                        if (!alreadyInQueue) {
                            pendingReloads.offer(new PendingReload(basePath, resourceRoot));
                        }
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    logger.warn("WatchKey no longer valid for {}", eventDir);
                }
            }
        } catch (ClosedWatchServiceException e) {
            // Normal when stopping
        } catch (Exception e) {
            logger.error("Error while watching shader hot reload", e);
        }
    }

    /**
     * Registers all subdirectories of the given directory to the watch service.
     * <p>
     * Newly created files or directories will not be registered at runtime, only modifications to shader files will trigger a reload.
     *
     * @param watchService The watch service to register files/directories to
     * @param dir          The root directory to recursively check and register to the watch service
     */
    private void registerRecursive(WatchService watchService, Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes attrs) throws IOException {
                d.register(watchService, ENTRY_MODIFY, ENTRY_CREATE);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private Path findResourceRoot(Path eventDir) {
        for (Path root : watchRoots) {
            if (eventDir.startsWith(root)) {
                return root;
            }
        }
        return null;
    }

    /**
     * If path is a shader file (e.g. shaders/gltf.vert), returns the base path (shaders/gltf).
     * Otherwise, returns null.
     */
    private static String shaderBasePath(String relativePath) {
        if (relativePath.endsWith(".vert") || relativePath.endsWith(".frag")) {
            return relativePath.substring(0, relativePath.length() - 5);
        }
        return null;
    }

    private record PendingReload(String basePath, Path resourceRoot) {}


    @Override
    public void init() {
        start();
    }

    @Override
    public void update() {
        processPendingReloads();
    }

    @Override
    public void destroy() {
        stop();
    }
}
