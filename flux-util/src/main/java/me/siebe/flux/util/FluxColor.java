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

    public FluxColor() {
    }

    public FluxColor(FluxColor color) {
        this.rgb.set(color.rgb);
        this.a = color.a;
    }

    public FluxColor(Vector3f rgb) {
        this.rgb.set(rgb);
    }

    public FluxColor(Vector3f rgb, float alpha) {
        this.rgb.set(rgb);
        this.a = alpha;
    }

    public FluxColor(float r, float g, float b) {
        this.rgb.set(r, g, b);
    }

    public FluxColor(float r, float g, float b, float a) {
        rgb.set(r, g, b);
        this.a = a;
    }

    public FluxColor(String hex) {
        this.rgb = new Vector3f(
                Integer.valueOf(hex.substring(1, 3), 16) / 255f,
                Integer.valueOf(hex.substring(3, 5), 16) / 255f,
                Integer.valueOf(hex.substring(5, 7), 16) / 255f
        );
    }

    public FluxColor copy() {
        return new FluxColor(this);
    }

    public void set(float r, float g, float b, float a) {
        this.rgb.set(r, g, b);
        this.a = a;
    }

    public void set(Vector4f rgba) {
        this.rgb.set(rgba.x, rgba.y, rgba.z);
        this.a = rgba.w;
    }

    public void setR(float r) {
        this.rgb.x = r;
    }

    public void setG(float g) {
        this.rgb.y = g;
    }

    public void setB(float b) {
        this.rgb.z = b;
    }

    public void setAlpha(float a) {
        this.a = a;
    }


    public Vector4f toVec4() {
        return new Vector4f(getR(), getG(), getB(), getA());
    }

    public Vector3f toVec3() {
        return new Vector3f(getR(), getG(), getB());
    }


    public float getR() {
        return this.rgb.x;
    }

    public float getG() {
        return this.rgb.y;
    }

    public float getB() {
        return this.rgb.z;
    }

    public float getA() {
        return this.a;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FluxColor c) {
            return (rgb.x == c.rgb.x && rgb.y == c.rgb.y && rgb.z == c.rgb.z && a == c.a);
        }
        return false;
    }
}
