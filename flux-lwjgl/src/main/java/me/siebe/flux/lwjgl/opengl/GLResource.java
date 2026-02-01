package me.siebe.flux.lwjgl.opengl;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import me.siebe.flux.util.memory.NativeTracker;

import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

/**
 * Base class for OpenGL resources that own a single GL object (vertex array, buffer, texture, etc.).
 * <p>
 * Provides a unified lifecycle: each resource wraps one OpenGL id, participates in reference counting,
 * and is tracked via {@link NativeTracker} for native memory accounting. Actual GL deletion is deferred
 * until the reference count reaches zero, so shared resources (e.g. copied {@link me.siebe.flux.lwjgl.opengl.vertex.VertexArray}s)
 * stay valid until all references are released.
 * </p>
 * <h2>How it works</h2>
 * <ul>
 *   <li><b>Construction:</b> Subclasses pass their GL id to the constructor. The base class registers
 *       the resource with {@link NativeTracker} and sets the initial reference count to 1.</li>
 *   <li><b>Reference counting:</b> {@link #addReference()} and {@link #removeReference()} (or {@link #delete()})
 *       update the count. When {@link #delete()} is called, the count is decremented; the GL object is
 *       only deleted and {@link NativeTracker} notified when the reference count becomes 0.</li>
 *   <li><b>Binding:</b> {@link #bind()} and {@link #unbind()} use the target returned by {@link #getBindTarget()},
 *       so subclasses only need to specify their GL target (e.g. {@code GL_VERTEX_ARRAY}, {@code GL_ARRAY_BUFFER},
 *       {@code GL_TEXTURE_2D}).</li>
 * </ul>
 */
public abstract class GLResource {
    private static final Logger logger = LoggerFactory.getLogger(GLResource.class, LoggingCategories.OPENGL);

    /** Sentinel for shader programs; OpenGL has no GL_PROGRAM target, binding uses {@code glUseProgram}. */
    protected static final int GL_PROGRAM = 0;

    /** The OpenGL object id (VAO, buffer, texture, etc.). */
    protected final int glId;
    private boolean deleted = false;
    private int referenceCounter = 0;

    /**
     * Initializes the resource with the given OpenGL id, registers it with {@link NativeTracker},
     * and sets the reference count to 1.
     *
     * @param glId the OpenGL object id (must be valid and not 0)
     */
    protected GLResource(int glId) {
        this.glId = glId;
        alloc();
        addReference();
    }

    /** Returns the OpenGL object id for this resource. */
    public int getGlId() {
        return glId;
    }

    /**
     * Returns the OpenGL binding target used for {@link #bind()} and {@link #unbind()}.
     * For example {@code GL_VERTEX_ARRAY}, {@code GL_ARRAY_BUFFER}, {@code GL_ELEMENT_ARRAY_BUFFER},
     * or {@code GL_TEXTURE_2D}.
     */
    protected abstract int getBindTarget();

    /** Binds this resource to its default target (see {@link #getBindTarget()}). */
    public void bind() {
        bind(getBindTarget());
    }

    /**
     * Binds this resource to the given OpenGL target.
     *
     * @param target the binding target (e.g. {@code GL_VERTEX_ARRAY}, {@code GL_ARRAY_BUFFER}, {@code GL_TEXTURE_2D})
     * @throws IllegalStateException if this resource has already been deleted
     */
    public void bind(int target) {
        if (deleted) {
            throw new IllegalStateException("Cannot bind deleted " + this.getClass().getSimpleName());
        }

        switch (target) {
            case GL_VERTEX_ARRAY -> glBindVertexArray(glId);
            case GL_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER -> glBindBuffer(target, glId);
            case GL_TEXTURE_2D -> glBindTexture(target, glId);
            case GL_PROGRAM -> glUseProgram(glId);
            default -> throw new IllegalArgumentException("Invalid target " + target);
        }
    }

    /** Unbinds this resource by binding 0 to its default target. */
    public void unbind() {
        int target = getBindTarget();
        switch (target) {
            case GL_VERTEX_ARRAY -> glBindVertexArray(0);
            case GL_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER -> glBindBuffer(target, 0);
            case GL_TEXTURE_2D -> glBindTexture(target, 0);
            case GL_PROGRAM -> glUseProgram(0);
            default -> throw new IllegalArgumentException("Invalid target " + target);
        }
    }

    /**
     * Releases one reference to this resource. When the reference count reaches zero, the OpenGL
     * object is deleted and the resource is unregistered from {@link NativeTracker}. Safe to call
     * multiple times; further calls after the resource is deleted are no-ops.
     */
    public final boolean delete() {
        removeReference();
        if (referenceCounter != 0) {
            logger.debug("Resource {} is being referenced somewhere else, only removing current reference", getClass().getSimpleName());
            return false;
        }

        if (deleted) {
            logger.debug("Resource {} is already deleted", getClass().getSimpleName());
            return false;
        }

        unbind();
        if (!deleteDependencies()) {
            logger.debug("Failed to delete dependencies for {}, continuing anyway", getClass().getSimpleName());
        }

        logger.debug("Resource {} is being deleted", getClass().getSimpleName());
        switch (getBindTarget()) {
            case GL_VERTEX_ARRAY -> glDeleteVertexArrays(glId);
            case GL_ELEMENT_ARRAY_BUFFER, GL_ARRAY_BUFFER -> glDeleteBuffers(glId);
            case GL_TEXTURE_2D -> glDeleteTextures(glId);
            case GL_PROGRAM -> glDeleteProgram(glId);
            default -> throw new IllegalArgumentException("Invalid target " + getBindTarget());
        }

        this.deleted = true;
        release();
        return true;
    }

    /** Hook for subclasses to delete dependent resources */
    protected boolean deleteDependencies() {
        return true;
    }

    protected void alloc() {
        alloc(this.getClass().getSimpleName());
    }
    protected void alloc(String tag) {
        NativeTracker.alloc(tag);
    }

    protected void release() {
        release(this.getClass().getSimpleName());
    }
    protected void release(String tag) {
        NativeTracker.free(tag);
    }

    /** Increments the reference count. Call when another object starts sharing this resource (e.g. on copy). */
    protected void addReference() {
        referenceCounter++;
    }

    /** Decrements the reference count. Called internally by {@link #delete()}. */
    protected void removeReference() {
        referenceCounter--;
    }
}
