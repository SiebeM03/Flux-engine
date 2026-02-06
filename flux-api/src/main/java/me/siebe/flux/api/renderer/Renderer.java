package me.siebe.flux.api.renderer;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.api.renderer.data.Renderable;
import me.siebe.flux.api.renderer.pipeline.RenderPipeline;

public class Renderer {
    private final RenderPipeline pipeline;
    private BaseRenderContext renderContext;

    public Renderer(RenderPipeline pipeline) {
        this.pipeline = pipeline;
        pipeline.init();
    }

    public void setRenderContext(BaseRenderContext renderContext) {
        this.renderContext = renderContext;
    }
    public BaseRenderContext getRenderContext() {
        return renderContext;
    }

    public RenderPipeline getPipeline() {
        return pipeline;
    }

    public void render() {
        if (renderContext == null) {
            throw RenderException.triedRenderingWithoutContext();
        }
        pipeline.render(renderContext);
    }

    public void destroy() {
        pipeline.destroy();
        renderContext.getRenderables().forEach(Renderable::destroy);
    }
}
