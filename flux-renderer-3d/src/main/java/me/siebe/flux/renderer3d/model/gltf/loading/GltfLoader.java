package me.siebe.flux.renderer3d.model.gltf.loading;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
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
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Map;

public class GltfLoader {
    private static final Logger logger = LoggerFactory.getLogger(GltfLoader.class, LoggingCategories.GLTF);

    private GltfLoader() {}

    public static Model loadModel(String filePath) {
        Path assetPath = AssetPathResolver.resolveAssetPath(filePath);
        Validator.assertFileExists(assetPath);
        logger.debug("Loading gltf model from {}", assetPath);

        GltfModel gltfModel = getGltfModel(assetPath);
        return convertToFluxModel(gltfModel);
    }

    private static GltfModel getGltfModel(Path filePath) throws FileIOException {
        try {
            GltfModelReader reader = new GltfModelReader();
            return reader.read(filePath);
        } catch (IOException e) {
            throw new FileIOException("Failed to load gltf model from " + filePath, e);
        }
    }

    private static Model convertToFluxModel(GltfModel gltfModel) {
        Model out = new Model();

        for (NodeModel nodeModel : gltfModel.getNodeModels()) {
            /*
             * nodes/mesh -> Mesh (relative transform)
             * mesh's primitives -> Primitive
             * */

            // Create a basic mesh setup with name and translation
            Mesh mesh = new Mesh(nodeModel.getName());
            if (nodeModel.getTranslation() == null) {
                mesh.setRelativePosition(new Vector3f());
            } else {
                mesh.setRelativePosition(new Vector3f(nodeModel.getTranslation()));
            }


            for (MeshModel meshModel : gltfModel.getMeshModels()) {
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
                    primitiveData.indices = readIndices(indicesAccessor, primitiveData);

                    VertexArray vertexArray = createVertexArray(primitiveData);
                    Material material = createMaterial(primitiveData);

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

    private static int[] readIndices(AccessorModel accessor, PrimitiveData primitiveData) {
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
            int vertexCount = (primitiveData.positions != null) ? (primitiveData.positions.length / 3) : 0;
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
        float[] vertexData = new float[vertexCount * bufferLayout.getStride()];

        for (int v = 0; v < vertexCount; v++) {
            int vertexOffset = v * bufferLayout.getStride();
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

    private static Material createMaterial(PrimitiveData primitiveData) {
        Material material = new Material();

        material.setBaseColor(FluxColor.RED);

        return material;
    }

    private static class PrimitiveData {
        public float[] positions;
        public float[] normals;
        public float[] texCoords;
        public float[] tangents;
        public int[] indices;
    }
}
