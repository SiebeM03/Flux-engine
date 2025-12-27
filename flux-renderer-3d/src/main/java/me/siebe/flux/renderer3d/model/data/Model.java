package me.siebe.flux.renderer3d.model.data;

import me.siebe.flux.api.renderer.data.Renderable;
import me.siebe.flux.lwjgl.opengl.OpenGLState;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import me.siebe.flux.lwjgl.opengl.vertex.IndexBuffer;
import me.siebe.flux.util.exceptions.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a complete 3D model that can contain multiple meshes.
 * This is a generic data structure that can be populated from various file formats
 * (GLTF, OBJ, FBX, etc.) by their respective parsers.
 * <p>
 * The Model class implements Renderable, allowing it to be used directly in the render pipeline.
 * Transform, scale, and rotation are handled separately by the scene manager, not stored here.
 */
public class Model implements Renderable {
    /**
     * List of meshes that make up this model.
     */
    private final List<Mesh> meshes;
    /**
     * Optional name for this model. Can be used for identification and debugging.
     */
    private String name;

    public Model() {
        this(new ArrayList<>(), null);
    }
    public Model(List<Mesh> meshes) {
        this(meshes, null);
    }
    public Model(String name) {
        this(new ArrayList<>(), name);
    }

    public Model(List<Mesh> meshes, String name) {
        Validator.notNull(meshes, () -> "Meshes");
        this.meshes = meshes;
        this.name = name;
    }

    public void addMesh(Mesh mesh) {
        Validator.notNull(mesh, () -> "Mesh");
        meshes.add(mesh);
    }

    public Optional<Mesh> getMesh(String name) {
        Validator.notNull(name, () -> "Mesh name");
        return meshes.stream()
                .filter(mesh -> name.equals(mesh.getName()))
                .findFirst();
    }

    public boolean removeMesh(Mesh mesh) {
        return meshes.remove(mesh);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Renders all meshes in this model.
     * This method is called by the render pipeline when this model is in the render context.
     * <p>
     * Note: The shader should be bound and view-projection matrix should be uploaded
     * before calling this method. The model matrix should also be uploaded by the scene manager.
     */
    @Override
    public void render() {
        for (Mesh mesh : meshes) {
            renderMesh(mesh);
        }
    }

    /**
     * Renders a single mesh by rendering all its primitives.
     * This is a basic implementation that binds the vertex array and draws the elements.
     * Subclasses or render steps may override this behavior for more advanced rendering.
     *
     * @param mesh the mesh to render
     */
    protected void renderMesh(Mesh mesh) {
        ShaderProgram.getActiveShader().upload("uModelMatrix", mesh.getModelMatrix());
        for (Primitive primitive : mesh.getPrimitives()) {
            renderPrimitive(primitive);
        }
    }

    /**
     * Renders a single primitive.
     * This is a basic implementation that binds the vertex array and draws the elements.
     * Subclasses or render steps may override this behavior for more advanced rendering.
     *
     * @param primitive the primitive to render
     */
    protected void renderPrimitive(Primitive primitive) {
        Material material = primitive.getMaterial();
        ShaderProgram shader = ShaderProgram.getActiveShader();

        material.applyOpenGLState();

        // Bind textures and upload material uniforms
        material.uploadToShader(shader);

        primitive.getVertexArray().bind();
        IndexBuffer indexBuffer = primitive.getVertexArray().getIndexBuffer();
        if (indexBuffer != null) {
            OpenGLState.drawElements(primitive.getVertexArray());
        }
        primitive.getVertexArray().unbind();

        material.restoreOpenGLState();
    }

    /**
     * Cleanup method called when the model is no longer needed.
     * This can be used to release resources, but in this implementation,
     * resource cleanup is typically handled by the renderer or resource manager.
     */
    @Override
    public void destroy() {
        // Resource cleanup can be added here if needed
        // Typically, VertexArray and Texture cleanup is handled by the renderer
    }
}
