package me.siebe.flux.api.ui.impl;

import me.siebe.flux.api.event.common.FramebufferResizeEvent;
import me.siebe.flux.api.ui.api.Ui;
import me.siebe.flux.api.ui.api.UiComponent;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.util.DirtyValue;
import me.siebe.flux.util.FluxColor;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class UiContainer extends UiComponent {

    private final DirtyValue<Matrix4f> uiProjectionMatrix;

    public UiContainer() {
        super(
                "UI_CONTAINER",
                new Vector2f(0, 0),
                new Vector2f(AppContext.get().getWindow().getWidth(), AppContext.get().getWindow().getHeight()),
                new FluxColor(FluxColor.TRANSPARENT)
        );

        uiProjectionMatrix = new DirtyValue<>(new Matrix4f(), this::updateProjection);

        AppContext.get().getEventBus().getListenerRegistry().register(FramebufferResizeEvent.class, e -> {
            uiProjectionMatrix.markDirty();
            this.size.set(e.getNewWidth(), e.getNewHeight());
        });

        this.children.add(new UiComponent(
                "UI_CONTAINER",
                new Vector2f(0, 0),
                new Vector2f(100, 100),
                new FluxColor(FluxColor.RED)) {
            @Override
            protected void updateSelf() {

            }
        });

    }

    private void updateProjection(Matrix4f m) {
        AppContext.withContextNoReturn(ctx -> {
            m.identity().ortho(
                    0, ctx.getWindow().getWidth(),
                    0, ctx.getWindow().getHeight(),
                    -1, 1
            );
        });
    }

    @Override
    protected void updateSelf() {

    }

    public Matrix4f getProjectionMatrix() {
        return uiProjectionMatrix.get();
    }
}
