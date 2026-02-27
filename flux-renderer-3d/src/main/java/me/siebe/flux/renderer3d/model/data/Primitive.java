package me.siebe.flux.renderer3d.model.data;

import me.siebe.flux.opengl.vertex.VertexArray;
import me.siebe.flux.util.exceptions.Validator;
import me.siebe.flux.util.memory.Copyable;

/**
 * Represents a single primitive within a mesh.
 * A primitive contains the geometry data (vertices, indices) and material properties.
 * In GLTF terms, this corresponds to a single primitive within a mesh.
 * This is a generic data structure that can be populated from various file formats
 * (GLTF, OBJ, FBX, etc.) by their respective parsers.
 */
public class Primitive implements Copyable<Primitive> {
    /**
     * The vertex array object containing the geometry data (vertices, indices).
     * This encapsulates all vertex buffers and the index buffer needed for rendering.
     */
    private final VertexArray vertexArray;

    /**
     * The material properties for this primitive.
     */
    private final Material material;

    public Primitive(VertexArray vertexArray, Material material) {
        Validator.notNull(vertexArray);
        Validator.notNull(material);
        this.vertexArray = vertexArray;
        this.material = material;
    }

    public VertexArray getVertexArray() {
        return vertexArray;
    }

    public Material getMaterial() {
        return material;
    }

    public void delete() {
        vertexArray.delete();
        material.delete();
    }

    @Override
    public Primitive copy() {
        Primitive clone = new Primitive(vertexArray.copy(), material.copy());
        return clone;
    }
}
