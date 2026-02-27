package me.siebe.flux.api.input.context;

import me.siebe.flux.api.input.actions.BooleanInputAction;
import me.siebe.flux.api.input.actions.FloatInputAction;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.ArrayDeque;
import java.util.Deque;

public class InputManager {
    private static final Logger logger = LoggerFactory.getLogger(InputManager.class, LoggingCategories.INPUT);

    private final Deque<InputContext> contextStack = new ArrayDeque<>();

    public void pushContext(InputContext context) {
        contextStack.push(context);
    }

    public void popContext() {
        contextStack.pop();
    }

    public void removeContext(String name) {
        contextStack.removeIf(c -> c.getName().equals(name));
    }

    public InputContext peekContext() {
        return contextStack.peek();
    }

    public boolean isActionActive(String actionName) {
        for (InputContext context : contextStack) {
            if (!context.isEnabled()) continue;

            BooleanInputAction action = context.getDigitalAction(actionName);
            if (action == null) {
                if (context.shouldConsumeInput()) return false;
                continue;
            }

            if (action.getValue()) return true;
            if (context.shouldConsumeInput()) return false;
        }
        return false;
    }

    public float getActionValue(String actionName) {
        for (InputContext context : contextStack) {
            if (!context.isEnabled()) continue;

            FloatInputAction action = context.getAnalogAction(actionName);
            if (action == null) {
                if (context.shouldConsumeInput()) return 0.0f;
                continue;
            }

            return action.getValue();
        }
        return 0.0f;
    }

//    public Vector2f getActionDelta(String actionName) {
//        return getActionAxis(actionName, MouseMoveAction.class);
//    }
//
//    public Vector2f getActionScroll(String actionName) {
//        return getActionAxis(actionName, MouseScrollAction.class);
//    }
//
//    private Vector2f getActionAxis(String actionName, Class<? extends InputAction> actionType) {
//        for (InputContext context : contextStack) {
//            if (!context.isEnabled()) continue;
//
//            InputAction action = context.getDigitalAction(actionName);
//            if (action == null) {
//                if (context.shouldConsumeInput()) return new Vector2f();
//                continue;
//            }
//
//            if (actionType.isAssignableFrom(action.getClass())) return action.getValue();
//            if (context.shouldConsumeInput()) return new Vector2f();
//        }
//        return new Vector2f();
//    }
}
