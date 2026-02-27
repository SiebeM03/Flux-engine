package me.siebe.flux.api.input.context;

import me.siebe.flux.api.input.Input;
import me.siebe.flux.api.input.actions.AnalogInputAction;
import me.siebe.flux.api.input.actions.DigitalInputAction;
import me.siebe.flux.api.input.actions.InputAction;
import me.siebe.flux.api.input.enums.InputType;

import java.util.HashMap;
import java.util.Map;

public class InputContext {
    private final String name;
    private final Map<String, Map<InputType, DigitalInputAction>> digitalActions = new HashMap<>();
    private final Map<String, Map<InputType, AnalogInputAction>> analogActions = new HashMap<>();

    private boolean enabled = true;
    private boolean consumeInput = false;

    public InputContext(String name) {
        this.name = name;
    }

    public void bind(String actionName, InputAction action) {
        if (action instanceof DigitalInputAction digitalAction) {
            Map<InputType, DigitalInputAction> map = digitalActions.computeIfAbsent(actionName, k -> new HashMap<>());
            map.put(action.getTargetDevice(), digitalAction);
        }
        if (action instanceof AnalogInputAction analogAction) {
            Map<InputType, AnalogInputAction> map = analogActions.computeIfAbsent(actionName, k -> new HashMap<>());
            map.put(analogAction.getTargetDevice(), analogAction);
        }
    }

    public DigitalInputAction getDigitalAction(String actionName) {
        return digitalActions.get(actionName).get(Input.activeDevice());
    }

    public AnalogInputAction getAnalogAction(String actionName) {
        Map<InputType, AnalogInputAction> map = analogActions.get(actionName);
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
