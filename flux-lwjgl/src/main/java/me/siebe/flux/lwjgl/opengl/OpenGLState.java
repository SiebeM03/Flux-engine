package me.siebe.flux.lwjgl.opengl;

import me.siebe.flux.util.FluxColor;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class OpenGLState {
    private static boolean initialized = false;

    private OpenGLState() {
        // Utility class
    }

    public static void init() {
        if (initialized) {
            throw new IllegalStateException("OpenGLState already initialized");
        }
        initialized = true;
        createCapabilities();
    }

    /**
     * Enables depth testing.
     */
    public static void enableDepthTest() {
        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Disables depth testing.
     */
    public static void disableDepthTest() {
        glDisable(GL_DEPTH_TEST);
    }

    /**
     * Sets the depth function.
     *
     * @param func the depth function (e.g., GL_LESS, GL_LEQUAL)
     */
    public static void setDepthFunc(int func) {
        glDepthFunc(func);
    }

    /**
     * Enables blending.
     */
    public static void enableBlend() {
        glEnable(GL_BLEND);
    }

    /**
     * Disables blending.
     */
    public static void disableBlend() {
        glDisable(GL_BLEND);
    }

    /**
     * Sets the blending function.
     *
     * @param sfactor the source factor (e.g., GL_SRC_ALPHA)
     * @param dfactor the destination factor (e.g., GL_ONE_MINUS_SRC_ALPHA)
     */
    public static void setBlendFunc(int sfactor, int dfactor) {
        glBlendFunc(sfactor, dfactor);
    }

    /**
     * Sets the blending function for alpha blending.
     * <p>
     * Convenience method that sets GL_SRC_ALPHA and GL_ONE_MINUS_SRC_ALPHA.
     */
    public static void setBlendFuncAlpha() {
        setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Enables face culling.
     */
    public static void enableCullFace() {
        glEnable(GL_CULL_FACE);
    }

    /**
     * Disables face culling.
     */
    public static void disableCullFace() {
        glDisable(GL_CULL_FACE);
    }

    /**
     * Sets which faces to cull.
     *
     * @param mode the cull mode (e.g., GL_BACK, GL_FRONT, GL_FRONT_AND_BACK)
     */
    public static void setCullFace(int mode) {
        glCullFace(mode);
    }

    /**
     * Sets the front face winding order.
     *
     * @param mode the winding order (e.g., GL_CCW, GL_CW)
     */
    public static void setFrontFace(int mode) {
        glFrontFace(mode);
    }

    /**
     * Sets the viewport.
     *
     * @param x      the lower left corner x coordinate
     * @param y      the lower left corner y coordinate
     * @param width  the viewport width
     * @param height the viewport height
     */
    public static void setViewport(int x, int y, int width, int height) {
        glViewport(x, y, width, height);
    }

    /**
     * Sets the clear color from a FluxColor.
     *
     * @param color the color
     */
    public static void setClearColor(FluxColor color) {
        glClearColor(color.getR(), color.getG(), color.getB(), color.getA());
    }

    /**
     * Clears the specified buffers.
     *
     * @param mask the bitwise OR of masks indicating which buffers to clear
     *             (e.g., GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
     */
    public static void clear(int mask) {
        glClear(mask);
    }

    /**
     * Clears the color and depth buffers.
     * <p>
     * Convenience method that clears both GL_COLOR_BUFFER_BIT and GL_DEPTH_BUFFER_BIT.
     */
    public static void clearColorAndDepth() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Activates a texture unit.
     *
     * @param textureUnit the texture unit index (0-31)
     */
    public static void activateTexture(int textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
    }

    /**
     * Enables wireframe mode.
     */
    public static void enableWireframe() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    }

    /**
     * Disables wireframe mode (enables fill mode).
     */
    public static void disableWireframe() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    /**
     * Sets the polygon mode.
     *
     * @param face the face to apply the mode to (e.g., GL_FRONT_AND_BACK)
     * @param mode the polygon mode (e.g., GL_FILL, GL_LINE, GL_POINT)
     */
    public static void setPolygonMode(int face, int mode) {
        glPolygonMode(face, mode);
    }
}
