package me.siebe.flux.renderer3d.model.data;

import me.siebe.flux.util.exceptions.Validator;
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
     * Position relative to the model's position
     */
    private Vector3f relativePosition;

    private Quaternionf rotation;

    private Vector3f scale;

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

        this.relativePosition = new Vector3f(0, 0, 0);
        this.rotation = new Quaternionf();
        this.scale = new Vector3f(1, 1, 1);
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

    public Vector3f getRelativePosition() {
        return relativePosition;
    }

    public void setRelativePosition(Vector3f relativePosition) {
        this.relativePosition.set(relativePosition);
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public void setRotation(Quaternionf rotation) {
        if (rotation != null) {
            this.rotation.set(rotation);
        } else {
            resetRotation();
        }
    }

    public void resetRotation() {
        this.rotation.identity();
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        if (scale != null) {
            this.scale.set(scale);
        } else {
            resetScale();
        }
    }

    public void resetScale() {
        this.scale.set(1, 1, 1);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
