package me.siebe.flux.renderer2d.steps;

import me.siebe.flux.api.renderer.RenderStep;
import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.lwjgl.opengl.shader.ShaderProgram;
import me.siebe.flux.renderer2d.RenderContext2D;

import static org.lwjgl.opengl.GL11.*;

public class TriangleStep implements RenderStep {
    private ShaderProgram shaderProgram;

    @Override
    public void init(BaseRenderContext context) {
        shaderProgram = new ShaderProgram("shaders/triangle");
    }

    @Override
    public void execute(BaseRenderContext context) {
        if (context instanceof RenderContext2D ctx) {
            shaderProgram.bind();
            shaderProgram.upload("uProjection", ctx.getProjectionMatrix());
            shaderProgram.upload("uView", ctx.getViewMatrix());
            shaderProgram.upload("uModel", ctx.getModelMatrix());

            ctx.getVertexArray().bind();
            glDrawElements(GL_TRIANGLES, ctx.getVertexArray().getIndexBuffer().getCount(), GL_UNSIGNED_INT, 0);
            ctx.getVertexArray().unbind();
        }
    }
}
