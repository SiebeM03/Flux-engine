package me.siebe.flux.api.renderer.pipeline;

import me.siebe.flux.api.renderer.context.BaseRenderContext;
import me.siebe.flux.util.system.ProvidableSystem;
import me.siebe.flux.util.system.SystemProvider;
import me.siebe.flux.util.system.SystemProviderType;

public interface RenderPipeline extends ProvidableSystem {
    static RenderPipeline create() {
        return SystemProvider.provide(RenderPipeline.class, SystemProviderType.ALL);
    }

    void addStep(RenderStep step);

    void addStep(RenderStep step, int index);

    void removeStep(RenderStep step);

    void init();

    void render(BaseRenderContext context);

    void destroy();
}
