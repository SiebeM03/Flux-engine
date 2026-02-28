package game.core.demos.input.camera_controller;

import game.core.demos.Demo;
import me.siebe.flux.api.camera.PerspectiveCamera;
import me.siebe.flux.api.input.Input;
import me.siebe.flux.core.AppContext;
import me.siebe.flux.util.ValueUtils;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static game.core.demos.input.contexts.GameContext.*;
import static game.core.demos.input.contexts.InputContexts.GAME_CONTEXT;

/**
 * A demo implementation showcasing a free-look 3D camera controller using input action bindings.
 *
 * <p>
 * This demo creates and manages a {@link PerspectiveCamera} that can:
 * </p>
 * <ul>
 *     <li>Move forward/backward, left/right, and up/down using input actions.</li>
 *     <li>Rotate based on mouse movement (yaw and pitch).</li>
 *     <li>Simulate a typical first-person camera system.</li>
 * </ul>
 *
 * <h2>Controls</h2>
 * <ul>
 *     <li><b>Movement:</b> Controlled via the {@code MOVE_FORWARD}, {@code MOVE_RIGHT}, and {@code MOVE_UP} input actions.</li>
 *     <li><b>Look:</b> Controlled via {@code LOOK_HORIZONTAL} and {@code LOOK_VERTICAL} input actions.</li>
 * </ul>
 *
 * <h2>Behavior</h2>
 * <ul>
 *     <li>The camera movement speed is frame-rate independent and scaled by delta time.</li>
 *     <li>Mouse sensitivity can be configured via {@code mouseSensitivity}.</li>
 *     <li>Pitch rotation is clamped to prevent gimbal lock (limited to -89° to 89°).</li>
 *     <li>The cursor is hidden and captured when the demo initializes.</li>
 * </ul>
 *
 * <h2>Technical Details</h2>
 * <p>
 * The direction vector is recalculated each frame using spherical coordinates
 * derived from yaw and pitch angles. The camera is then updated by setting
 * its new position and calling {@link PerspectiveCamera#lookAt(org.joml.Vector3f)}
 * followed by {@link PerspectiveCamera#update()}.
 * </p>
 *
 * <p>
 * This class is intended as a minimal example of integrating input handling with camera movement in a 3D rendering context.
 * It pushes and uses input actions from {@link game.core.demos.input.contexts.InputContexts#GAME_CONTEXT GAME_CONTEXT}
 * </p>
 *
 * @see PerspectiveCamera
 */
public class CameraControllerDemo implements Demo {
    private PerspectiveCamera camera;
    private float cameraSpeed = 5.0f;
    private float mouseSensitivity = 0.05f;
    private float yaw = -90.0f;
    private float pitch = 0.0f;

    @Override
    public void init() {
        AppContext.withContextNoReturn(ctx -> {
            camera = new PerspectiveCamera(ctx.getWindow().getAspectRatio(), 0.1f, 1000.f);
            camera.setPosition(new Vector3f(0.0f, 0.0f, 3.0f));
            ctx.getRenderer().getRenderContext().setCamera(camera);

            // Hide cursor and capture it
            GLFW.glfwSetInputMode(ctx.getWindow().getId(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        });

        Input.manager().pushContext(GAME_CONTEXT);
    }

    @Override
    public void update() {
        AppContext ctx = AppContext.get();

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
    @Override
    public void destroy() {

    }
}
