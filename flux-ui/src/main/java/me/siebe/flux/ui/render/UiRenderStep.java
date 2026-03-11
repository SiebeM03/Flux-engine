package me.siebe.flux.ui.render;

import me.siebe.flux.api.event.common.FramebufferResizeEvent;
import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.api.ui.UIElement;
import me.siebe.flux.api.ui.UIScene;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.opengl.OpenGLState;
import me.siebe.flux.opengl.shader.ShaderDataType;
import me.siebe.flux.opengl.shader.ShaderLoader;
import me.siebe.flux.opengl.shader.ShaderProgram;
import me.siebe.flux.opengl.vertex.BufferElement;
import me.siebe.flux.opengl.vertex.BufferLayout;
import me.siebe.flux.ui.components.UiTexturedElement;
import me.siebe.flux.util.DirtyValue;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class UiRenderStep implements RenderStep {
    private ShaderProgram shader;
    private DirtyValue<Matrix4f> projectionMatrix;

    private RenderBatch batch;

    protected ShaderProgram getShader() {
        return ShaderLoader.get().load("shaders/ui");
    }

    @Override
    public void init() {
        shader = getShader();

        projectionMatrix = new DirtyValue<>(new Matrix4f(),
                m -> AppContext.withContextNoReturn(
                        ctx -> m.identity().ortho(
                                0, ctx.getWindow().getWidth(),
                                0, ctx.getWindow().getHeight(),
                                -1, 1
                        )));
        AppContext.get().getEventBus().getListenerRegistry().register(FramebufferResizeEvent.class, e -> projectionMatrix.markDirty());

        this.batch = new RenderBatch(2000, RenderPrimitive.QUAD, new BufferLayout(
                new BufferElement("aPos", ShaderDataType.Float2, false),
                new BufferElement("aColor", ShaderDataType.Float4, false),
                new BufferElement("aTexCoords", ShaderDataType.Float2, false),
                new BufferElement("aTexId", ShaderDataType.Float, false)
        ));
        this.batch.init();
    }

    @Override
    public void prepare(BaseRenderContext context) {
        shader = getShader();
        shader.bind();
        OpenGLState.disableDepthTest();
        OpenGLState.enableBlend();
        OpenGLState.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.upload("uProjection", projectionMatrix.get());
        shader.upload("uTextures", new int[]{0, 1, 2, 3, 4, 5, 6, 7});

        batch.clearBuffer();
        rebuffer();
        batch.submitBuffer();
    }

    @Override
    public void execute(BaseRenderContext context) {
        shader.bind();
        batch.render();
    }

    @Override
    public void finish(BaseRenderContext context) {
        OpenGLState.enableDepthTest();
        OpenGLState.disableBlend();
        shader.unbind();
    }

    @Override
    public void destroy() {
        shader.delete();
    }

    private void rebuffer() {
        for (UIScene scene : AppContext.get().getUi().getScenes()) {
            addUiElementToBatch(scene.getRoot());
        }
    }


    private void addUiElementToBatch(UIElement element) {
        if (!element.isVisible()) return;

        int texID;
        if (element instanceof UiTexturedElement texturedElement) {
            texID = batch.addTexture(texturedElement.getTexture());
        } else {
            texID = batch.addTexture(null);
        }

        for (int i = 0; i < 4; i++) {
            //  i3 ---------- i0
            //  |              |
            //  i2 ---------- i1
            float xAdd = (i == 0 || i == 1) ? element.getWidth() : 0;
            float yAdd = (i == 0 || i == 3) ? element.getHeight() : 0;

            // aPos
            batch.vertexData.add(element.getX() + xAdd);
            batch.vertexData.add(element.getY() + yAdd);
            // aColor
            batch.vertexData.add(element.getBackground().redFloat());
            batch.vertexData.add(element.getBackground().greenFloat());
            batch.vertexData.add(element.getBackground().blueFloat());
            batch.vertexData.add(element.getBackground().alphaFloat());
            // aTexCoords
            switch (i) {
                case 0 -> {
                    batch.vertexData.add(1);
                    batch.vertexData.add(1);
                }
                case 1 -> {
                    batch.vertexData.add(1);
                    batch.vertexData.add(0);
                }
                case 2 -> {
                    batch.vertexData.add(0);
                    batch.vertexData.add(0);
                }
                case 3 -> {
                    batch.vertexData.add(0);
                    batch.vertexData.add(1);
                }
            }
            // aTexId
            batch.vertexData.add(texID);
        }

        for (UIElement child : element.getChildren()) {
            addUiElementToBatch(child);
        }
    }
}
