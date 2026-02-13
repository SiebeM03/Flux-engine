package game.core.temp;

import me.siebe.flux.api.input.InputController;
import me.siebe.flux.api.input.enums.Key;

public class CameraMoveController extends InputController {
    public boolean isMoveForwardHeld() {
        return isKeyboardEnabled && keyboard.isKeyDown(Key.KEY_W);
    }
    public boolean isMoveBackwardHeld() {
        return isKeyboardEnabled && keyboard.isKeyDown(Key.KEY_S);
    }
    public boolean isMoveLeftHeld() {
        return isKeyboardEnabled && keyboard.isKeyDown(Key.KEY_A);
    }
    public boolean isMoveRightHeld() {
        return isKeyboardEnabled && keyboard.isKeyDown(Key.KEY_D);
    }
    public boolean isMoveUpHeld() {
        return isKeyboardEnabled && keyboard.isKeyDown(Key.KEY_SPACE);
    }
    public boolean isMoveDownHeld() {
        return isKeyboardEnabled && keyboard.isKeyDown(Key.KEY_LEFT_SHIFT);
    }
}
