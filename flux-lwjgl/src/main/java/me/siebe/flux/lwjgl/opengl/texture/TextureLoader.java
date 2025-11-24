package me.siebe.flux.lwjgl.opengl.texture;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public final class TextureLoader {
    public static Texture loadTexture(String texturePath) throws IOException {
        return null;
    }


    public static Texture fromGltfTextureData(ByteBuffer textureData) {
        // Ensure texture buffer is properly positioned and has valid data
        if (textureData == null) {
            throw new IllegalArgumentException("Texture data buffer cannot be null");
        }

        // Make sure buffer is at position 0 and limit is set correctly
        textureData.rewind();
        if (textureData.remaining() == 0) {
            throw new IllegalArgumentException("Texture data buffer is empty");
        }

        // STBImage requires a direct buffer. If the buffer is not direct, create a copy
        ByteBuffer directBuffer = textureData;
        if (!textureData.isDirect()) {
            // Create a direct buffer copy
            directBuffer = org.lwjgl.BufferUtils.createByteBuffer(textureData.remaining());
            int oldPosition = textureData.position();
            textureData.rewind();
            directBuffer.put(textureData);
            directBuffer.flip();
            textureData.position(oldPosition);
        }

        // Ensure the buffer is positioned at the start
        directBuffer.rewind();

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        STBImage.stbi_set_flip_vertically_on_load(false);   // glTF uses bottom-up coordinates

        // Request desired channels (4 = RGBA), but STB will return what's available
        ByteBuffer pixels = STBImage.stbi_load_from_memory(directBuffer, width, height, channels, 4);
        if (pixels == null) {
            String reason = STBImage.stbi_failure_reason();
            throw new RuntimeException("Failed to load texture: " + (reason == null ? "Unknown error" : reason));
        }

        int w = width.get(0);
        int h = height.get(0);

        // When we request 4 channels, STBImage will:
        // - Return 4 channels (RGBA) in the pixel buffer
        // - Set comp to 4 (the requested channel count)
        // - Pad RGB images with alpha=255
        // So we always use RGBA format when we requested 4 channels

        Texture texture = new Texture(w, h, GL_TEXTURE_2D, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        texture.bind();
        // Set texture parameters (glTF spec recommends these)
        texture.setFilters(GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
        texture.setWrap(GL_REPEAT, GL_REPEAT);
        texture.unbind();

        // Free STB image data
        STBImage.stbi_image_free(pixels);
        return texture;
    }
}
