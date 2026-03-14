package me.siebe;

import me.siebe.flux.api.window.Window;
import me.siebe.flux.api.window.WindowPlatform;
import me.siebe.flux.opengl.OpenGLState;
import me.siebe.render.DefaultStep;

import static org.lwjgl.opengl.GL11.*;

public class Main {
    private static Window window;

    public static void main(String[] args) {
        window = Window.builder(WindowPlatform.GLFW).build();
        window.init();

        DefaultStep defaultStep = new DefaultStep();

//        dataHandler.pushVertexData(new float[]{
//                // Bottom left quad
//                0, 0,   0,0,1,
//                0, -1,  0,0,1,
//                -1, -1, 0,0,1,
//                -1, 0,  0,0,1,
//
//                // Top right quad
//                1, 1,   0,0,0,
//                1, 0,   0,0,0,
//                0, 0,   0,0,0,
//                0, 1,   0,0,0,
//        });

        glClearColor(0, 0, 0, 1);
        OpenGLState.disableCullFace();

        while (!window.shouldClose()) {
            defaultStep.execute(null);

            window.update();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        window.destroy();
    }
}