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
import me.siebe.flux.util.exceptions.Validator;
import me.siebe.flux.util.io.FileIOException;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class GltfLoader {
    private static final Logger logger = LoggerFactory.getLogger(GltfLoader.class, LoggingCategories.GLTF);

    private GltfLoader() {}

    public static Model loadModel(String filePath) {
        Path assetPath = AssetPathResolver.resolveAssetPath(filePath);
        Validator.assertFileExists(assetPath);
        logger.debug("Loading gltf model from {}", assetPath);

        GltfModel gltfModel = getGltfModel(assetPath);
        return convertToFluxModel(gltfModel, assetPath);
    }

    private static GltfModel getGltfModel(Path filePath) throws FileIOException {
        try {
            GltfModelReader reader = new GltfModelReader();
            return reader.read(filePath);
        } catch (IOException e) {
            throw new FileIOException("Failed to load gltf model from " + filePath, e);
        }
    }

    private static Model convertToFluxModel(GltfModel gltfModel, Path assetPath) {
        Model out = new Model();

        for (NodeModel nodeModel : gltfModel.getNodeModels()) {
            // Create a basic mesh setup with name and translation
            Mesh mesh = new Mesh(nodeModel.getName());
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

    // Read float attributes (POSITION, NORMAL, TEXCOORD_0, TANGENT)
    private static float[] readFloatAttribute(AccessorModel accessor) {
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
            return null;
        }
    }

    private static int[] readIndices(AccessorModel accessor, float[] positions) {
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

    private static VertexArray createVertexArray(PrimitiveData primitiveData) {
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
     * Creates a Material from the GLTF primitive model.
     * Extracts all material properties including base color, metallic/roughness,
     * textures, emissive properties, occlusion, and alpha settings.
     * <p>
     * Uses the jgltf extension mechanism to access material properties.
     *
     * @param primitiveModel the GLTF primitive model containing material reference
     * @return the created Material instance
     */
    private static Material createMaterial(MeshPrimitiveModel primitiveModel) {
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
                        throw texCoordsNotNull("BaseColorTexcoord");

                    // Metallic-roughness
                    material.setMetallicFactor(gltfMaterial.getMetallicFactor());
                    material.setRoughnessFactor(gltfMaterial.getRoughnessFactor());
                    material.setMetallicRoughnessTexture(loadTexture(gltfMaterial.getMetallicRoughnessTexture()));
                    if (gltfMaterial.getMetallicRoughnessTexcoord() != null)
                        throw texCoordsNotNull("MetallicRoughnessTexcoord");

                    // Normal
                    material.setNormalTexture(loadTexture(gltfMaterial.getNormalTexture()));
                    if (gltfMaterial.getNormalTexcoord() != null) {
                        material.getNormalTexture().setFilters(GL_LINEAR, GL_LINEAR);
                    }
                    if (gltfMaterial.getNormalTexcoord() != null)
                        throw texCoordsNotNull("NormalTexcoord");

                    // Occlusion
                    material.setOcclusionStrength(gltfMaterial.getOcclusionStrength());
                    material.setOcclusionTexture(loadTexture(gltfMaterial.getOcclusionTexture()));
                    if (gltfMaterial.getOcclusionTexcoord() != null)
                        throw texCoordsNotNull("OcclusionTexcoord");

                    // Emissive
                    material.setEmissiveFactor(new Vector3f(gltfMaterial.getEmissiveFactor()));
                    material.setEmissiveTexture(loadTexture(gltfMaterial.getEmissiveTexture()));
                    if (gltfMaterial.getEmissiveTexcoord() != null)
                        throw texCoordsNotNull("EmissiveTexcoord");

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
     * Loads a texture from the GLTF model.
     * Extracts the image data from the texture model and creates a Texture instance.
     *
     * @param textureModel the GLTF texture model
     * @return the loaded Texture, or null if loading fails
     */
    private static Texture loadTexture(TextureModel textureModel) {
        if (textureModel == null) return null;
        ImageModel imageModel = textureModel.getImageModel();
        if (imageModel == null) return null;

        // Path imagePath = assetPath.getParent().resolve(imageModel.getUri());
        ByteBuffer textureData = imageModel.getImageData();
        if (textureData == null) return null; // TODO possibly create ByteBuffer from the file itself??

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

    private static class PrimitiveData {
        public float[] positions;
        public float[] normals;
        public float[] texCoords;
        public float[] tangents;
        public int[] indices;
    }

    /**
     * This will simply create a RuntimeException for when a {@link MaterialModelV2}'s texcoords are not null.
     * <p>
     * It alerts Flux developers that the GLTF loading might require attention
     *
     * @param name the name of the texcoords
     */
    private static RuntimeException texCoordsNotNull(String name) {
        return new RuntimeException(name + " is not null, this might require attention to make sure the GLTF loading works as expected (texcoords are currently ignored as they were always null during development/testing)");
    }
}
