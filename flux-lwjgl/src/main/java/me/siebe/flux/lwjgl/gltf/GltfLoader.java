package me.siebe.flux.lwjgl.gltf;

import me.siebe.flux.lwjgl.gltf.parsers.AbstractGltfParser;
import me.siebe.flux.util.assets.AssetPathResolver;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class GltfLoader {
    private static final Logger logger = LoggerFactory.getLogger(GltfLoader.class, LoggingCategories.GLTF);

    private GltfLoader() {}

    /**
     * Loads a glTF model from a resource path (classpath).
     *
     * @param resourcePath resource path (e.g., "models/cube/untitled.glb")
     * @return loaded model
     * @throws IOException              if the resource cannot be read
     * @throws IllegalArgumentException if the file is invalid
     */
    public static GltfModel loadFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = GltfLoader.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            Path path = Paths.get(resourcePath);
            if (!Files.exists(path)) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            // Select appropriate parser based on file extension
            AbstractGltfParser parser = AbstractGltfParser.getParser(path);
            return parser.parse();
        }
    }

    /**
     * Loads a glTF model from the assets folder.
     * <p>
     * The asset path is relative to the assets folder. For example, if the assets folder
     * is at {@code ./assets/} and the path is {@code "models/cube.glb"}, this will load
     * {@code ./assets/models/cube.glb}.
     * <p>
     * The assets folder is located beside the JAR file when running from JAR, or in the
     * working directory when running from IDE.
     *
     * @param assetPath asset path relative to the assets folder (e.g., "models/cube.glb")
     * @return loaded model
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file is invalid
     */
    public static GltfModel loadFromAssets(String assetPath) throws IOException {
        Path assetFilePath = AssetPathResolver.resolveAssetPath(assetPath);
        if (!Files.exists(assetFilePath)) {
            throw new IOException("Asset file not found: " + assetFilePath + " (resolved from: " + assetPath + ")");
        }

        // Select appropriate parser based on file extension
        AbstractGltfParser parser = AbstractGltfParser.getParser(assetFilePath);
        return parser.parse();
    }
}
