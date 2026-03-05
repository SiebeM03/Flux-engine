package me.siebe.flux.ui.render;

import me.siebe.flux.api.event.common.FramebufferResizeEvent;
import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.opengl.OpenGLState;
import me.siebe.flux.opengl.shader.ShaderLoader;
import me.siebe.flux.opengl.shader.ShaderProgram;
import me.siebe.flux.ui.UIScene;
import me.siebe.flux.util.DirtyValue;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class UiRenderStep implements RenderStep {
    private ShaderProgram shader;
    private UiSceneRenderDataBuilder dataBuilder;
    private DirtyValue<Matrix4f> projectionMatrix;

    protected ShaderProgram getShader() {
        return ShaderLoader.get().load("shaders/ui");
    }

    @Override
    public void init() {
        shader = getShader();
        dataBuilder = new UiSceneRenderDataBuilder();

        projectionMatrix = new DirtyValue<>(new Matrix4f(), m -> {
            AppContext.withContextNoReturn(ctx -> {
                m.identity().ortho(
                        0, ctx.getWindow().getWidth(),
                        0, ctx.getWindow().getHeight(),
                        -1, 1
                );
            });
        });
        AppContext.get().getEventBus().getListenerRegistry().register(FramebufferResizeEvent.class, e -> projectionMatrix.markDirty());
    }

    @Override
    public void prepare(BaseRenderContext context) {
        shader = getShader();
        shader.bind();
        OpenGLState.disableDepthTest();
        OpenGLState.enableBlend();
        OpenGLState.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.upload("uProjection", projectionMatrix.get());
    }

    @Override
    public void execute(BaseRenderContext context) {
        for (UIScene scene : AppContext.get().getUi().getScenes()) {
            dataBuilder.addScene(scene);

            OpenGLState.drawElements(dataBuilder.getVertexArray());

            dataBuilder.clear();
        }
    }

    @Override
    public void finish(BaseRenderContext context) {
        OpenGLState.enableDepthTest();
        shader.unbind();
    }

    @Override
    public void destroy() {
        shader.delete();
        dataBuilder.delete();
    }
}
