package me.siebe.flux.renderer.pipeline.steps;

import me.siebe.flux.api.renderer.RenderContext;
import me.siebe.flux.api.renderer.RenderStep;
import me.siebe.flux.api.renderer.models.Material;
import me.siebe.flux.api.renderer.models.Mesh;
import me.siebe.flux.api.renderer.models.Model;
import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import me.siebe.flux.lwjgl.opengl.vertex.*;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL30.glDrawElements;

/**
 * Render step that renders GLTF models loaded using the GltfLoader.
 * <p>
 * Models can be registered with this step using {@link #registerModel(String, Model)}.
 * Each model is converted to OpenGL buffers (VAO, VBO, IBO) on registration.
 * During rendering, all registered models are drawn using the configured shader program.
 */
public class GltfModelRenderStep implements RenderStep {
    private static final Logger logger = LoggerFactory.getLogger(GltfModelRenderStep.class, LoggingCategories.RENDERER);

    private final String shaderPath;
    private ShaderProgram shaderProgram;
    private final Map<String, ModelRenderData> registeredModels;
    private final List<ModelInstance> instances;

    /**
     * Creates a new GLTF model render step with the default shader path.
     * <p>
     * The default shader path is "shaders/gltf" which will load "shaders/gltf.vert" and "shaders/gltf.frag".
     */
    public GltfModelRenderStep() {
        this("shaders/gltf");
    }

    /**
     * Creates a new GLTF model render step with a custom shader path.
     *
     * @param shaderPath the base path to the shader files (without extension)
     */
    public GltfModelRenderStep(String shaderPath) {
        this.shaderPath = shaderPath;
        this.registeredModels = new HashMap<>();
        this.instances = new ArrayList<>();
    }

    /**
     * Registers a model for rendering.
     * <p>
     * The model is converted to OpenGL buffers (VAO, VBO, IBO) immediately.
     * Multiple instances of the same model can be rendered by calling {@link #addInstance(String, Matrix4f)}.
     *
     * @param name  a unique identifier for this model
     * @param model the model to register
     * @throws IllegalStateException if a model with the same name is already registered
     */
    public void registerModel(String name, Model model) {
        if (registeredModels.containsKey(name)) {
            throw new IllegalStateException("Model with name '" + name + "' is already registered");
        }

        logger.debug("Registering model '{}' with {} meshes", name, model.meshes.size());
        ModelRenderData renderData = convertModelToRenderData(model);
        registeredModels.put(name, renderData);
    }

    /**
     * Adds an instance of a registered model to be rendered.
     * <p>
     * The model must have been registered using {@link #registerModel(String, Model)} first.
     *
     * @param modelName the name of the registered model
     * @param transform the model transformation matrix
     * @throws IllegalArgumentException if the model name is not registered
     */
    public void addInstance(String modelName, Matrix4f transform) {
        ModelRenderData renderData = registeredModels.get(modelName);
        if (renderData == null) {
            throw new IllegalArgumentException("Model '" + modelName + "' is not registered");
        }
        instances.add(new ModelInstance(modelName, new Matrix4f(transform)));
    }

    /**
     * Clears all model instances for the next frame.
     * <p>
     * This should be called each frame before adding new instances.
     */
    public void clearInstances() {
        instances.clear();
    }

    @Override
    public void init(RenderContext context) {
        logger.info("Initializing GLTF model render step with shader: {}", shaderPath);
        try {
            shaderProgram = new ShaderProgram(shaderPath);
            logger.info("Shader program loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load shader program '{}': {}", shaderPath, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize GLTF model render step", e);
        }
    }

    @Override
    public void execute(RenderContext context) {
        if (shaderProgram == null) {
            logger.warn("Shader program not initialized, skipping GLTF model rendering");
            return;
        }

        if (!context.has3DContext()) {
            logger.warn("3D context not available, skipping GLTF model rendering");
            return;
        }

        if (instances.isEmpty()) {
            return;
        }

        shaderProgram.bind();

        // Upload view and projection matrices
        Matrix4f viewMatrix = context.getViewMatrix();
        Matrix4f projectionMatrix = context.getProjectionMatrix();
        if (viewMatrix != null) {
            shaderProgram.upload("uView", viewMatrix);
        }
        if (projectionMatrix != null) {
            shaderProgram.upload("uProjection", projectionMatrix);
        }

        // Upload lighting information if available
        if (context.getLightPosition() != null) {
            shaderProgram.upload("uLightPosition", context.getLightPosition());
        }
        if (context.getLightColor() != null) {
            shaderProgram.upload("uLightColor", context.getLightColor());
        }
        if (context.getViewPosition() != null) {
            shaderProgram.upload("uViewPosition", context.getViewPosition());
        }

        // Render all instances
        for (ModelInstance instance : instances) {
            ModelRenderData renderData = registeredModels.get(instance.modelName);
            if (renderData == null) {
                logger.warn("Model instance references unknown model: {}", instance.modelName);
                continue;
            }

            // Upload model matrix
            shaderProgram.upload("uModel", instance.transform);

            // Render each mesh in the model
            for (MeshRenderData meshData : renderData.meshes) {
                // Upload material properties
                if (meshData.material != null) {
                    if (meshData.material.baseColorFactor != null) {
                        shaderProgram.upload("uBaseColorFactor", new org.joml.Vector4f(
                                meshData.material.baseColorFactor[0],
                                meshData.material.baseColorFactor[1],
                                meshData.material.baseColorFactor[2],
                                meshData.material.baseColorFactor.length > 3 ? meshData.material.baseColorFactor[3] : 1.0f
                        ));
                    }
                }

                // Bind VAO and draw
                meshData.vao.bind();
                if (meshData.indexBuffer != null) {
                    glDrawElements(GL_TRIANGLES, meshData.indexBuffer.getCount(), GL_UNSIGNED_INT, 0);
                } else {
                    // Non-indexed rendering (shouldn't happen with GLTF, but handle it)
                    logger.warn("Mesh has no index buffer, skipping");
                }
                meshData.vao.unbind();
            }
        }

        shaderProgram.unbind();
    }

    @Override
    public void destroy() {
        logger.info("Destroying GLTF model render step");
        
        // Clean up all model render data
        for (ModelRenderData renderData : registeredModels.values()) {
            for (MeshRenderData meshData : renderData.meshes) {
                meshData.vao.delete();
                if (meshData.indexBuffer != null) {
                    meshData.indexBuffer.delete();
                }
            }
        }
        registeredModels.clear();
        instances.clear();

        if (shaderProgram != null) {
            shaderProgram.destroy();
            shaderProgram = null;
        }
    }

    /**
     * Converts a Model to OpenGL render data (VAO, VBO, IBO).
     */
    private ModelRenderData convertModelToRenderData(Model model) {
        ModelRenderData renderData = new ModelRenderData();
        
        for (Mesh mesh : model.meshes) {
            MeshRenderData meshData = convertMeshToRenderData(mesh);
            renderData.meshes.add(meshData);
        }
        
        return renderData;
    }

    /**
     * Converts a Mesh to OpenGL render data.
     */
    private MeshRenderData convertMeshToRenderData(Mesh mesh) {
        if (mesh.positions == null || mesh.positions.length == 0) {
            throw new IllegalArgumentException("Mesh must have position data");
        }

        // Calculate vertex count
        int vertexCount = mesh.positions.length / 3;

        // Always include all attributes for consistent shader interface
        // Use default values when mesh data is missing
        boolean hasNormals = mesh.normals != null && mesh.normals.length >= vertexCount * 3;
        boolean hasTexCoords = mesh.texCoords != null && mesh.texCoords.length >= vertexCount * 2;
        boolean hasTangents = mesh.tangents != null && mesh.tangents.length >= vertexCount * 4;

        // Interleave vertex data: position, normal, texCoord, tangent
        List<BufferElement> elements = new ArrayList<>();

        // Position (vec3) - required
        elements.add(new BufferElement("aPosition", ShaderDataType.Float3, false));
        // Normal (vec3) - always included
        elements.add(new BufferElement("aNormal", ShaderDataType.Float3, false));
        // TexCoord (vec2) - always included
        elements.add(new BufferElement("aTexCoord", ShaderDataType.Float2, false));
        // Tangent (vec4) - always included
        elements.add(new BufferElement("aTangent", ShaderDataType.Float4, false));

        BufferLayout layout = new BufferLayout(elements);
        
        // Create interleaved vertex buffer
        // Stride: position(3) + normal(3) + texCoord(2) + tangent(4) = 12 floats
        int stride = 12; // 3 + 3 + 2 + 4
        float[] interleavedData = new float[vertexCount * stride];
        
        for (int i = 0; i < vertexCount; i++) {
            int baseIdx = i * stride;
            int posIdx = i * 3;
            int normalIdx = i * 3;
            int texIdx = i * 2;
            int tangentIdx = i * 4;

            // Position (required)
            interleavedData[baseIdx] = mesh.positions[posIdx];
            interleavedData[baseIdx + 1] = mesh.positions[posIdx + 1];
            interleavedData[baseIdx + 2] = mesh.positions[posIdx + 2];

            // Normal (use mesh data or default to up vector)
            if (hasNormals) {
                interleavedData[baseIdx + 3] = mesh.normals[normalIdx];
                interleavedData[baseIdx + 4] = mesh.normals[normalIdx + 1];
                interleavedData[baseIdx + 5] = mesh.normals[normalIdx + 2];
            } else {
                // Default normal pointing up
                interleavedData[baseIdx + 3] = 0.0f;
                interleavedData[baseIdx + 4] = 1.0f;
                interleavedData[baseIdx + 5] = 0.0f;
            }

            // TexCoord (use mesh data or default to (0,0))
            if (hasTexCoords) {
                interleavedData[baseIdx + 6] = mesh.texCoords[texIdx];
                interleavedData[baseIdx + 7] = mesh.texCoords[texIdx + 1];
            } else {
                interleavedData[baseIdx + 6] = 0.0f;
                interleavedData[baseIdx + 7] = 0.0f;
            }

            // Tangent (use mesh data or default)
            if (hasTangents) {
                interleavedData[baseIdx + 8] = mesh.tangents[tangentIdx];
                interleavedData[baseIdx + 9] = mesh.tangents[tangentIdx + 1];
                interleavedData[baseIdx + 10] = mesh.tangents[tangentIdx + 2];
                interleavedData[baseIdx + 11] = mesh.tangents[tangentIdx + 3];
            } else {
                // Default tangent (right vector) with w=1.0
                interleavedData[baseIdx + 8] = 1.0f;
                interleavedData[baseIdx + 9] = 0.0f;
                interleavedData[baseIdx + 10] = 0.0f;
                interleavedData[baseIdx + 11] = 1.0f;
            }
        }

        VertexArray vao = new VertexArray();
        vao.bind();

        VertexBuffer vertexBuffer = new VertexBuffer(interleavedData);
        vertexBuffer.setLayout(layout);

        vao.addVertexBuffer(vertexBuffer);

        IndexBuffer indexBuffer = null;
        if (mesh.indices != null && mesh.indices.length > 0) {
            indexBuffer = new IndexBuffer(mesh.indices);
            vao.setIndexBuffer(indexBuffer);
        }

        MeshRenderData meshData = new MeshRenderData();
        meshData.vao = vao;
        meshData.indexBuffer = indexBuffer;
        meshData.material = mesh.material;

        return meshData;
    }

    /**
     * Internal data structure for a model's render data.
     */
    private static class ModelRenderData {
        final List<MeshRenderData> meshes = new ArrayList<>();
    }

    /**
     * Internal data structure for a mesh's render data.
     */
    private static class MeshRenderData {
        VertexArray vao;
        IndexBuffer indexBuffer;
        Material material;
    }

    /**
     * Internal data structure for a model instance.
     */
    private static class ModelInstance {
        final String modelName;
        final Matrix4f transform;

        ModelInstance(String modelName, Matrix4f transform) {
            this.modelName = modelName;
            this.transform = transform;
        }
    }
}

