package game.core.temp;

import me.siebe.flux.api.camera.PerspectiveCamera;
import me.siebe.flux.api.application.AppContext;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 * This is a temporary camera setup to simplify debugging of the render systems
 */
public class TempCameraSetup {

    private PerspectiveCamera camera;
    private float cameraSpeed = 5.0f;
    private float mouseSensitivity = 0.1f;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean firstMouse = true;
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
        long windowId = ctx.getWindow().getId();
        float deltaTime = (float) ctx.getTimer().getDeltaTime();
        float moveSpeed = cameraSpeed * deltaTime;

        // Mouse input for camera rotation (must be done first to update direction)
        double[] mouseX = new double[1];
        double[] mouseY = new double[1];
        GLFW.glfwGetCursorPos(windowId, mouseX, mouseY);

        if (firstMouse) {
            lastMouseX = mouseX[0];
            lastMouseY = mouseY[0];
            firstMouse = false;
        }

        float xOffset = (float) (mouseX[0] - lastMouseX) * mouseSensitivity;
        float yOffset = (float) (lastMouseY - mouseY[0]) * mouseSensitivity;

        lastMouseX = mouseX[0];
        lastMouseY = mouseY[0];

        yaw += xOffset;
        pitch += yOffset;

        // Constrain pitch
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        // Calculate new direction vector
        Vector3f newDirection = new Vector3f();
        newDirection.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        newDirection.y = (float) Math.sin(Math.toRadians(pitch));
        newDirection.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        newDirection.normalize();

        // Update camera direction by setting target
        Vector3f position = camera.getPosition();
        Vector3f target = new Vector3f(position).add(newDirection);
        camera.lookAt(target);

        // Keyboard input for camera movement
        Vector3f direction = camera.getDirection();
        Vector3f right = new Vector3f(direction).cross(camera.getUp()).normalize();
        Vector3f up = new Vector3f(camera.getUp());
        Vector3f newPosition = new Vector3f(position);

        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            newPosition.add(new Vector3f(direction).mul(moveSpeed));
        }
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            newPosition.sub(new Vector3f(direction).mul(moveSpeed));
        }
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            newPosition.sub(new Vector3f(right).mul(moveSpeed));
        }
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            newPosition.add(new Vector3f(right).mul(moveSpeed));
        }
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            newPosition.add(new Vector3f(up).mul(moveSpeed));
        }
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            newPosition.sub(new Vector3f(up).mul(moveSpeed));
        }

        camera.setPosition(newPosition);

        // Update target after position change to maintain direction
        Vector3f updatedPosition = camera.getPosition();
        camera.lookAt(new Vector3f(updatedPosition).add(newDirection));
        camera.update();
    }
}
