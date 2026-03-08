package me.siebe.flux.test.junit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;

public class OpenGLResetExtension implements AfterEachCallback {
    private static Class<?> OpenGLStateClass;

    static {
        try {
            OpenGLStateClass = Class.forName("me.siebe.flux.opengl.OpenGLState");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        try {
            Field field = OpenGLStateClass.getDeclaredField("initialized");
            field.setAccessible(true);
            field.set(null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
