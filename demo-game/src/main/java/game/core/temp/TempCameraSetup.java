package game.core.temp;

import me.siebe.flux.api.camera.PerspectiveCamera;
import me.siebe.flux.api.input.Input;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.util.ValueUtils;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static game.core.temp.input.GameContext.*;

/**
 * This is a temporary camera setup to simplify debugging of the render systems
 */
public class TempCameraSetup {

    private PerspectiveCamera camera;
    private float cameraSpeed = 5.0f;
    private float mouseSensitivity = 0.05f;
    private float yaw = -90.0f;
    private float pitch = 0.0f;

    public void init() {
        AppContext.withContextNoReturn(ctx -> {
            camera = new PerspectiveCamera(ctx.getWindow().getAspectRatio(), 0.1f, 1000.f);
            camera.setPosition(new Vector3f(0.0f, 0.0f, 3.0f));
            ctx.getRenderer().getRenderContext().setCamera(camera);

            // Hide cursor and capture it
            GLFW.glfwSetInputMode(ctx.getWindow().getId(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        });
    }

    public void update(final AppContext ctx) {
        float deltaTime = (float) ctx.getTimer().getDeltaTime();
        float moveSpeed = cameraSpeed * deltaTime;

        // Keyboard input for camera movement
        Vector3f direction = camera.getDirection();
        Vector3f right = new Vector3f(direction).cross(camera.getUp()).normalize();
        Vector3f up = new Vector3f(camera.getUp());
        Vector3f newPosition = new Vector3f(camera.getPosition());

        float inputMoveForward = Input.manager().getActionValue(MOVE_FORWARD);
        float inputMoveRight = Input.manager().getActionValue(MOVE_RIGHT);
        float inputMoveUp = Input.manager().getActionValue(MOVE_UP);

        newPosition.add(new Vector3f(direction).mul(inputMoveForward).mul(moveSpeed));
        newPosition.add(new Vector3f(right).mul(inputMoveRight).mul(moveSpeed));
        newPosition.add(new Vector3f(up).mul(inputMoveUp).mul(moveSpeed));

        // Mouse input for camera movement
        float xOffset = Input.manager().getActionValue(LOOK_HORIZONTAL) * mouseSensitivity;
        float yOffset = Input.manager().getActionValue(LOOK_VERTICAL) * mouseSensitivity;

        yaw += xOffset;
        pitch += yOffset;
        pitch = ValueUtils.clampedValue(pitch, -89.0f, 89.0f);

        // Calculate new direction vector
        Vector3f newDirection = new Vector3f();
        newDirection.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        newDirection.y = (float) Math.sin(Math.toRadians(pitch));
        newDirection.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        newDirection.normalize();

        // Commit new values to camera
        camera.setPosition(newPosition);
        Vector3f target = new Vector3f(newPosition).add(newDirection);
        camera.lookAt(target);
        camera.update();
    }
}
