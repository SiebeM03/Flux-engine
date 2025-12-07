package me.siebe.flux.api.renderer.pipeline;

import me.siebe.flux.api.renderer.context.BaseRenderContext;

public interface RenderStep {
    default void init() {}

    default void prepare(BaseRenderContext context) {}

    void execute(BaseRenderContext context);

    default void finish(BaseRenderContext context) {}

    default void destroy() {}
}
