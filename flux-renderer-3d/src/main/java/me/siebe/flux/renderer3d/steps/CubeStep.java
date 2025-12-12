package me.siebe.flux.renderer3d.steps;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import me.siebe.flux.lwjgl.opengl.vertex.*;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

public class CubeStep implements RenderStep {
    private ShaderProgram shader;

    private Cube cube;

    @Override
    public void init() {
        shader = new ShaderProgram("shaders/base3d");

        cube = new Cube();
    }

    @Override
    public void execute(BaseRenderContext context) {
        shader.bind();
        shader.upload("uViewProj", context.getCamera().getViewProjectionMatrix());
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.scale(1);
        shader.upload("uModelMatrix", modelMatrix);

        cube.render(shader, context);

        shader.unbind();
    }

    private static class Cube {
        private VertexArray vertexArray;
        private IndexBuffer indexBuffer;

        public Cube() {
            vertexArray = new VertexArray();
            vertexArray.bind();

            BufferLayout layout = new BufferLayout(
                    new BufferElement("aPos", ShaderDataType.Float3, false)
            );
            float[] cubeVertices = new float[]{
                    // front
                    0.0f, 0.0f, 0.0f,   // 0
                    1.0f, 0.0f, 0.0f,   // 1
                    1.0f, 1.0f, 0.0f,   // 2
                    0.0f, 1.0f, 0.0f,   // 3

                    // back
                    0.0f, 0.0f, 1.0f,   // 4
                    1.0f, 0.0f, 1.0f,   // 5
                    1.0f, 1.0f, 1.0f,   // 6
                    0.0f, 1.0f, 1.0f,   // 7
            };
            VertexBuffer vertexBuffer = new VertexBuffer(cubeVertices);
            vertexBuffer.setLayout(layout);

            vertexArray.addVertexBuffer(vertexBuffer);

            int[] indices = new int[]{
                    // front
                    0, 1, 2,
                    2, 3, 0,
                    // back
                    5, 4, 6,
                    6, 4, 7,
                    // top
                    2, 3, 7,
                    2, 6, 7,
                    // bottom
                    1, 0, 5,
                    0, 4, 5,
                    // left
                    0, 7, 4,
                    7, 0, 3,
                    // right
                    2, 1, 5,
                    5, 6, 2
            };
            indexBuffer = new IndexBuffer(indices);
            vertexArray.setIndexBuffer(indexBuffer);
        }

        public void render(ShaderProgram shader, BaseRenderContext context) {
            vertexArray.bind();
            glDrawElements(GL_TRIANGLES, indexBuffer.getCount(), GL_UNSIGNED_INT, 0);
            vertexArray.unbind();
        }
    }
}
