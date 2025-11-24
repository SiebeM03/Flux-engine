package me.siebe.flux.lwjgl.gltf.parsers.glb;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static me.siebe.flux.util.buffer.BufferUtils.readUInt32;


/**
 * Parser for binary glTF (.glb) files.
 * <p>
 * A GLB file consists of:
 * - 12-byte header (magic, version, length)
 * - JSON chunk (type + length + data)
 * - BIN chunk (type + length + data) - optional
 * <p>
 * This parser extracts the JSON and binary data chunks for further processing.
 */
public final class BinaryGltfParser {
    private static final int GLB_MAGIC = 0x46546C67; // "glTF"
    private static final int GLB_VERSION = 2;
    private static final int GLB_HEADER_SIZE = 12;
    private static final int GLB_CHUNK_HEADER_SIZE = 8;

    private static final int CHUNK_TYPE_JSON = 0x4E4F534A; // "JSON"
    private static final int CHUNK_TYPE_BIN = 0x004E4942;   // "BIN\0"

    /**
     * Result of parsing a GLB file.
     */
    public record GlbParseResult(
            String json,
            @Nullable ByteBuffer binData
    ) {}

    private BinaryGltfParser() {
        // Utility class
    }

    /**
     * Parses a GLB file from an input stream.
     *
     * @param inputStream the input stream to read from
     * @return parse result containing JSON and binary data
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if the file is not a valid GLB
     */
    public static GlbParseResult parse(InputStream inputStream) throws IOException {
        // Read entire file into memory
        ByteBuffer fileBuffer = readStreamToBuffer(inputStream);
        fileBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Read header
        int magic = fileBuffer.getInt();
        if (magic != GLB_MAGIC) {
            throw new IllegalArgumentException("Invalid GLB magic number: expected 0x" + Integer.toHexString(GLB_MAGIC) + ", got 0x" + Integer.toHexString(magic));
        }

        int version = fileBuffer.getInt();
        if (version != GLB_VERSION) {
            throw new IllegalArgumentException("Unsupported GLB version: " + version + " (expected " + GLB_VERSION + ")");
        }

        long totalLength = readUInt32(fileBuffer);
        int actualLength = fileBuffer.limit();
        if (totalLength != actualLength) {
            throw new IllegalArgumentException("GLB length mismatch: header says " + totalLength + " bytes, file has " + actualLength + " bytes");
        }

        // Read JSON chunk (must be first)
        Chunk jsonChunk = readChunk(fileBuffer);
        if (jsonChunk.type != CHUNK_TYPE_JSON) {
            throw new IllegalArgumentException("First chunk must be JSON, got type: 0x" + Integer.toHexString(jsonChunk.type));
        }

        String json = new String(jsonChunk.data, StandardCharsets.UTF_8);

        // Read BIN chunk (optional, must be second if present)
        ByteBuffer binData = null;
        if (fileBuffer.hasRemaining()) {
            Chunk binChunk = readChunk(fileBuffer);
            if (binChunk.type != CHUNK_TYPE_BIN) {
                throw new IllegalArgumentException("Second chunk must be BIN, got type: 0x" + Integer.toHexString(binChunk.type));
            }
            binData = ByteBuffer.wrap(binChunk.data).order(ByteOrder.LITTLE_ENDIAN);
        }

        return new GlbParseResult(json, binData);
    }


    /**
     * Reads a chunk from the buffer.
     *
     * @param buffer the buffer to read from
     * @return chunk data
     */
    private static Chunk readChunk(ByteBuffer buffer) {
        long chunkLength = readUInt32(buffer);
        int chunkType = buffer.getInt();

        if (chunkLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Chunk length too large: " + chunkLength);
        }

        byte[] chunkData = new byte[(int) chunkLength];
        buffer.get(chunkData);

        // Chunks must be aligned to 4-byte boundaries
        int padding = (int) ((4 - (chunkLength % 4)) % 4);
        if (padding > 0) {
            buffer.position(buffer.position() + padding);
        }

        return new Chunk(chunkType, chunkData);
    }

    /**
     * Reads an entire input stream into a ByteBuffer.
     *
     * @param inputStream the stream to read
     * @return buffer containing all data
     * @throws IOException if an I/O error occurs
     */
    private static ByteBuffer readStreamToBuffer(InputStream inputStream) throws IOException {
        // Read all bytes from the stream
        byte[] bytes = inputStream.readAllBytes();
        // Create a ByteBuffer with the exact size needed
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }


    /**
     * Represents a chunk in a GLB file.
     */
    private record Chunk(
            int type,
            byte[] data
    ) {
    }
}

