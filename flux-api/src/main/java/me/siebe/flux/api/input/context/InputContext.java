package me.siebe.flux.api.input.context;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.BooleanInputAction;
import me.siebe.flux.api.input.actions.FloatInputAction;
import me.siebe.flux.api.input.actions.InputAction;
import me.siebe.flux.api.input.enums.InputType;

import java.util.HashMap;
import java.util.Map;

public class InputContext {
    private final String name;
    private final Map<String, Map<InputType, BooleanInputAction>> digitalActions = new HashMap<>();
    private final Map<String, Map<InputType, FloatInputAction>> analogActions = new HashMap<>();

    private boolean enabled = true;
    private boolean consumeInput = false;

    public InputContext(String name) {
        this.name = name;
    }

    public void bind(String actionName, InputAction<?> action) {
        if (action instanceof BooleanInputAction digitalAction) {
            Map<InputType, BooleanInputAction> map = digitalActions.computeIfAbsent(actionName, k -> new HashMap<>());
            map.put(action.getTargetDevice(), digitalAction);
        }
        if (action instanceof FloatInputAction analogAction) {
            Map<InputType, FloatInputAction> map = analogActions.computeIfAbsent(actionName, k -> new HashMap<>());
            map.put(analogAction.getTargetDevice(), analogAction);
        }
    }

    BooleanInputAction getDigitalAction(String actionName) {
        return digitalActions.get(actionName).get(Input.activeDevice());
    }

    FloatInputAction getAnalogAction(String actionName) {
        Map<InputType, FloatInputAction> map = analogActions.get(actionName);
        if (map == null) return null;
        return map.get(Input.activeDevice());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean shouldConsumeInput() {
        return consumeInput;
    }

    public String getName() {
        return name;
    }
}
