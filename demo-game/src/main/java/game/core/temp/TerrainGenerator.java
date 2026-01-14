package game.core.temp;

import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import me.siebe.flux.lwjgl.opengl.vertex.*;
import me.siebe.flux.renderer3d.model.data.Material;
import me.siebe.flux.renderer3d.model.data.Mesh;
import me.siebe.flux.renderer3d.model.data.Model;
import me.siebe.flux.renderer3d.model.data.Primitive;
import me.siebe.flux.util.FluxColor;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.util.List;
import java.util.Random;

public class TerrainGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TerrainGenerator.class);

    private final long seed;

    public TerrainGenerator(long seed) {
        this.seed = seed;
    }

    public Model generateTerrainModel(
            int width,
            int depth,
            float scale,
            float maxHeight
    ) {
        Random random = new Random(seed);

        BufferLayout bufferLayout = new BufferLayout(
                new BufferElement("aPos", ShaderDataType.Float3, false),
                new BufferElement("aNormal", ShaderDataType.Float3, false),
                new BufferElement("aTexCoord", ShaderDataType.Float2, false),
                new BufferElement("aTangent", ShaderDataType.Float4, false)
        );

        int vertexCount = (width + 1) * (depth + 1);
        float[] vertices = new float[vertexCount * bufferLayout.getComponentCount()];

        int indexCount = width * depth * 6;
        int[] indices = new int[indexCount];

        // -----------------------------
        // Generate positions & UVs
        // -----------------------------
        for (int z = 0; z <= depth; z++) {
            for (int x = 0; x <= width; x++) {

                int index = (z * (width + 1) + x) * 12;

                float posX = x * scale;
                float posY = random.nextFloat() * maxHeight;
                float posZ = z * scale;

                // Position
                vertices[index] = posX;
                vertices[index + 1] = posY;
                vertices[index + 2] = posZ;

                // Normal (temporary, calculated later)
                vertices[index + 3] = 0;
                vertices[index + 4] = 1;
                vertices[index + 5] = 0;

                // UV
                vertices[index + 6] = x;
                vertices[index + 7] = z;

                // Tangent (temporary)
                vertices[index + 8] = 1;
                vertices[index + 9] = 0;
                vertices[index + 10] = 0;
                vertices[index + 11] = 1; // handedness
            }
        }


        // -----------------------------
        // Generate indices
        // -----------------------------
        int pointer = 0;
        for (int z = 0; z < depth; z++) {
            for (int x = 0; x < width; x++) {

                int topLeft = z * (width + 1) + x;
                int topRight = topLeft + 1;
                int bottomLeft = (z + 1) * (width + 1) + x;
                int bottomRight = bottomLeft + 1;

                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;

                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }


        calculateNormals(vertices, indices);
        calculateTangents(vertices, indices);


        VertexArray vertexArray = new VertexArray();
        vertexArray.bind();

        VertexBuffer vertexBuffer = new VertexBuffer(vertices);
        vertexBuffer.setLayout(bufferLayout);
        vertexArray.addVertexBuffer(vertexBuffer);

        IndexBuffer indexBuffer = new IndexBuffer(indices);
        vertexArray.setIndexBuffer(indexBuffer);

        Mesh mesh = new Mesh("terrain_mesh");
        mesh.addPrimitive(new Primitive(vertexArray, new Material(FluxColor.OLIVE)));
        Model model = new Model(List.of(mesh), "terrain_model");

        return model;
    }

    // -----------------------------
    // Height Function (replaceable)
    // -----------------------------
    private static float randomHeight(Random random, int x, int z, float maxHeight) {
        return random.nextFloat() * maxHeight;
    }



    // =====================================================
    // NORMAL CALCULATION
    // =====================================================
    private static void calculateNormals(float[] v, int[] indices) {

        for (int i = 0; i < indices.length; i += 3) {

            int i0 = indices[i] * 12;
            int i1 = indices[i + 1] * 12;
            int i2 = indices[i + 2] * 12;

            float[] v0 = { v[i0], v[i0 + 1], v[i0 + 2] };
            float[] v1 = { v[i1], v[i1 + 1], v[i1 + 2] };
            float[] v2 = { v[i2], v[i2 + 1], v[i2 + 2] };

            float[] edge1 = subtract(v1, v0);
            float[] edge2 = subtract(v2, v0);

            float[] normal = cross(edge1, edge2);
            normalize(normal);

            for (int j : new int[]{i0, i1, i2}) {
                v[j + 3] += normal[0];
                v[j + 4] += normal[1];
                v[j + 5] += normal[2];
            }
        }

        // Normalize all normals
        for (int i = 0; i < v.length; i += 12) {
            float[] n = { v[i + 3], v[i + 4], v[i + 5] };
            normalize(n);
            v[i + 3] = n[0];
            v[i + 4] = n[1];
            v[i + 5] = n[2];
        }
    }

    // =====================================================
    // TANGENT CALCULATION
    // =====================================================
    private static void calculateTangents(float[] v, int[] indices) {

        for (int i = 0; i < indices.length; i += 3) {

            int i0 = indices[i] * 12;
            int i1 = indices[i + 1] * 12;
            int i2 = indices[i + 2] * 12;

            float[] p0 = { v[i0], v[i0 + 1], v[i0 + 2] };
            float[] p1 = { v[i1], v[i1 + 1], v[i1 + 2] };
            float[] p2 = { v[i2], v[i2 + 1], v[i2 + 2] };

            float[] uv0 = { v[i0 + 6], v[i0 + 7] };
            float[] uv1 = { v[i1 + 6], v[i1 + 7] };
            float[] uv2 = { v[i2 + 6], v[i2 + 7] };

            float[] edge1 = subtract(p1, p0);
            float[] edge2 = subtract(p2, p0);

            float du1 = uv1[0] - uv0[0];
            float dv1 = uv1[1] - uv0[1];
            float du2 = uv2[0] - uv0[0];
            float dv2 = uv2[1] - uv0[1];

            float f = 1.0f / (du1 * dv2 - du2 * dv1);

            float[] tangent = {
                    f * (dv2 * edge1[0] - dv1 * edge2[0]),
                    f * (dv2 * edge1[1] - dv1 * edge2[1]),
                    f * (dv2 * edge1[2] - dv1 * edge2[2])
            };

            normalize(tangent);

            for (int j : new int[]{i0, i1, i2}) {
                v[j + 8]  += tangent[0];
                v[j + 9]  += tangent[1];
                v[j + 10] += tangent[2];
                v[j + 11]  = 1.0f;
            }
        }
    }

    // =====================================================
    // MATH HELPERS
    // =====================================================
    private static float[] subtract(float[] a, float[] b) {
        return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static float[] cross(float[] a, float[] b) {
        return new float[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    private static void normalize(float[] v) {
        float len = (float) Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
        if (len == 0) return;
        v[0] /= len;
        v[1] /= len;
        v[2] /= len;
    }
}
