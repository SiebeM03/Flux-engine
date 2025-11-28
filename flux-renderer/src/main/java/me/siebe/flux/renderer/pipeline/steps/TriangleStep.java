package me.siebe.flux.renderer.pipeline.steps;

import me.siebe.flux.api.renderer.RenderContext;
import me.siebe.flux.api.renderer.RenderStep;
import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import me.siebe.flux.lwjgl.opengl.vertex.*;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class TriangleStep implements RenderStep {
    private static final Logger logger = LoggerFactory.getLogger(TriangleStep.class, LoggingCategories.RENDERER);

    private String shaderPath;
    private ShaderProgram shaderProgram;
    private Triangle triangle;

    public TriangleStep(String shaderPath) {
        this.shaderPath = shaderPath;
    }

    @Override
    public void execute(RenderContext context) {
        shaderProgram.bind();
        triangle.render();
    }

    @Override
    public void init(RenderContext context) {
        logger.info("Initializing triangle render step with shader: {}", shaderPath);
        try {
            shaderProgram = new ShaderProgram(shaderPath);
            logger.info("Shader program loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load shader program '{}': {}", shaderPath, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize triangle render step", e);
        }

        this.triangle = new Triangle();
    }

    @Override
    public void destroy() {

    }


    private static class Triangle {
        private VertexArray vertexArray;
        private IndexBuffer indexBuffer;

        public Triangle() {
            vertexArray = new VertexArray();
            vertexArray.bind();

            BufferLayout layout = new BufferLayout(List.of(
                    new BufferElement("aPosition", ShaderDataType.Float3, false),
                    new BufferElement("aColor", ShaderDataType.Float4, false)
            ));
            float[] data = new float[]{
                    -0.5f, -0.5f, 0.0f,     1.0f, 0.0f, 0.0f, 1.0f,
                    0.5f, -0.5f, 0.0f,      1.0f, 0.0f, 1.0f, 1.0f,
                    0.0f,  0.5f, 0.0f,      1.0f, 1.0f, 0.0f, 1.0f,
            };

            VertexBuffer vertexBuffer = new VertexBuffer(data);
            vertexBuffer.setLayout(layout);

            vertexArray.addVertexBuffer(vertexBuffer);

            indexBuffer = new IndexBuffer(new int[]{0, 2, 1});
            vertexArray.setIndexBuffer(indexBuffer);
        }

        public void render() {
            vertexArray.bind();
            glDrawElements(GL_TRIANGLES, indexBuffer.getCount(), GL_UNSIGNED_INT, 0);
            vertexArray.unbind();
        }
    }
}
