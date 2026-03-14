package me.siebe.render;

import me.siebe.flux.api.renderer.pipeline.RenderStep;
import me.siebe.flux.opengl.texture.Texture;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public abstract class BatchedRenderStep implements RenderStep {
    protected List<RenderBatch> batches = new ArrayList<>();

    protected abstract RenderBatch createBatch();

    protected RenderBatch getAvailableBatch(Texture texture) {
        for (RenderBatch batch : batches) {
            if (texture == null || batch.hasTexture(texture) || batch.hasTextureRoom()) {
                return batch;
            }
        }

        RenderBatch batch = createBatch();
        batches.add(batch);
        return batch;
    }

    protected void renderBatches() {
        for (RenderBatch batch : batches) {
            batch.bind();
            batch.submitBuffer();

            glDrawElements(GL_TRIANGLES, batch.getVertexArray().getIndexBuffer().getCount(), GL_UNSIGNED_INT, 0);

            batch.unbind();
        }
    }
}
