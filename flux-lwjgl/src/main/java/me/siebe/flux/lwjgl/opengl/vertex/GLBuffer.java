package me.siebe.flux.lwjgl.opengl.vertex;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

public abstract class GLBuffer {
    private static final Logger logger = LoggerFactory.getLogger(GLBuffer.class, LoggingCategories.RENDERER);

    private boolean deleted = false;
    private final int bufferId;

    protected GLBuffer(int bufferId) {
        if (bufferId == 0) {
            throw new IllegalArgumentException("Failed to create " + this.getClass().getSimpleName() + ". BufferId cannot be 0");
        }
        this.bufferId = bufferId;
        logger.trace("Created {} with ID: {}", this.getClass().getSimpleName(), bufferId);
    }

    /** Return the OpenGL binding target for glBindBuffer (GL_ARRAY_BUFFER or GL_ELEMENT_ARRAY_BUFFER) or 0 for VAOs */
    protected abstract int getBindTarget();

    public void bind() {
        bind(getBindTarget());
    }

    public void bind(int target) {
        if (deleted) {
            throw new IllegalStateException("Cannot bind deleted " + this.getClass().getSimpleName());
        }

        if (target == 0) {
            // Used for VAOs
            glBindVertexArray(bufferId);
        } else {
            glBindBuffer(target, bufferId);
        }
    }

    public void unbind() {
        int target = getBindTarget();
        if (target == 0) {
            glBindVertexArray(0);
        } else {
            glBindBuffer(target, 0);
        }
    }

    public void delete() {
        if (!deleted) {
            if (getBindTarget() == 0) {
                glDeleteVertexArrays(bufferId);
            } else {
                glDeleteBuffers(bufferId);
            }

            deleted = true;
            logger.trace("Destroyed {} with ID: {}", this.getClass().getSimpleName(), bufferId);
        }
    }
}
