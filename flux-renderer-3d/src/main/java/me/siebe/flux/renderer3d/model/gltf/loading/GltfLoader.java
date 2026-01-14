package me.siebe.flux.renderer3d.model.gltf.loading;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import me.siebe.flux.lwjgl.opengl.texture.Texture;
import me.siebe.flux.lwjgl.opengl.vertex.*;
import me.siebe.flux.renderer3d.model.data.Material;
import me.siebe.flux.renderer3d.model.data.Mesh;
import me.siebe.flux.renderer3d.model.data.Model;
import me.siebe.flux.renderer3d.model.data.Primitive;
import me.siebe.flux.util.FluxColor;
import me.siebe.flux.util.assets.AssetPathResolver;
import me.siebe.flux.util.assets.AssetPool;
import me.siebe.flux.util.exceptions.Validator;
import me.siebe.flux.util.io.FileIOException;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

/**
 * Utility class for loading GLTF (GL Transmission Format) 3D models and converting them
 * into the Flux engine's internal model representation.
 * <p>
 * This loader supports GLTF 2.0 models with the following features:
 * <ul>
 *   <li>Mesh geometry (positions, normals, texture coordinates, tangents)</li>
 *   <li>Indexed and non-indexed primitives</li>
 *   <li>PBR materials (base color, metallic-roughness, normal maps, occlusion, emissive)</li>
 *   <li>Texture loading from embedded or external image data</li>
 *   <li>Node transformations (translation, rotation, scale)</li>
 * </ul>
 * <p>
 *
 * @apiNote GLTF extensions are not currently supported. Texture coordinate indices
 * other than the default (TEXCOORD_0) are not supported and will throw an exception.
 */
public class GltfLoader extends AssetPool<Model> {
    private static final Logger logger = LoggerFactory.getLogger(GltfLoader.class, LoggingCategories.GLTF);
    /** Error message that is used when GLTF's texCoord for a certain texture is not null and thus might require some attention */
    private static final String texCoordAlreadyExistsErrorMsg = " is not null, this might require attention to make sure the GLTF loading works as expected (texcoords are currently ignored as they were always null during development/testing)";

    private static GltfLoader instance;

    private HashMap<String, Model> models;

    private GltfLoader() {
        this.models = new HashMap<>();
    }

    public static GltfLoader get() {
        if (instance == null) instance = new GltfLoader();
        return instance;
    }

    /**
     * Loads a GLTF model from the specified file path and converts it to a Flux Model.
     * <p>
     * The file path is resolved using the asset path resolver, and the resulting model
     * contains all meshes, primitives, materials, and textures from the GLTF file.
     *
     * @param filePath the path to the GLTF file (relative to the asset root)
     * @return a Model containing all meshes and materials from the GLTF file
     * @throws FileIOException  if the file cannot be read or is invalid
     * @throws RuntimeException if the GLTF model contains unsupported features (e.g., non-default texture coordinate indices)
     */
    @Override
    protected Model create(String filePath) {
        // TODO add support for .glb files. It mostly works already but the textures seem to be off (light has no visual effect on the model)
        Path assetPath = AssetPathResolver.resolveAssetPath(filePath);
        Validator.assertFileExists(assetPath);
        logger.debug("Loading gltf model from {}", assetPath);

        GltfModel gltfModel = getGltfModel(assetPath);
        return convertToFluxModel(gltfModel);
    }

    /**
     * Reads a GLTF model from the specified file path using the jgltf library.
     *
     * @param filePath the path to the GLTF file
     * @return the loaded GltfModel
     * @throws FileIOException if an I/O error occurs while reading the file
     */
    private GltfModel getGltfModel(Path filePath) throws FileIOException {
        try {
            GltfModelReader reader = new GltfModelReader();
            return reader.read(filePath);
        } catch (IOException e) {
            throw new FileIOException("Failed to load gltf model from " + filePath, e);
        }
    }

    /**
     * Converts a GltfModel from the jgltf library into a Flux Model.
     * <p>
     * This method processes all nodes in the GLTF model, extracting:
     * <ul>
     *   <li>Mesh primitives with vertex attributes (position, normal, texture coordinates, tangents)</li>
     *   <li>Index buffers for indexed rendering</li>
     *   <li>Materials with PBR properties and textures</li>
     * </ul>
     *
     * @param gltfModel the GLTF model to convert
     * @return a Model containing all converted meshes and materials
     */
    private Model convertToFluxModel(GltfModel gltfModel) {
        Model out = new Model();

        for (NodeModel nodeModel : gltfModel.getNodeModels()) {
            Mesh mesh = getMesh(nodeModel);

            for (MeshModel meshModel : nodeModel.getMeshModels()) {
                for (MeshPrimitiveModel primitiveModel : meshModel.getMeshPrimitiveModels()) {
                    // Attributes
                    Map<String, AccessorModel> attrs = primitiveModel.getAttributes();
                    PrimitiveData primitiveData = new PrimitiveData();

                    primitiveData.positions = readFloatAttribute(attrs.get("POSITION"));
                    primitiveData.normals = readFloatAttribute(attrs.get("NORMAL"));
                    primitiveData.texCoords = readFloatAttribute(attrs.get("TEXCOORD_0"));
                    primitiveData.tangents = readFloatAttribute(attrs.get("TANGENT"));

                    // Indices
                    AccessorModel indicesAccessor = primitiveModel.getIndices();
                    primitiveData.indices = readIndices(indicesAccessor, primitiveData.positions);

                    VertexArray vertexArray = createVertexArray(primitiveData);
                    Material material = createMaterial(primitiveModel);

                    mesh.addPrimitive(new Primitive(vertexArray, material));
                }
            }

            out.addMesh(mesh);
        }

        return out;
    }

    /**
     * Creates a Mesh from a GLTF node model, extracting transformation properties.
     * <p>
     * This method extracts the following properties from the node:
     * <ul>
     *   <li>Mesh name from the node name</li>
     *   <li>Translation (3D position vector) if present</li>
     *   <li>Rotation (quaternion with x, y, z, w components) if present</li>
     *   <li>Scale (3D scale vector) if present</li>
     * </ul>
     * Missing transformation properties are left at their default values in the Mesh.
     *
     * @param nodeModel the GLTF node model containing mesh and transformation data
     * @return a Mesh with name and transformations extracted from the node
     */
    private Mesh getMesh(NodeModel nodeModel) {
        Mesh mesh = new Mesh(nodeModel.getName());

        float[] matrixArray = nodeModel.getMatrix();

        if (matrixArray != null && matrixArray.length == 16) {
            // glTF matrices are column-major → JOML matches this
            Matrix4f matrix = new Matrix4f().set(matrixArray);

            // Decompose matrix
            Vector3f translation = new Vector3f();
            matrix.getTranslation(translation);
            mesh.setRelativePosition(translation);

            Vector3f scale = new Vector3f();
            matrix.getScale(scale);
            mesh.setScale(scale);

            Quaternionf rotation = new Quaternionf();
            matrix.getUnnormalizedRotation(rotation).normalize();
            mesh.setRotation(rotation);
        } else {
            // Load translation
            float[] translation = nodeModel.getTranslation();
            if (translation != null && translation.length == 3) {
                mesh.setRelativePosition(new Vector3f(nodeModel.getTranslation()));
            }

            // Load rotation (quaternion: x, y, z, w)
            float[] rotation = nodeModel.getRotation();
            if (rotation != null && rotation.length == 4) {
                mesh.setRotation(new Quaternionf(rotation[0], rotation[1], rotation[2], rotation[3]));
            }

            // Load scale
            float[] scale = nodeModel.getScale();
            if (scale != null && scale.length == 3) {
                mesh.setScale(new Vector3f(scale));
            }
        }
        return mesh;
    }

    /**
     * Reads float attribute data from a GLTF accessor.
     * <p>
     * This method extracts float data from accessors used for vertex attributes such as
     * POSITION, NORMAL, TEXCOORD_0, and TANGENT. The data is returned as a flat float array
     * with all elements and components interleaved.
     *
     * @param accessor the accessor model containing the attribute data, or null if the attribute is missing
     * @return a float array containing the attribute data, or null if the accessor is null
     * or contains non-float data (e.g., normalized bytes)
     */
    private float[] readFloatAttribute(AccessorModel accessor) {
        if (accessor == null) return null;
        AccessorData data = accessor.getAccessorData();

        if (data instanceof AccessorFloatData floatData) {
            int elements = floatData.getNumElements();
            int componentsPerElement = floatData.getNumComponentsPerElement();
            float[] out = new float[elements * componentsPerElement];
            for (int e = 0; e < elements; e++) {
                for (int c = 0; c < componentsPerElement; c++) {
                    out[e * componentsPerElement + c] = floatData.get(e, c);
                }
            }
            return out;
        } else {
            // Unexpected component type (e.g. normalized bytes). Try to create a compact ByteBuffer and interpret
            // floats if possible: but for simplicity, return null here and let caller handle missing attributes.
            // FIXME implement alternative way to load buffer data
            return null;
        }
    }

    /**
     * Reads index data from a GLTF accessor, or generates sequential indices if no accessor is provided.
     * <p>
     * Supports indices stored as integers, shorts, or bytes. If no index accessor is provided
     * (non-indexed geometry), this method generates sequential indices based on the vertex count
     * derived from the positions array.
     *
     * @param accessor  the accessor model containing the index data, or null for non-indexed geometry
     * @param positions the position array used to determine vertex count when generating indices
     * @return an array of indices, or a sequential index array if no accessor is provided
     */
    private int[] readIndices(AccessorModel accessor, float[] positions) {
        if (accessor != null) {
            AccessorData data = accessor.getAccessorData();
            int elements = data.getNumElements();
            int[] out = new int[elements];

            switch (data) {
                case AccessorIntData intData -> {
                    for (int i = 0; i < elements; i++) {
                        out[i] = intData.get(i, 0);
                    }
                }
                case AccessorShortData shortData -> {
                    for (int i = 0; i < elements; i++) {
                        out[i] = shortData.getInt(i, 0);
                    }
                }
                case AccessorByteData byteData -> {
                    for (int i = 0; i < elements; i++) {
                        out[i] = byteData.getInt(i, 0);
                    }
                }
                default -> {
                    // Fallback: try to get a byte buffer and read it as ints
                    ByteBuffer byteBuffer = data.createByteBuffer();
                    byteBuffer.rewind();
                    // assume tightly packed scalar of 4 bytes (GL_UNSIGNED_BYTE) - be conservative
                    for (int i = 0; i < elements; i++) {
                        out[i] = byteBuffer.getInt();
                    }
                }
            }

            return out;
        } else {
            // No indices found -> non-indexed. Generate a simple index buffer (triangle list assumption is up to the caller)
            int vertexCount = (positions != null) ? (positions.length / 3) : 0;
            int[] out = new int[vertexCount];
            for (int i = 0; i < vertexCount; i++) {
                out[i] = i;
            }
            return out;
        }
    }

    /**
     * Creates a VertexArray from primitive data, setting up vertex buffers and index buffers.
     * <p>
     * This method creates a vertex array with a fixed layout containing:
     * <ul>
     *   <li>Position (3 floats, required)</li>
     *   <li>Normal (3 floats, defaults to [0,0,0] if missing)</li>
     *   <li>Texture coordinates (2 floats, defaults to [0,0] if missing)</li>
     *   <li>Tangent (4 floats including handedness, defaults to [0,0,0,0] if missing)</li>
     * </ul>
     * Missing attributes are filled with default values. The vertex array is bound during creation
     * and unbound before returning.
     *
     * @param primitiveData the primitive data containing vertex attributes and indices
     * @return a configured VertexArray ready for rendering
     * @throws IllegalArgumentException if primitiveData does not contain positions
     */
    private VertexArray createVertexArray(PrimitiveData primitiveData) {
        if (primitiveData.positions == null || primitiveData.positions.length == 0) {
            throw new IllegalArgumentException("PrimitiveData must have positions");
        }

        // Create and bind vertex array that will be returned
        VertexArray vertexArray = new VertexArray();
        vertexArray.bind();

        // Determine vertex count from positions (assuming 3 components per position)
        int vertexCount = primitiveData.positions.length / 3;

        // Determine which attributes are present
        boolean hasNormals = primitiveData.normals != null && primitiveData.normals.length >= vertexCount * 3;
        boolean hasTexCoords = primitiveData.texCoords != null && primitiveData.texCoords.length >= vertexCount * 2;
        boolean hasTangents = primitiveData.tangents != null && primitiveData.tangents.length >= vertexCount * 4;

        // Create buffer layout
        // TODO find better way to sync shaders and bufferlayouts
        BufferElement posBe = new BufferElement("aPos", ShaderDataType.Float3, false);
        BufferElement norBe = new BufferElement("aNormal", ShaderDataType.Float3, false);
        BufferElement texBe = new BufferElement("aTexCoord", ShaderDataType.Float2, false);
        BufferElement tanBe = new BufferElement("aTangent", ShaderDataType.Float4, false);

        BufferLayout bufferLayout = new BufferLayout(posBe, norBe, texBe, tanBe);

        // Populate vertex data
        float[] vertexData = new float[vertexCount * bufferLayout.getComponentCount()];

        for (int v = 0; v < vertexCount; v++) {
            int vertexOffset = v * bufferLayout.getComponentCount();
            int posIndex = v * posBe.getComponentSize();
            int norIndex = v * norBe.getComponentSize();
            int texIndex = v * texBe.getComponentSize();
            int tanIndex = v * tanBe.getComponentSize();

            // Position (always present)
            vertexData[vertexOffset + 0] = primitiveData.positions[posIndex + 0];
            vertexData[vertexOffset + 1] = primitiveData.positions[posIndex + 1];
            vertexData[vertexOffset + 2] = primitiveData.positions[posIndex + 2];

            // Normal (3 floats, defaults to [0.0f, 0.0f, 0.0f])
            if (hasNormals) {
                vertexData[vertexOffset + 3] = primitiveData.normals[norIndex + 0];
                vertexData[vertexOffset + 4] = primitiveData.normals[norIndex + 1];
                vertexData[vertexOffset + 5] = primitiveData.normals[norIndex + 2];
            } else {
                vertexData[vertexOffset + 3] = 0.0f;
                vertexData[vertexOffset + 4] = 0.0f;
                vertexData[vertexOffset + 5] = 0.0f;
            }

            // TexCoords (2 floats, defaults to [0.0f, 0.0f])
            if (hasTexCoords) {
                vertexData[vertexOffset + 6] = primitiveData.texCoords[texIndex + 0];
                vertexData[vertexOffset + 7] = primitiveData.texCoords[texIndex + 1];
            } else {
                vertexData[vertexOffset + 6] = 0.0f;
                vertexData[vertexOffset + 7] = 0.0f;
            }

            // Tangent (4 floats - includes w component for handedness, defaults to [0.0f, 0.0f, 0.0f, 0.0f])
            if (hasTangents) {
                vertexData[vertexOffset + 8] = primitiveData.tangents[tanIndex + 0];
                vertexData[vertexOffset + 9] = primitiveData.tangents[tanIndex + 1];
                vertexData[vertexOffset + 10] = primitiveData.tangents[tanIndex + 2];
                vertexData[vertexOffset + 11] = primitiveData.tangents[tanIndex + 3];
            } else {
                vertexData[vertexOffset + 8] = 0.0f;
                vertexData[vertexOffset + 9] = 0.0f;
                vertexData[vertexOffset + 10] = 0.0f;
                vertexData[vertexOffset + 11] = 0.0f;
            }
        }

        // Create vertex buffer and add the vertex data
        VertexBuffer vertexBuffer = new VertexBuffer(vertexData);
        vertexBuffer.setLayout(bufferLayout);

        // Add the vertex buffer to the vertex array
        vertexArray.addVertexBuffer(vertexBuffer);

        // Create and set index buffer
        if (primitiveData.indices != null && primitiveData.indices.length > 0) {
            IndexBuffer indexBuffer = new IndexBuffer(primitiveData.indices);
            vertexArray.setIndexBuffer(indexBuffer);
        }

        vertexArray.unbind();
        return vertexArray;
    }

    /**
     * Creates a Material from a GLTF mesh primitive model.
     * <p>
     * Extracts PBR material properties from GLTF 2.0 materials, including:
     * <ul>
     *   <li>Base color factor and albedo texture</li>
     *   <li>Metallic and roughness factors with combined texture</li>
     *   <li>Normal map texture</li>
     *   <li>Occlusion strength and texture</li>
     *   <li>Emissive factor and texture</li>
     *   <li>Alpha mode (OPAQUE, MASK, BLEND)</li>
     *   <li>Double-sided rendering flag</li>
     * </ul>
     * <p>
     * Currently only supports MaterialModelV2 (GLTF 2.0). Other material types will log
     * an error and return a default material.
     *
     * @param primitiveModel the mesh primitive model containing material information
     * @return a Material with properties extracted from the GLTF material
     * @throws RuntimeException if the material uses non-default texture coordinate indices
     */
    private Material createMaterial(MeshPrimitiveModel primitiveModel) {
        Material material = new Material();

        MaterialModel gltfGenericMaterial = primitiveModel.getMaterialModel();
        if (gltfGenericMaterial != null) {
            material.setName(gltfGenericMaterial.getName());

            switch (gltfGenericMaterial) {
                case MaterialModelV2 gltfMaterial -> {
                    // Base color
                    material.setBaseColor(new FluxColor(gltfMaterial.getBaseColorFactor()));
                    material.setAlbedoTexture(loadTexture(gltfMaterial.getBaseColorTexture()));
                    if (gltfMaterial.getBaseColorTexcoord() != null)
                        throw new RuntimeException("BaseColorTexcoord" + texCoordAlreadyExistsErrorMsg);

                    // Metallic-roughness
                    material.setMetallicFactor(gltfMaterial.getMetallicFactor());
                    material.setRoughnessFactor(gltfMaterial.getRoughnessFactor());
                    material.setMetallicRoughnessTexture(loadTexture(gltfMaterial.getMetallicRoughnessTexture()));
                    if (gltfMaterial.getMetallicRoughnessTexcoord() != null)
                        throw new RuntimeException("MetallicRoughnessTexcoord" + texCoordAlreadyExistsErrorMsg);

                    // Normal
                    material.setNormalTexture(loadTexture(gltfMaterial.getNormalTexture()));
                    if (gltfMaterial.getNormalTexcoord() != null) {
                        material.getNormalTexture().setFilters(GL_LINEAR, GL_LINEAR);
                    }
                    if (gltfMaterial.getNormalTexcoord() != null)
                        throw new RuntimeException("NormalTexcoord" + texCoordAlreadyExistsErrorMsg);

                    // Occlusion
                    material.setOcclusionStrength(gltfMaterial.getOcclusionStrength());
                    material.setOcclusionTexture(loadTexture(gltfMaterial.getOcclusionTexture()));
                    if (gltfMaterial.getOcclusionTexcoord() != null)
                        throw new RuntimeException("OcclusionTexcoord" + texCoordAlreadyExistsErrorMsg);

                    // Emissive
                    material.setEmissiveFactor(new Vector3f(gltfMaterial.getEmissiveFactor()));
                    material.setEmissiveTexture(loadTexture(gltfMaterial.getEmissiveTexture()));
                    if (gltfMaterial.getEmissiveTexcoord() != null)
                        throw new RuntimeException("EmissiveTexcoord" + texCoordAlreadyExistsErrorMsg);

                    // AlphaMode
                    try {
                        Material.AlphaMode alphaMode = Material.AlphaMode.valueOf(gltfMaterial.getAlphaMode().name());
                        material.setAlphaMode(alphaMode);
                    } catch (IllegalArgumentException logged) {
                        logger.error("Invalid alpha mode found in loaded GLTF model: {}", gltfMaterial.getAlphaMode().name());
                    }

                    // Double-sided
                    material.setDoubleSided(gltfMaterial.isDoubleSided());
                }
                default -> logger.error("Unsupported GLTF material found while loading model");
            }
            // TODO add support for GLTF extensions
        }

        return material;
    }

    /**
     * Loads a texture from a GLTF texture model.
     * <p>
     * This method extracts image data from the GLTF model and uses STBImage to decode it.
     * The texture is configured with:
     * <ul>
     *   <li>Minification and magnification filters from the GLTF model (defaults to GL_LINEAR_MIPMAP_LINEAR and GL_LINEAR)</li>
     *   <li>Wrap modes for S and T coordinates from the GLTF model</li>
     *   <li>RGBA format (RGB images are padded with alpha=255)</li>
     * </ul>
     * <p>
     * The image data is flipped vertically during loading to account for GLTF's bottom-up
     * coordinate system. STBImage memory is freed after texture creation.
     *
     * @param textureModel the texture model containing image data, or null if no texture
     * @return a Texture object ready for use, or null if textureModel is null or image data is unavailable
     * @throws RuntimeException if STBImage fails to decode the image data
     */
    private Texture loadTexture(TextureModel textureModel) {
        if (textureModel == null) return null;
        ImageModel imageModel = textureModel.getImageModel();
        if (imageModel == null) return null;

        // Path imagePath = assetPath.getParent().resolve(imageModel.getUri());
        ByteBuffer textureData = imageModel.getImageData();
        if (textureData == null) return null; // FIXME possibly create ByteBuffer from the file itself??

        // Make sure buffer is at position 0 and limit is set correctly
        textureData.rewind();
        if (textureData.remaining() == 0) return null;  // Texture data buffer is empty

        // STBImage requires a direct buffer. If the buffer is not direct, create a copy
        ByteBuffer directBuffer;
        if (textureData.isDirect()) {
            directBuffer = textureData;
        } else {
            directBuffer = BufferUtils.createByteBuffer(textureData.remaining());
            int oldPosition = textureData.position();
            textureData.rewind();
            directBuffer.put(textureData);
            directBuffer.flip();
            textureData.position(oldPosition);
        }

        // Ensure the buffer is positioned at the start
        directBuffer.rewind();

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        STBImage.stbi_set_flip_vertically_on_load(false);   // GLTF uses bottom-up coordinates

        // Request desired channels (4 = RGBA), but STB will return what's available so this might be ignored
        ByteBuffer pixels = STBImage.stbi_load_from_memory(directBuffer, width, height, channels, 4);
        if (pixels == null) {
            String reason = STBImage.stbi_failure_reason();
            throw new RuntimeException("Failed to load texture " + imageModel.getUri() + ": " + (reason == null ? "Unknown error" : reason));
        }

        int w = width.get(0);
        int h = height.get(0);

        /*
        When we request 4 channels, STBImage will:
        - Return 4 channels (RGBA) in the pixel buffer
        - Set comp to 4 (the requested channel count)
        - Pad RGB images with alpha=255
        So we always use RGBA format when we requested 4 channels
        */

        Texture texture = new Texture(w, h, GL_TEXTURE_2D, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        texture.bind();
        // Typical GLTF filters:
        // - GL_TEXTURE_MIN_FILTER: 9987 -> GL_LINEAR_MIPMAP_LINEAR
        // - GL_TEXTURE_MAG_FILTER: 9729 -> GL_LINEAR
        texture.setFilters(
                textureModel.getMinFilter() != null ? textureModel.getMinFilter() : GL_LINEAR_MIPMAP_LINEAR,
                textureModel.getMagFilter() != null ? textureModel.getMagFilter() : GL_LINEAR
        );
        // Typical GLTF wrap options:
        // - GL_TEXTURE_WRAP_S: 10497 -> GL_REPEAT
        // - GL_TEXTURE_WRAP_T: 10497 -> GL_REPEAT
        texture.setWrap(textureModel.getWrapS(), textureModel.getWrapT());
        texture.unbind();

        // Free STB image data
        STBImage.stbi_image_free(pixels);
        return texture;
    }

    /**
     * Internal data structure for holding primitive vertex and index data during GLTF conversion.
     * <p>
     * This class is used as an intermediate representation when processing GLTF mesh primitives
     * before creating the final VertexArray and Material objects.
     */
    private static class PrimitiveData {
        /** Vertex positions as a flat array of 3 floats per vertex (x, y, z). */
        public float[] positions;

        /** Vertex normals as a flat array of 3 floats per vertex (x, y, z). May be null if not present. */
        public float[] normals;

        /** Texture coordinates as a flat array of 2 floats per vertex (u, v). May be null if not present. */
        public float[] texCoords;

        /** Vertex tangents as a flat array of 4 floats per vertex (x, y, z, w) where w indicates handedness. May be null if not present. */
        public float[] tangents;

        /** Index buffer for indexed rendering. May be null for non-indexed geometry. */
        public int[] indices;
    }
}
