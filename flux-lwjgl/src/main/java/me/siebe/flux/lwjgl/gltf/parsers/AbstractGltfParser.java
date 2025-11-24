package me.siebe.flux.lwjgl.gltf.parsers;

import me.siebe.flux.lwjgl.gltf.GltfModel;
import me.siebe.flux.lwjgl.gltf.parsers.glb.GlbParser;
import me.siebe.flux.lwjgl.gltf.parsers.gltf.GltfParser;
import me.siebe.flux.lwjgl.opengl.texture.Texture;
import me.siebe.flux.lwjgl.opengl.texture.TextureLoader;
import me.siebe.flux.util.buffer.BufferUtils;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractGltfParser {
    private static final Logger logger = LoggerFactory.getLogger(AbstractGltfParser.class, LoggingCategories.GLTF);

    protected final Path assetFilePath;
    protected Map<String, Object> json;
    protected List<ByteBuffer> buffers;

    protected AbstractGltfParser(Path assetFilePath) {
        this.assetFilePath = assetFilePath;
    }

    public static AbstractGltfParser getParser(Path filePath) {
        if (filePath.getFileName().toString().toLowerCase().endsWith(".glb")) {
            return new GlbParser(filePath);
        } else {
            return new GltfParser(filePath);
        }
    }

    public GltfModel parse() throws IOException {
        loadFormatSpecificData();
        return parseModel();
    }

    protected abstract void loadFormatSpecificData() throws IOException;

    private GltfModel parseModel() throws IOException {
        System.out.println(json.toString());
        // Parse buffer views
        List<Map<String, Object>> bufferViews = getList(json, "bufferViews", Collections.emptyList());

        // Parse accessors
        List<Map<String, Object>> accessorDefs = getList(json, "accessors", Collections.emptyList());

        // Parse textures
        List<Texture> textures = parseTextures(bufferViews);

        return new GltfModel();
    }


    /**
     * Parses textures from the glTF JSON.
     *
     * @param bufferViews list of buffer view definitions
     * @return list of parsed textures
     */
    @SuppressWarnings("unchecked")
    protected List<Texture> parseTextures(List<Map<String, Object>> bufferViews) {
        List<Texture> textures = new ArrayList<>();

        List<Map<String, Object>> textureDefs = getList(json, "textures", Collections.emptyList());
        List<Map<String, Object>> imageDefs = getList(json, "images", Collections.emptyList());

        for (Map<String, Object> textureDef : textureDefs) {
            Integer sourceIndex = getInteger(textureDef, "source");
            if (sourceIndex == null || sourceIndex < 0 || sourceIndex >= imageDefs.size()) {
                textures.add(null); // Invalid texture
                continue;
            }

            try {
                Map<String, Object> image = imageDefs.get(sourceIndex);
                ByteBuffer imageData = loadImageData(image, bufferViews);
                Texture texture = TextureLoader.fromGltfTextureData(imageData);
                textures.add(texture);
            } catch (Exception e) {
                // Log errors but continue loading other textures
                logger.error("Failed to load texture at source index {}: {}", sourceIndex, e.getMessage());
                e.printStackTrace();
                textures.add(null); // Failed texture - will be handled gracefully in rendering
            }
        }

        return textures;
    }

    private ByteBuffer loadImageData(Map<String, Object> image, List<Map<String, Object>> bufferViews) throws IOException {
        // Check if image is embedded (has bufferView) or external (has URI)
        Integer bufferViewIndex = getInteger(image, "bufferView");
        if (bufferViewIndex != null) {
            // Embedded image (bufferView specified)
            if (bufferViewIndex < 0 || bufferViewIndex >= bufferViews.size()) {
                throw new IllegalArgumentException("Invalid buffer view index for image: " + bufferViewIndex);
            }

            Map<String, Object> bufferView = bufferViews.get(bufferViewIndex);
            int bufferIndex = getInt(bufferView, "buffer", 0);
            int byteOffset = getInt(bufferView, "byteOffset", 0);
            int byteLength = getInt(bufferView, "byteLength", 0);

            if (bufferIndex < 0 || bufferIndex >= buffers.size()) {
                throw new IllegalArgumentException("Invalid buffer index for image: " + bufferIndex);
            }

            ByteBuffer buffer = buffers.get(bufferIndex);
            return BufferUtils.slice(buffer, byteOffset, byteLength);
        }

        // External image (URI specified)
        String uri = getString(image, "uri", null);
        if (uri == null) {
            throw new IllegalArgumentException("Image must have either bufferView or uri");
        }

        if (uri.startsWith("data:")) {
            // Data URI (base64 encoded)
            // Format: data:[<mediatype>][;base64],<data>
            int commaIndex = uri.indexOf(',');
            if (commaIndex == -1) {
                throw new IllegalArgumentException("Invalid data URI format");
            }

            String data = uri.substring(commaIndex + 1);
            byte[] decoded = java.util.Base64.getDecoder().decode(data);
            return BufferUtils.createByteBuffer(decoded);
        }


        Path imagePath = assetFilePath.getParent().resolve(uri);
        if (!Files.exists(imagePath)) {
            throw new IOException("Image file not found: " + imagePath);
        }
        return BufferUtils.createByteBuffer(Files.readAllBytes(imagePath));
    }


    // =================================================================================================================
    // Helper methods for JSON Parsing
    // =================================================================================================================
    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> getList(Map<String, Object> map, String key, List<Map<String, Object>> defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return defaultValue;
    }

    protected Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    protected int getInt(Map<String, Object> map, String key, int defaultValue) {
        Integer value = getInteger(map, key);
        return value != null ? value : defaultValue;
    }

    protected String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
