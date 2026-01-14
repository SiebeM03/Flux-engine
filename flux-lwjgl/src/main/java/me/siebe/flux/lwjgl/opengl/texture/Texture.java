package me.siebe.flux.lwjgl.opengl.texture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Texture {
    /**
     * OpenGL texture handle (GLuint).
     */
    private int glId;

    /**
     * Texture width in pixels.
     */
    private int width;

    /**
     * Texture height in pixels.
     */
    private int height;

    /**
     * Number of color components (1 = grayscale, 3 = RGB, 4 = RGBA).
     */
    private int componentCount;

    /**
     * The target to which the texture is bound, used in OpenGL methods such as
     * {@link GL11#glBindTexture(int target, int id)}
     */
    private int target;


    /**
     * Creates a new texture with the given width, height, internal format, format,
     * type, and target.
     *
     * @param width          the width of the texture
     * @param height         the height of the texture
     * @param target         the target to which the texture is bound. E.g. {@link GL11#GL_TEXTURE_2D}
     * @param internalFormat the texture internal format. Used in OpenGL methods such as {@link GL11#glTexImage2D(int, int, int, int, int, int, int, int, ByteBuffer)}
     * @param format         the format of the texture, used in OpenGL methods such as {@link GL11#glTexImage2D(int, int, int, int, int, int, int, int, ByteBuffer)}
     * @param type           the type of the texture, used in OpenGL methods such as {@link GL11#glTexImage2D(int, int, int, int, int, int, int, int, ByteBuffer)}
     * @param data           the data of the texture, used in OpenGL methods such as {@link GL11#glTexImage2D(int, int, int, int, int, int, int, int, ByteBuffer)}
     */
    public Texture(int width, int height, int target, int internalFormat, int format, int type, ByteBuffer data) {
        this.width = width;
        this.height = height;
        this.target = target;

        this.glId = glGenTextures();
        bind();
        glTexImage2D(target, 0, internalFormat, width, height, 0, format, type, data);
        glGenerateMipmap(target);

        setFilters(GL_LINEAR, GL_LINEAR);
        setWrap(GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
        unbind();
    }

    // TODO implement with GltfLoader#loadTexture() as it uses a lot of similar code
    public Texture(String path) {
        this.glId = glGenTextures();
        bind();

        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer imageData = STBImage.stbi_load(path, w, h, channels, 4);
            if (imageData == null) {
                throw new RuntimeException("Failed to load texture: " + path);
            }

            width = w.get();
            height = h.get();
            target = GL_TEXTURE_2D;

            glTexImage2D(target, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
            glGenerateMipmap(target);

            STBImage.stbi_image_free(imageData);
        }
    }

    public void bind() {
        glBindTexture(target, glId);
    }

    public void bindToSlot(int slot) {
        glActiveTexture(GL_TEXTURE0 + slot);
        bind();
    }

    public void unbind() {
        glBindTexture(target, 0);
    }

    public void delete() {
        glDeleteTextures(glId);
    }

    public void setFilters(int minFilter, int magFilter) {
        bind();
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, minFilter);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, magFilter);
        unbind();
    }

    public void setWrap(int wrapS, int wrapT) {
        bind();
        glTexParameteri(target, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, wrapT);
        unbind();
    }

    /**
     * @return OpenGL texture handle
     */
    public int getGlId() {
        return glId;
    }

    /**
     * @return texture width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return texture height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return number of color components (1, 3, or 4)
     */
    public int getComponentCount() {
        return componentCount;
    }
}
