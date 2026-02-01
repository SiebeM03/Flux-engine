package me.siebe.flux.util;

import me.siebe.flux.util.memory.Copyable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Represents a 3D transformation with position, rotation, and scale.
 * Provides methods for manipulating and computing transformation matrices.
 * <p>
 * The transform uses a lazy evaluation system where the model matrix is computed
 * only when needed and cached until the transform values change. This provides
 * efficient matrix computation for rendering operations.
 * <p>
 * Transformation order: translate → rotate → scale
 */
public class Transform implements Copyable<Transform> {
    private final Vector3f position;
    private final Quaternionf rotation;
    private final Vector3f scale;

    private final DirtyValue<Matrix4f> modelMatrix;

    /**
     * Creates a new Transform with default values.
     * <ul>
     *   <li>Position: (0, 0, 0)</li>
     *   <li>Rotation: Identity quaternion (no rotation)</li>
     *   <li>Scale: (1, 1, 1)</li>
     * </ul>
     */
    public Transform() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Quaternionf();
        this.scale = new Vector3f(1, 1, 1);
        this.modelMatrix = new DirtyValue<>(new Matrix4f(), this::updateModelMatrix);
    }

    /**
     * Creates a new Transform with the specified position, rotation, and scale.
     *
     * @param position The position vector. If null, defaults to (0, 0, 0)
     * @param rotation The rotation quaternion. If null, defaults to identity
     * @param scale    The scale vector. If null, defaults to (1, 1, 1)
     */
    public Transform(Vector3f position, Quaternionf rotation, Vector3f scale) {
        this.position = new Vector3f(position != null ? position : new Vector3f(0, 0, 0));
        this.rotation = new Quaternionf(rotation != null ? rotation : new Quaternionf());
        this.scale = new Vector3f(scale != null ? scale : new Vector3f(1, 1, 1));
        this.modelMatrix = new DirtyValue<>(new Matrix4f(), this::updateModelMatrix);
    }

    /**
     * Creates a new Transform by copying the values from another Transform.
     *
     * @param other The transform to copy from. Must not be null.
     */
    public Transform(Transform other) {
        this.position = new Vector3f(other.position);
        this.rotation = new Quaternionf(other.rotation);
        this.scale = new Vector3f(other.scale);
        this.modelMatrix = new DirtyValue<>(new Matrix4f(), this::updateModelMatrix);
    }

    // =================================================================================================================
    // Position methods
    // =================================================================================================================

    /**
     * Gets the position vector.
     *
     * @return A copy of the position vector
     */
    public Vector3f getPosition() {
        return position;
    }

    /**
     * Sets the position vector.
     *
     * @param position The new position
     */
    public void setPosition(Vector3f position) {
        this.position.set(position);
        modelMatrix.markDirty();
    }

    /**
     * Sets the position using individual components.
     *
     * @param x The X component
     * @param y The Y component
     * @param z The Z component
     */
    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        modelMatrix.markDirty();
    }

    /**
     * Translates the position by the given translation vector.
     *
     * @param translation The translation vector to apply
     */
    public void translate(Vector3f translation) {
        this.position.add(translation);
        modelMatrix.markDirty();
    }

    /**
     * Translates the position by the given components.
     *
     * @param x The X translation component
     * @param y The Y translation component
     * @param z The Z translation component
     */
    public void translate(float x, float y, float z) {
        this.position.add(x, y, z);
        modelMatrix.markDirty();
    }

    /**
     * Resets the position to the origin (0, 0, 0).
     */
    public void resetPosition() {
        this.position.set(0, 0, 0);
        modelMatrix.markDirty();
    }

    // =================================================================================================================
    // Rotation methods
    // =================================================================================================================

    /**
     * Gets the rotation quaternion.
     *
     * @return A copy of the rotation quaternion
     */
    public Quaternionf getRotation() {
        return new Quaternionf(rotation);
    }

    /**
     * Sets the rotation quaternion.
     *
     * @param rotation The new rotation quaternion
     */
    public void setRotation(Quaternionf rotation) {
        this.rotation.set(rotation);
        modelMatrix.markDirty();
    }

    /**
     * Sets the rotation quaternion using individual components.
     *
     * @param x The X component of the quaternion
     * @param y The Y component of the quaternion
     * @param z The Z component of the quaternion
     * @param w The W component of the quaternion
     */
    public void setRotation(float x, float y, float z, float w) {
        this.rotation.set(x, y, z, w);
        modelMatrix.markDirty();
    }

    /**
     * Multiplies the current rotation by the given rotation quaternion.
     * This applies the rotation relative to the current rotation.
     *
     * @param rotation The rotation quaternion to apply
     */
    public void rotate(Quaternionf rotation) {
        this.rotation.mul(rotation);
        modelMatrix.markDirty();
    }

    /**
     * Rotates the transform by the given Euler angles (in degrees).
     * The rotation is applied in the order: X, then Y, then Z.
     *
     * @param angleX Rotation around X-axis in degrees
     * @param angleY Rotation around Y-axis in degrees
     * @param angleZ Rotation around Z-axis in degrees
     */
    public void rotate(float angleX, float angleY, float angleZ) {
        this.rotation.rotateXYZ(
                (float) Math.toRadians(angleX),
                (float) Math.toRadians(angleY),
                (float) Math.toRadians(angleZ)
        );
        modelMatrix.markDirty();
    }

    /**
     * Rotates the transform around the X-axis by the given angle.
     *
     * @param angleDegrees The rotation angle in degrees
     */
    public void rotateX(float angleDegrees) {
        this.rotation.rotateX((float) Math.toRadians(angleDegrees));
        modelMatrix.markDirty();
    }

    /**
     * Rotates the transform around the Y-axis by the given angle.
     *
     * @param angleDegrees The rotation angle in degrees
     */
    public void rotateY(float angleDegrees) {
        this.rotation.rotateY((float) Math.toRadians(angleDegrees));
        modelMatrix.markDirty();
    }

    /**
     * Rotates the transform around the Z-axis by the given angle.
     *
     * @param angleDegrees The rotation angle in degrees
     */
    public void rotateZ(float angleDegrees) {
        this.rotation.rotateZ((float) Math.toRadians(angleDegrees));
        modelMatrix.markDirty();
    }

    /**
     * Resets the rotation to identity (no rotation).
     */
    public void resetRotation() {
        this.rotation.identity();
        modelMatrix.markDirty();
    }

    // =================================================================================================================
    // Scale methods
    // =================================================================================================================

    /**
     * Gets the scale vector.
     *
     * @return A copy of the scale vector
     */
    public Vector3f getScale() {
        return new Vector3f(scale);
    }

    /**
     * Sets the scale vector.
     *
     * @param scale The new scale vector
     */
    public void setScale(Vector3f scale) {
        this.scale.set(scale);
        modelMatrix.markDirty();
    }

    /**
     * Sets the scale using individual components.
     *
     * @param x The X scale component
     * @param y The Y scale component
     * @param z The Z scale component
     */
    public void setScale(float x, float y, float z) {
        this.scale.set(x, y, z);
        modelMatrix.markDirty();
    }

    /**
     * Sets a uniform scale (same value for all axes).
     *
     * @param scale The scale value to apply to all axes
     */
    public void setScale(float scale) {
        this.scale.set(scale);
        modelMatrix.markDirty();
    }

    /**
     * Multiplies the current scale by the given scale vector.
     * This applies the scale relative to the current scale.
     *
     * @param scale The scale vector to multiply with
     */
    public void scale(Vector3f scale) {
        this.scale.mul(scale);
        modelMatrix.markDirty();
    }

    /**
     * Multiplies the current scale by the given components.
     *
     * @param x The X scale multiplier
     * @param y The Y scale multiplier
     * @param z The Z scale multiplier
     */
    public void scale(float x, float y, float z) {
        this.scale.mul(x, y, z);
        modelMatrix.markDirty();
    }

    /**
     * Multiplies the current scale by a uniform value (applied to all axes).
     *
     * @param scale The scale multiplier to apply to all axes
     */
    public void scale(float scale) {
        this.scale.mul(scale);
        modelMatrix.markDirty();
    }

    /**
     * Resets the scale to (1, 1, 1) (no scaling).
     */
    public void resetScale() {
        this.scale.set(1, 1, 1);
        modelMatrix.markDirty();
    }

    // =================================================================================================================
    // Matrix methods
    // =================================================================================================================

    /**
     * Gets the model matrix representing this transform.
     * The matrix is computed lazily and cached until the transform changes.
     *
     * @return A copy of the model matrix
     */
    public Matrix4f getModelMatrix() {
        return new Matrix4f(modelMatrix.get());
    }

    /**
     * Computes the model matrix from position, rotation, and scale.
     * <p>
     * Transformation order: translate → rotate → scale
     * <p>
     * This method is called automatically by the DirtyValue system when
     * the model matrix needs to be recomputed.
     *
     * @param m The matrix to update with the transformation
     */
    private void updateModelMatrix(Matrix4f m) {
        m.identity()
                .translate(position)
                .rotate(rotation)
                .scale(scale);
    }

    /**
     * Computes a combined transformation matrix by applying this transform
     * followed by another relative transform.
     * <p>
     * This is useful for hierarchical transformations where a child transform
     * (relativeTransform) should be applied relative to a parent transform (this).
     *
     * @param relativeTransform The relative transform to apply after this one. Must not be null.
     * @return The combined transformation matrix
     */
    public Matrix4f getCombinedMatrix(Transform relativeTransform) {
        return new Matrix4f(modelMatrix.get())
                .translate(relativeTransform.position)
                .rotate(relativeTransform.rotation)
                .scale(relativeTransform.scale);
    }


    // =================================================================================================================
    // Utility methods
    // =================================================================================================================

    /**
     * Resets all transform values to their defaults.
     * <ul>
     *   <li>Position: (0, 0, 0)</li>
     *   <li>Rotation: Identity quaternion</li>
     *   <li>Scale: (1, 1, 1)</li>
     * </ul>
     */
    public void reset() {
        resetPosition();
        resetRotation();
        resetScale();
    }

    /**
     * Copies the transform values from another Transform.
     * <p>
     * This replaces all current transform values (position, rotation, scale)
     * with the values from the other transform.
     *
     * @param other The transform to copy from. If null, no change is made.
     */
    public void set(Transform other) {
        if (other != null) {
            this.position.set(other.position);
            this.rotation.set(other.rotation);
            this.scale.set(other.scale);
            modelMatrix.markDirty();
        }
    }

    /**
     * Gets the forward direction vector based on the rotation.
     * <p>
     * The forward direction is the negative Z-axis (0, 0, -1) transformed
     * by the rotation quaternion. This represents the direction the transform
     * is "facing" in world space.
     *
     * @return The forward direction vector (normalized)
     */
    public Vector3f getForward() {
        Vector3f forward = new Vector3f(0, 0, -1);
        rotation.transform(forward);
        return forward.normalize();
    }

    /**
     * Gets the right direction vector based on the rotation.
     * <p>
     * The right direction is the positive X-axis (1, 0, 0) transformed
     * by the rotation quaternion.
     *
     * @return The right direction vector (normalized)
     */
    public Vector3f getRight() {
        Vector3f right = new Vector3f(1, 0, 0);
        rotation.transform(right);
        return right.normalize();
    }

    /**
     * Gets the up direction vector based on the rotation.
     * <p>
     * The up direction is the positive Y-axis (0, 1, 0) transformed
     * by the rotation quaternion.
     *
     * @return The up direction vector (normalized)
     */
    public Vector3f getUp() {
        Vector3f up = new Vector3f(0, 1, 0);
        rotation.transform(up);
        return up.normalize();
    }

    /**
     * Orients the rotation to look at a target position from the current position.
     * <p>
     * This method calculates the direction from the current position to the target
     * and sets the rotation quaternion to face that direction. The up vector is used
     * to determine the roll orientation.
     *
     * @param target The target position to look at. Must not be null.
     * @param up     The up vector used to determine orientation (typically (0, 1, 0)). Must not be null.
     */
    public void lookAt(Vector3f target, Vector3f up) {
        if (target != null && up != null) {
            Vector3f direction = new Vector3f(target).sub(position).normalize();
            rotation.lookAlong(direction, up);
            modelMatrix.markDirty();
        }
    }

    /**
     * Orients the rotation to look at a target position from the current position
     * using the default up vector (0, 1, 0).
     * <p>
     * This is a convenience method that calls {@link #lookAt(Vector3f, Vector3f)}
     * with a default up vector.
     *
     * @param target The target position to look at. Must not be null.
     */
    public void lookAt(Vector3f target) {
        lookAt(target, new Vector3f(0, 1, 0));
    }

    @Override
    public Transform copy() {
        Transform clone = new Transform();
        clone.setPosition(new Vector3f(position));
        clone.setRotation(new Quaternionf(rotation));
        clone.setScale(new Vector3f(scale));
        return clone;
    }
}
