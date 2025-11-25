package me.siebe.flux.lwjgl.gltf;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import me.siebe.flux.api.renderer.models.Material;
import me.siebe.flux.api.renderer.models.Mesh;
import me.siebe.flux.api.renderer.models.Model;
import me.siebe.flux.util.assets.AssetPathResolver;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class GltfLoader {
    private static final Logger logger = LoggerFactory.getLogger(GltfLoader.class, LoggingCategories.GLTF);

    private GltfLoader() {
        // Utility class
    }

    public static Model loadAsset(String gltfFile) throws IOException {
        Path assetPath = AssetPathResolver.resolveAssetPath(gltfFile);
        GltfModelReader reader = new GltfModelReader();
        GltfModel gltfModel = reader.read(assetPath);
        Model out = new Model();

        for (MeshModel meshModel : gltfModel.getMeshModels()) {
            for (MeshPrimitiveModel primitive : meshModel.getMeshPrimitiveModels()) {
                Mesh mesh = new Mesh();

                // Attributes
                Map<String, AccessorModel> attrs = primitive.getAttributes();

                mesh.positions = readFloatAttribute(attrs.get("POSITION"));
                mesh.normals = readFloatAttribute(attrs.get("NORMAL"));
                mesh.texCoords = readFloatAttribute(attrs.get("TEXCOORD_0"));
                mesh.tangents = readFloatAttribute(attrs.get("TANGENT"));

                // Indices (maybe null -> non-indexed)
                AccessorModel indicesAccessor = primitive.getIndices();
                if (indicesAccessor != null) {
                    mesh.indices = readIndices(indicesAccessor);
                } else {
                    // generate a simple index buffer (triangle list assumption is up to the caller)
                    int vertexCount = (mesh.positions != null) ? (mesh.positions.length / 3) : 0;
                    mesh.indices = new int[vertexCount];
                    for (int i = 0; i < vertexCount; i++) {
                        mesh.indices[i] = i;
                    }
                }

                // Material (basic extraction)
                mesh.material = convertMaterial(primitive.getMaterialModel());

                out.meshes.add(mesh);
            }
        }

        return out;
    }


    // Read float attributes (POSITION, NORMAL, TEXCOORD_0, TANGENT)
    private static float[] readFloatAttribute(AccessorModel accessor) {
        if (accessor == null) return null;
        AccessorData data = accessor.getAccessorData();
        if (!(data instanceof AccessorFloatData)) {
            // unexpected component type (e.g. normalized bytes). Try to create a compact ByteBuffer and interpret floats if possible:
            // but for simplicity, return null here and let caller handle missing attributes.
            return null;
        }

        AccessorFloatData afd = (AccessorFloatData) data;
        int elements = afd.getNumElements();
        int comps = afd.getNumComponentsPerElement();
        float[] out = new float[elements * comps];
        for (int e = 0; e < elements; e++) {
            for (int c = 0; c < comps; c++) {
                out[e * comps + c] = afd.get(e, c);
            }
        }
        return out;
    }


    // Read index accessor and return int[]
    private static int[] readIndices(AccessorModel accessor) {
        if (accessor == null) return null;
        AccessorData data = accessor.getAccessorData();
        int elements = data.getNumElements();
        int[] out = new int[elements];

        switch (data) {
            case AccessorIntData aid -> {
                for (int i = 0; i < elements; i++) {
                    // indices are scalar (one component per element)
                    out[i] = aid.get(i, 0);
                }
                return out;
            }
            case AccessorShortData asd -> {
                // getInt handles unsigned shorts properly
                for (int i = 0; i < elements; i++) {
                    out[i] = asd.getInt(i, 0);
                }
                return out;
            }
            case AccessorByteData abd -> {
                // getInt handles unsigned bytes properly if necessary
                for (int i = 0; i < elements; i++) {
                    out[i] = abd.getInt(i, 0);
                }
                return out;
            }
            default -> {
            }
        }
        // fallback: try to get a compact bytebuffer and read as ints (rare)
        java.nio.ByteBuffer bb = data.createByteBuffer();
        bb.rewind();
        // assume tightly packed scalar of 4 bytes (GL_UNSIGNED_INT) - be conservative
        for (int i = 0; i < elements; i++) {
            out[i] = bb.getInt();
        }
        return out;
    }

    private static Material convertMaterial(MaterialModel materialModel) {
        Material m = new Material();
        if (materialModel == null) return m;
        if (materialModel instanceof MaterialModelV2 mm) {
            // For glTF 2.0, PBR metallic-roughness is usually accessible via getPbrMetallicRoughness()
            try {
                Object pbrObj = mm.getMetallicRoughnessTexture();
                if (pbrObj != null) {
                    // mm.getPbrMetallicRoughness() returns an implementation that typically has getBaseColorFactor() and getBaseColorTexture()
                    // but since types are in the API under different packages, just attempt a safe cast via reflection to avoid compile issues across subpackages.
                    // If you depend on the v2 model classes explicitly, you can cast to the concrete type and access typed methods.
                    // We'll attempt a simple reflection read:
                    java.lang.reflect.Method mBaseColor = pbrObj.getClass().getMethod("getBaseColorFactor");
                    Object bc = mBaseColor.invoke(pbrObj);
                    if (bc instanceof float[]) {
                        m.baseColorFactor = ((float[]) bc).clone();
                    }
                    java.lang.reflect.Method mBaseTex = pbrObj.getClass().getMethod("getBaseColorTexture");
                    Object tex = mBaseTex.invoke(pbrObj);
                    if (tex != null) {
                        java.lang.reflect.Method mImage = tex.getClass().getMethod("getImageModel");
                        Object img = mImage.invoke(tex);
                        if (img != null) {
                            java.lang.reflect.Method mUri = img.getClass().getMethod("getUri");
                            Object uri = mUri.invoke(img);
                            if (uri instanceof String) m.baseColorTexture = (String) uri;
                        }
                    }
                }
            } catch (Exception e) {
                // fail silently; this is a convenience extractor. If you want typed access,
                // import the v2 model types and cast explicitly.
            }
        }
        return m;
    }

}
