package me.siebe.flux.lwjgl.glfw.input;

import me.siebe.flux.api.camera.Camera;
import me.siebe.flux.api.event.Event;
import me.siebe.flux.api.event.traits.Pooled;
import me.siebe.flux.api.window.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MousePosEvent extends Event implements Pooled {
    private Window window;
    private double x;
    private double y;

    public double getScreenX() {
        return x;
    }

    public double getScreenY() {
        return y;
    }

    public float getNdcX() {
        return (float) (2.0 * getScreenX() / window.getWidth()) - 1.0f;
    }

    public float getNdcY() {
        return (float) (2.0 * getScreenY() / window.getHeight()) - 1.0f;
    }

    public Vector3f getWorldCoordinates(final Camera camera) {
        float ndcX = getNdcX();
        float ndcY = getNdcY();

        // Create a point in clip space (NDC with z = -1 for near plane, z = 1 for far plane)
        // We use z = -1 to get a point on the near plane
        Vector4f clipCoords = new Vector4f(ndcX, ndcY, -1.0f, 1.0f);

        // Get the view-projection matrix and invert it
        // Note: The view-projection matrix is projection * view, so the inverse is invView * invProjection
        Matrix4f viewProjection = camera.getViewProjectionMatrix();
        Matrix4f invViewProjection = new Matrix4f(viewProjection).invert();

        // Transform from clip space to world space
        Vector4f worldCoords = new Vector4f();
        invViewProjection.transform(clipCoords, worldCoords);

        // Perspective divide (if w != 0)
        // After unprojection, we need to divide by w to get the final world coordinates
        if (Math.abs(worldCoords.w) > 1e-6f) {
            worldCoords.div(worldCoords.w);
        }

        return new Vector3f(worldCoords.x, worldCoords.y, worldCoords.z);
    }


    public void set(Window window, double x, double y) {
        this.window = window;
        this.x = x;
        this.y = y;
    }

    @Override
    public void reset() {
        this.x = -1.0;
        this.y = -1.0;
    }

}
