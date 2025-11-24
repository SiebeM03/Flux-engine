package me.siebe.flux.util.assets;

import me.siebe.flux.util.io.FileIOException;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AssetPathResolver {
    private static final Logger logger = LoggerFactory.getLogger(AssetPathResolver.class, LoggingCategories.ASSETS);

    private static final String ASSETS_FOLDER_NAME = "assets";
    private static Path assetsBasePath = null;

    private AssetPathResolver() {

    }

    /**
     * Gets the base path to the assets folder.
     * <p>
     * The path is resolved once and cached for subsequent calls.
     *
     * @return the base path to the assets folder
     * @throws FileIOException if the assets folder cannot be located
     */
    public static Path getAssetsBasePath() {
        if (assetsBasePath == null) {
            assetsBasePath = locateAssetsFolder();
        }

        return assetsBasePath;
    }

    /**
     * Resolves an asset path relative to the assets folder.
     * <p>
     * For example, if the assets folder is at {@code ./assets/} and the path is
     * {@code "models/cube.glb"}, this returns {@code ./assets/models/cube.glb}.
     *
     * @param assetPath the asset path relative to the assets folder (e.g., "models/cube.glb")
     * @return the resolved absolute path to the asset
     * @throws FileIOException if the assets folder cannot be located
     */
    public static Path resolveAssetPath(String assetPath) {
        Path basePath = getAssetsBasePath();
        return basePath.resolve(assetPath).normalize();
    }

    /**
     * Locates the assets folder by checking multiple possible locations.
     * <p>
     * Checks in order:
     * <ol>
     *   <li>Beside the JAR file (if running from JAR)</li>
     *   <li>In the current working directory</li>
     * </ol>
     *
     * @return the path to the assets folder, or null if not found
     */
    private static Path locateAssetsFolder() {
        // Try 1: Besides the JAR file (if running from JAR)
        try {
            // Use toURI() to handle file:// URLs correctly on all platforms
            URI codeSourceUri = AssetPathResolver.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            File codeSourceFile = new File(codeSourceUri);
            if (codeSourceFile.getName().endsWith(".jar")) {
                // Game is running from JAR
                Path jarDir = codeSourceFile.getParentFile().toPath();
                Path assetsPath = jarDir.resolve(ASSETS_FOLDER_NAME);
                if (isValidFolderPath(assetsPath)) {
                    logger.info("Found assets folder at: {}", assetsPath);
                    return assetsPath;
                }
            }
        } catch (Exception ignored) {
        }

        // Try 2: In the current working directory
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        Path workingDirAssets = workingDir.resolve(ASSETS_FOLDER_NAME);
        if (isValidFolderPath(workingDirAssets)) {
            logger.info("Found assets folder at: {}", workingDirAssets);
            return workingDirAssets;
        }

        throw FileIOException.assetsFolderNotFound(workingDirAssets);
    }

    private static boolean isValidFolderPath(Path path) {
        return Files.exists(path) && Files.isDirectory(path);
    }
}
