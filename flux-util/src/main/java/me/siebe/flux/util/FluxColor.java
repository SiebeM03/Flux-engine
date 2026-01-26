package me.siebe.flux.util;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class FluxColor {
    public static final FluxColor WHITE = new FluxColor(1.0f, 1.0f, 1.0f);
    public static final FluxColor BLACK = new FluxColor(0.0f, 0.0f, 0.0f);
    public static final FluxColor RED = new FluxColor(1.0f, 0.0f, 0.0f);
    public static final FluxColor GREEN = new FluxColor(0.0f, 1.0f, 0.0f);
    public static final FluxColor BLUE = new FluxColor(0.0f, 0.0f, 1.0f);
    public static final FluxColor YELLOW = new FluxColor(1.0f, 1.0f, 0.0f);
    public static final FluxColor CYAN = new FluxColor(0.0f, 1.0f, 1.0f);
    public static final FluxColor MAGENTA = new FluxColor(1.0f, 0.0f, 1.0f);
    public static final FluxColor GRAY = new FluxColor(0.5f, 0.5f, 0.5f);
    public static final FluxColor DARK_GRAY = new FluxColor(0.25f, 0.25f, 0.25f);
    public static final FluxColor LIGHT_GRAY = new FluxColor(0.75f, 0.75f, 0.75f);
    public static final FluxColor PINK = new FluxColor(1.0f, 0.75f, 0.8f);
    public static final FluxColor ORANGE = new FluxColor(1.0f, 0.6f, 0.0f);
    public static final FluxColor PURPLE = new FluxColor(0.6f, 0.0f, 1.0f);
    public static final FluxColor BROWN = new FluxColor(0.6f, 0.4f, 0.2f);
    public static final FluxColor LIME = new FluxColor(0.8f, 1.0f, 0.0f);
    public static final FluxColor TEAL = new FluxColor(0.0f, 0.8f, 0.8f);
    public static final FluxColor NAVY = new FluxColor(0.0f, 0.0f, 0.5f);
    public static final FluxColor MAROON = new FluxColor(0.5f, 0.0f, 0.0f);
    public static final FluxColor OLIVE = new FluxColor(0.5f, 0.5f, 0.0f);


    private Vector3f rgb = new Vector3f();
    private float a = 1;

    private int rgba;
    private static final int R_OFFSET = 24;
    private static final int G_OFFSET = 16;
    private static final int B_OFFSET = 8;
    private static final int A_OFFSET = 0;
    private static final int VALUE_MASK = 0xFF;


    // =================================================================================================================
    // Constructors
    // =================================================================================================================
    public FluxColor(int r, int g, int b, int a) {
        set(r, g, b, a);
    }

    public FluxColor() {
        set(0, 0, 0, 255);
    }

    public FluxColor(int r, int g, int b) {
        set(r, g, b, 255);
    }

    @Deprecated
    public FluxColor(float r, float g, float b) {
        set(r, g, b, 1.0f);
    }

    @Deprecated
    public FluxColor(float r, float g, float b, float a) {
        set(r, g, b, a);
    }

    public FluxColor(String hex) {
        setR(Integer.valueOf(hex.substring(1, 3), 16));
        setG(Integer.valueOf(hex.substring(3, 5), 16));
        setB(Integer.valueOf(hex.substring(5, 7), 16));
    }

    public FluxColor(float[] colors) {
        if (colors.length < 3 || colors.length > 4) {
            throw new IllegalArgumentException("colors array requires 3 (RGB) or 4 (RGBA) values, got: " + colors.length);
        }

        setR(colors[0]);
        setG(colors[1]);
        setB(colors[2]);
        if (colors.length == 4) {
            setA(colors[3]);
        }
    }


    // =================================================================================================================
    // Cloning methods
    // =================================================================================================================
    public FluxColor(FluxColor color) {
        this.rgba = color.rgba;
    }

    public FluxColor copy() {
        return new FluxColor(this);
    }


    // =================================================================================================================
    // Setters
    // =================================================================================================================
    public void set(int r, int g, int b, int a) {
        setR(r);
        setG(g);
        setB(b);
        setA(a);
    }

    public void setR(int r) {
        ensureRange(r);
        this.rgba = (this.rgba & 0x00FFFFFF) | ((r & VALUE_MASK) << R_OFFSET);
    }

    public void setG(int g) {
        ensureRange(g);
        this.rgba = (this.rgba & 0xFF00FFFF) | ((g & VALUE_MASK) << G_OFFSET);
    }

    public void setB(int b) {
        ensureRange(b);
        this.rgba = (this.rgba & 0xFFFF00FF) | ((b & VALUE_MASK) << B_OFFSET);
    }

    public void setA(int a) {
        ensureRange(a);
        this.rgba = (this.rgba & 0xFFFFFF00) | ((a & VALUE_MASK) << A_OFFSET);
    }


    public void set(float r, float g, float b, float a) {
        setR(toInt(r));
        setG(toInt(g));
        setB(toInt(b));
        setA(toInt(a));
    }

    public void setR(float r) {
        setR(toInt(r));
    }

    public void setG(float g) {
        setG(toInt(g));
    }

    public void setB(float b) {
        setB(toInt(b));
    }

    public void setA(float a) {
        setA(toInt(a));
    }


    public void set(Vector4f rgba) {
        set(rgba.x, rgba.y, rgba.z, rgba.w);
    }

    public void set(Vector3f rgb) {
        set(rgb.x, rgb.y, rgb.z, 1.0f);
    }


    // =================================================================================================================
    // Getters
    // =================================================================================================================
    public int redInt() {
        return (rgba >> R_OFFSET) & VALUE_MASK;
    }

    public int greenInt() {
        return (rgba >> G_OFFSET) & VALUE_MASK;
    }

    public int blueInt() {
        return (rgba >> B_OFFSET) & VALUE_MASK;
    }

    public int alphaInt() {
        return (rgba >> A_OFFSET) & VALUE_MASK;
    }


    public float redFloat() {
        return redInt() / 255f;
    }

    public float greenFloat() {
        return greenInt() / 255f;
    }

    public float blueFloat() {
        return blueInt() / 255f;
    }

    public float alphaFloat() {
        return alphaInt() / 255f;
    }


    public Vector4f asVec4() {
        return new Vector4f(redFloat(), greenFloat(), blueFloat(), alphaFloat());
    }

    public Vector3f asVec3() {
        return new Vector3f(redFloat(), greenFloat(), blueFloat());
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FluxColor c) {
            return (rgb.x == c.rgb.x && rgb.y == c.rgb.y && rgb.z == c.rgb.z && a == c.a);
        }
        return false;
    }

    private void ensureRange(int... values) throws IllegalArgumentException {
        for (int value : values) {
            if (value < 0) throw new IllegalArgumentException("RGBA values cannot be less than 0");
            if (value > 255) throw new IllegalArgumentException("RGBA values cannot be greater than 255");
        }
    }

    private int toInt(float v) {
        return (int) (v * 255);
    }
}
