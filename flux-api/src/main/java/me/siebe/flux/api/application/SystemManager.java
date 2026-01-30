package me.siebe.flux.api.application;

import me.siebe.flux.util.exceptions.ApplicationException;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.HashMap;
import java.util.Map;

public class SystemManager {
    private static final Logger logger = LoggerFactory.getLogger(SystemManager.class, LoggingCategories.APPLICATION);

    private final Map<Class<? extends EngineSystem>, EngineSystem> engineSystems = new HashMap<>();

    public void init() {
        for (EngineSystem engineSystem : engineSystems.values()) {
            engineSystem.init();
        }
    }

    public void update() {
        for (EngineSystem engineSystem : engineSystems.values()) {
            engineSystem.update();
        }
    }

    public void destroy() {
        for (EngineSystem engineSystem : engineSystems.values()) {
            engineSystem.destroy();
        }
    }

    public void registerEngineSystem(EngineSystem engineSystem) {
        if (engineSystems.containsKey(engineSystem.getClass())) {
            throw ApplicationException.engineSystemAlreadyRegistered(engineSystem.getClass());
        }
        engineSystems.put(engineSystem.getClass(), engineSystem);
        logger.info("Engine System Registered: " + engineSystem.getClass().getName());
    }

    public void unregisterEngineSystem(Class<? extends EngineSystem> clazz) {
        engineSystems.remove(clazz);
        logger.info("Engine System Unregistered: " + clazz.getName());
    }
}
