package game.core;

import game.core.logging.GameCategories;
import me.siebe.flux.api.renderer.RenderPipeline;
import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowBuilder;
import me.siebe.flux.api.window.WindowMode;
import me.siebe.flux.api.window.WindowPlatform;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.core.FluxApplication;
import me.siebe.flux.lwjgl.opengl.shader.ShaderDataType;
import me.siebe.flux.lwjgl.opengl.vertex.*;
import me.siebe.flux.renderer2d.Basic2DRenderPipeline;
import me.siebe.flux.renderer2d.RenderContext2D;
import me.siebe.flux.renderer2d.steps.TriangleStep;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import org.joml.Matrix4f;

import java.util.List;

public class GameApplication extends FluxApplication {
    private static final Logger logger = LoggerFactory.getLogger(GameApplication.class, GameCategories.APPLICATION);

    RenderPipeline renderPipeline;

    @Override
    protected void initGameSystems() {
        logger.info("Initializing Game Systems");

        AppContext.withContextNoReturn(ctx -> {
            renderPipeline = new Basic2DRenderPipeline();
            renderPipeline.addStep(new TriangleStep());

            Matrix4f proj = new Matrix4f().ortho(
                    0, ctx.getWindow().getWidth(),
                    ctx.getWindow().getHeight(), 0,
                    -1, 1
            );
            Matrix4f view = new Matrix4f().identity();

            RenderContext2D renderCtx = new RenderContext2D(
                    ctx.getWindow().getWidth(),
                    ctx.getWindow().getHeight(),
                    (float) ctx.getTimer().getDeltaTime(),
                    ctx.getTimer().getTotalTime()
            );
            renderCtx.setProjectionMatrix(proj);
            renderCtx.setViewMatrix(view);
            renderCtx.setModelMatrix(new Matrix4f().identity());

            RenderContext2D.setInstance(renderCtx);
        });

        float[] vertices = {
                0f, 0f, 0f,
                100f, 0f, 0f,
                100f, 100f, 0f,
                0f, 100f, 0f
        };
        VertexArray vertexArray = new VertexArray();
        vertexArray.bind();

        BufferLayout layout = new BufferLayout(List.of(
                new BufferElement("aPos", ShaderDataType.Float3, false)
        ));
        VertexBuffer vertexBuffer = new VertexBuffer(vertices);
        vertexBuffer.setLayout(layout);

        vertexArray.addVertexBuffer(vertexBuffer);

        IndexBuffer indexBuffer = new IndexBuffer(new int[]{0, 1, 2, 2, 3, 0});
        vertexArray.setIndexBuffer(indexBuffer);

        RenderContext2D.getInstance().setVertexArray(vertexArray);
        renderPipeline.init(RenderContext2D.getInstance());
    }

    @Override
    protected void gameUpdate(final AppContext ctx) {
        logger.trace("Updating Game");

        renderPipeline.render(RenderContext2D.getInstance());
        RenderContext2D.getInstance().setModelMatrix(
                RenderContext2D.getInstance().getModelMatrix().translate((float) ctx.getTimer().getDeltaTime() * 5, 0f, 0f)
        );
    }

    @Override
    protected void destroyGameSystems() {
        logger.info("Destroying Game Systems");
    }

    @Override
    protected WindowBuilder createWindowBuilder() {
        return Window.builder(WindowPlatform.GLFW)
                .title("Demo Game")
                .mode(WindowMode.WINDOWED)
                .vsync(false);
    }
}
