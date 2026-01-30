package me.siebe.flux.renderer3d.model.data;

import me.siebe.flux.util.Transform;
import me.siebe.flux.util.exceptions.Validator;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a mesh in a 3D model.
 * A mesh can contain multiple primitives, each with its own geometry and material.
 * This allows grouping related geometry together (e.g., a cube with different materials
 * on different faces can be a single mesh with multiple primitives).
 * <p>
 * In GLTF terms, this corresponds directly to a mesh object, where each primitive
 * in the GLTF mesh becomes a Primitive in this structure.
 * This is a generic data structure that can be populated from various file formats
 * (GLTF, OBJ, FBX, etc.) by their respective parsers.
 */
public class Mesh {
    /**
     * List of primitives that make up this mesh.
     * Each primitive has its own geometry (VertexArray) and material.
     */
    private final List<Primitive> primitives;

    /**
     * The transform relative to the parent Model's transform
     */
    private Transform transform;

    /**
     * Optional name for this mesh. Can be used for identification and debugging.
     */
    private String name;

    public Mesh() {
        this(new ArrayList<>(), null);
    }
    public Mesh(String name) {
        this(new ArrayList<>(), name);
    }
    public Mesh(List<Primitive> primitives) {
        this(primitives, null);
    }

    public Mesh(List<Primitive> primitives, String name) {
        Validator.notNull(primitives, () -> "Primitives");
        this.primitives = new ArrayList<>(primitives);
        this.name = name;

        this.transform = new Transform();
    }

    public List<Primitive> getPrimitives() {
        return primitives;
    }

    public void addPrimitive(Primitive primitive) {
        Validator.notNull(primitive, () -> "Primitive");
        primitives.add(primitive);
    }

    public boolean removePrimitive(Primitive primitive) {
        return primitives.remove(primitive);
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    /**
     * Use Mesh#getTransform() instead
     */
    @Deprecated(forRemoval = true)
    public Vector3f getRelativePosition() {
        return transform.getPosition();
    }

    /**
     * Use Mesh#getTransform() instead
     */
    @Deprecated(forRemoval = true)
    public void setRelativePosition(Vector3f relativePosition) {
        transform.setPosition(relativePosition);
    }

    /**
     * Use Mesh#getTransform() instead
     */
    @Deprecated(forRemoval = true)
    public Quaternionf getRotation() {
        return transform.getRotation();
    }

    /**
     * Use Mesh#getTransform() instead
     */
    @Deprecated(forRemoval = true)
    public void setRotation(Quaternionf rotation) {
        transform.setRotation(rotation);
    }

    /**
     * Use Mesh#getTransform() instead
     */
    @Deprecated(forRemoval = true)
    public void resetRotation() {
        transform.resetRotation();
    }

    /**
     * Use Mesh#getTransform() instead
     */
    @Deprecated(forRemoval = true)
    public Vector3f getScale() {
        return transform.getScale();
    }

    /**
     * Use Mesh#getTransform() instead
     */
    @Deprecated(forRemoval = true)
    public void setScale(Vector3f scale) {
        transform.setScale(scale);
    }

    /**
     * Use Mesh#getTransform() instead
     */
    @Deprecated(forRemoval = true)
    public void resetScale() {
        transform.resetScale();
    }

    /**
     * Use Mesh#getTransform() instead
     */
    @Deprecated(forRemoval = true)
    public Matrix4f getModelMatrix() {
        return transform.getModelMatrix();
    }

    /**
     * Use Mesh#getTransform() instead
     */
    @Deprecated(forRemoval = true)
    private void updateModelMatrix(Matrix4f m) {
        m.set(transform.getModelMatrix());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
