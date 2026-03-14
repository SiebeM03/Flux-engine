package me.siebe.flux.opengl.texture;

import me.siebe.flux.opengl.GLResource;
import me.siebe.flux.util.memory.Copyable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture extends GLResource implements Copyable<Texture> {
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
        super(glGenTextures());
        this.width = width;
        this.height = height;
        this.target = target;

        bind();
        glTexImage2D(target, 0, internalFormat, width, height, 0, format, type, data);
        glGenerateMipmap(target);

        setFilters(GL_LINEAR, GL_LINEAR);
        setWrap(GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
        unbind();
    }

    // TODO implement with GltfLoader#loadTexture() as it uses a lot of similar code
    public Texture(String path) {
//        super(glGenTextures());
//        bind();
//
//        try (MemoryStack stack = stackPush()) {
//            IntBuffer w = stack.mallocInt(1);
//            IntBuffer h = stack.mallocInt(1);
//            IntBuffer channels = stack.mallocInt(1);
//
//            STBImage.stbi_set_flip_vertically_on_load(true);
//            ByteBuffer imageData = STBImage.stbi_load(path, w, h, channels, 4);
//            if (imageData == null) {
//                throw new RuntimeException("Failed to load texture: " + path);
//            }
//
//            width = w.get();
//            height = h.get();
//            target = GL_TEXTURE_2D;
//
//            glTexImage2D(target, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
//            glGenerateMipmap(target);
//
//            setFilters(GL_NEAREST, GL_NEAREST);
//            setWrap(GL_REPEAT, GL_REPEAT);
//
//            STBImage.stbi_image_free(imageData);
//        }

        super(glGenTextures());
        bind();

        setWrap(GL_REPEAT, GL_REPEAT);

        setFilters(GL_NEAREST, GL_NEAREST);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = stbi_load(path, width, height, channels, 0);

        if (image == null) {
            assert false : "Error: (Texture) Could not load image '" + path + "'";
        }

        this.width = width.get(0);
        this.height = height.get(0);
        this.target = GL_TEXTURE_2D;

        if (channels.get(0) == 3) {
            glTexImage2D(this.target, 0, GL_RGB, this.width, this.height, 0, GL_RGB, GL_UNSIGNED_BYTE, image);
        } else if (channels.get(0) == 4) {
            glTexImage2D(this.target, 0, GL_RGB, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        } else {
            assert false : "Error: (Texture) Unknown number of channels '" + channels.get(0) + "'";
        }
            glGenerateMipmap(target);

        stbi_image_free(image);
    }

    @Override
    protected int getBindTarget() {
        return target;
    }

    public void bindToSlot(int slot) {
        glActiveTexture(GL_TEXTURE0 + slot);
        bind();
    }

    public void unbind() {
        glBindTexture(target, 0);
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

    @Override
    public Texture copy() {
        addReference();
        return this;
    }
}
