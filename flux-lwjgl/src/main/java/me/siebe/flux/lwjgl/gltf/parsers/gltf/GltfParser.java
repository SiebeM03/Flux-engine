package me.siebe.flux.lwjgl.gltf.parsers.gltf;

import me.siebe.flux.lwjgl.gltf.parsers.AbstractGltfParser;
import me.siebe.flux.util.io.FileFormat;
import me.siebe.flux.util.io.FileIOManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static me.siebe.flux.util.buffer.BufferUtils.createByteBuffer;

public class GltfParser extends AbstractGltfParser {
    public GltfParser(Path path) {
        super(path);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void loadFormatSpecificData() throws IOException {
        try (InputStream is = Files.newInputStream(assetFilePath, StandardOpenOption.READ)) {
            String jsonStr = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            json = FileIOManager.get().parse(jsonStr, Map.class, FileFormat.JSON);
            buffers = loadExternalBuffers();
        }
    }

    /**
     * Loads external buffers for a .gltf file.
     *
     * @return list of loaded buffers
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    private List<ByteBuffer> loadExternalBuffers() throws IOException {
        List<ByteBuffer> buffers = new ArrayList<>();
        List<Map<String, Object>> bufferDefs = getList(json, "buffers", Collections.emptyList());

        for (Map<String, Object> bufferDef : bufferDefs) {
            String uri = getString(bufferDef, "uri", null);
            if (uri == null) {
                // Embedded buffer (shouldn't happen in .gltf, but handle it anyway)
                int byteLength = getInt(bufferDef, "byteLength", 0);
                buffers.add(ByteBuffer.allocate(byteLength).order(ByteOrder.LITTLE_ENDIAN));
                continue;
            }

            if (uri.startsWith("data:")) {
                // Data URI
                ByteBuffer data = decodeDataUri(uri);
                buffers.add(data);
                continue;
            }

            // External file - try asset path first, then file system path
            Path filePath = assetFilePath.getParent().resolve(uri);
            try (InputStream is = Files.newInputStream(filePath, StandardOpenOption.READ)) {
                byte[] bytes = is.readAllBytes();
                ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                buffers.add(buffer);
            } catch (IOException e) {
                throw new IOException("Failed to read buffer asset: " + filePath, e);
            }

        }

        return buffers;
    }


    /**
     * Decodes a data URI into a ByteBuffer.
     *
     * @param dataUri data URI string
     * @return decoded buffer
     */
    private ByteBuffer decodeDataUri(String dataUri) {
        int commaIndex = dataUri.indexOf(',');
        if (commaIndex == -1) {
            throw new IllegalArgumentException("Invalid data URI format");
        }
        String data = dataUri.substring(commaIndex + 1);
        byte[] decoded = java.util.Base64.getDecoder().decode(data);
        return createByteBuffer(decoded);
    }
}
