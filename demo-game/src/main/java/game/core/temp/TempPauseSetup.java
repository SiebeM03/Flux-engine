package game.core.temp;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.lwjgl.opengl.OpenGLState;
import me.siebe.flux.renderer3d.steps.ClearStep;
import me.siebe.flux.util.ValueUtils;

import static game.core.temp.input.InputContexts.PAUSE_CONTEXT;
import static game.core.temp.input.PauseContext.CHANGE_GREEN;
import static game.core.temp.input.PauseContext.UNPAUSE;
import static org.lwjgl.glfw.GLFW.*;

public class TempPauseSetup {
    private boolean paused = false;

    public void init() {
        OpenGLState.setClearColor(ClearStep.uiColor);
    }

    private float green = 0.0f;
    private float blue = 0.0f;
    public void update() {
        if (!paused) return;

        if (Input.manager().isActionActive(UNPAUSE)) unpause();

        green += (Input.manager().getActionValue(CHANGE_GREEN) / AppContext.get().getWindow().getWidth());
        green = ValueUtils.clampedValue(green, 0.0f, 1.0f);
        ClearStep.uiColor.setG(green);

//        blue += (Input.manager().getActionScroll(CHANGE_BLUE).y * 0.1f);
//        blue = ValueUtils.clampedValue(blue, 0.0f, 1.0f);
//        ClearStep.uiColor.setB(blue);

        OpenGLState.setClearColor(ClearStep.uiColor);
    }

    public void pause() {
        if (paused) return;

        System.out.println("PAUSE");
        paused = true;
        Input.manager().pushContext(PAUSE_CONTEXT);
        glfwSetInputMode(AppContext.get().getWindow().getId(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void unpause() {
        if (!paused) return;

        System.out.println("UNPAUSE");
        paused = false;
        Input.manager().popContext();
        glfwSetInputMode(AppContext.get().getWindow().getId(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }
}
