package game.core.demos.input.pause_actions;

import game.core.demos.Demo;
import me.siebe.flux.api.input.Input;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.opengl.OpenGLState;
import me.siebe.flux.renderer3d.steps.ClearStep;
import me.siebe.flux.util.ValueUtils;

import static game.core.demos.input.contexts.InputContexts.PAUSE_CONTEXT;
import static game.core.demos.input.contexts.PauseContext.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Demo showcasing pause handling using an input context stack. It is meant to showcase how input contexts are
 * supposed to be managed.
 *
 * <p>
 * This demo automatically pauses and unpauses after a certain number of frames
 * and demonstrates how a dedicated pause input context can:
 * </p>
 * <ul>
 *     <li>Consume input while paused</li>
 *     <li>Modify UI clear color values using input actions</li>
 *     <li>Restore gameplay input when unpaused</li>
 * </ul>
 *
 * <p>
 * While paused:
 * </p>
 * <ul>
 *     <li>{@link game.core.demos.input.contexts.PauseContext#UNPAUSE UNPAUSE action} resumes the game.</li>
 *     <li>{@link game.core.demos.input.contexts.PauseContext#CHANGE_GREEN CHANGE_GREEN action} adjusts the green clear color component.</li>
 *     <li>{@link game.core.demos.input.contexts.PauseContext#CHANGE_BLUE CHANGE_BLUE action} adjusts the blue clear color component.</li>
 * </ul>
 */
public class PauseActionsDemo implements Demo {

    private boolean paused = false;

    private float green = 0.0f;
    private float blue = 0.0f;

    @Override
    public void init() {
        OpenGLState.setClearColor(ClearStep.uiColor);
    }

    @Override
    public void update() {
        // Automatic pause/unpause demo triggers
        long frameCount = AppContext.get().getTimer().getFrameCount();

        if (frameCount == 15000) {
            pause();
        }
        if (frameCount == 45000) {
            unpause();
        }

        if (!paused) return;

        // Handle unpause action
        if (Input.manager().isActionActive(UNPAUSE)) {
            unpause();
            return;
        }

        // Adjust green component via axis input
        green += (Input.manager().getActionValue(CHANGE_GREEN)
                / AppContext.get().getWindow().getWidth());
        green = ValueUtils.clampedValue(green, 0.0f, 1.0f);
        ClearStep.uiColor.setG(green);

        // Adjust blue component via axis input
        blue += (Input.manager().getActionValue(CHANGE_BLUE) * 0.0005f);
        blue = ValueUtils.clampedValue(blue, 0.0f, 1.0f);
        ClearStep.uiColor.setB(blue);

        OpenGLState.setClearColor(ClearStep.uiColor);
    }

    @Override
    public void destroy() {
        // No cleanup required
    }

    /**
     * Activates the pause state and pushes the pause input context.
     */
    public void pause() {
        if (paused) return;

        System.out.println("PAUSE");
        paused = true;

        Input.manager().pushContext(PAUSE_CONTEXT);
        glfwSetInputMode(
                AppContext.get().getWindow().getId(),
                GLFW_CURSOR,
                GLFW_CURSOR_NORMAL
        );
    }

    /**
     * Deactivates the pause state and restores the previous input context.
     */
    public void unpause() {
        if (!paused) return;

        System.out.println("UNPAUSE");
        paused = false;

        Input.manager().popContext();
        glfwSetInputMode(
                AppContext.get().getWindow().getId(),
                GLFW_CURSOR,
                GLFW_CURSOR_DISABLED
        );
    }
}