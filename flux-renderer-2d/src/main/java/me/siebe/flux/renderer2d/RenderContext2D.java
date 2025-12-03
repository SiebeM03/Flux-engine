package me.siebe.flux.renderer2d;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.lwjgl.opengl.vertex.VertexArray;

public class RenderContext2D extends BaseRenderContext {
    private static RenderContext2D instance;

    private VertexArray vertexArray;

    public static void setInstance(RenderContext2D instance) {
        RenderContext2D.instance = instance;
    }

    public static RenderContext2D getInstance() {
        return instance;
    }

    public RenderContext2D(int viewportWidth, int viewportHeight, float deltaTime, double totalTime) {
        super(viewportWidth, viewportHeight, deltaTime, totalTime);
    }

    public void setVertexArray(VertexArray vertexArray) {
        this.vertexArray = vertexArray;
    }

    public VertexArray getVertexArray() {
        return vertexArray;
    }
}
