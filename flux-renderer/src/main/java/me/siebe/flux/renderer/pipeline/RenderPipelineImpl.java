package me.siebe.flux.renderer.pipeline;

import me.siebe.flux.api.renderer.RenderContext;
import me.siebe.flux.api.renderer.RenderPipeline;
import me.siebe.flux.api.renderer.RenderStep;
import me.siebe.flux.lwjgl.opengl.OpenGLState;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.ArrayList;
import java.util.List;

public class RenderPipelineImpl implements RenderPipeline {
    private static final Logger logger = LoggerFactory.getLogger(RenderPipelineImpl.class, LoggingCategories.RENDERER);

    private final List<RenderStep> steps;
    private boolean initialized = false;

    public RenderPipelineImpl() {
        this.steps = new ArrayList<>();
    }

    @Override
    public void render(RenderContext context) {
        if (!initialized) {
            logger.warn("Pipeline is not initialized. Call init() before rendering.");
            return;
        }

        for (RenderStep step : steps) {
            try {
                step.execute(context);
            } catch (Exception e) {
                logger.error("Error executing render step '{}': {}", step.getName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public RenderPipeline addStep(RenderStep step) {
        if (step == null) {
            throw new IllegalArgumentException("Render step cannot be null");
        }
        steps.add(step);
        logger.debug("Added render step '{}' to pipeline", step.getName());
        return this;
    }

    @Override
    public RenderPipeline addStep(int index, RenderStep step) {
        if (step == null) {
            throw new IllegalArgumentException("Render step cannot be null");
        }
        steps.add(index, step);
        logger.debug("Added render step '{}' at index {}", step.getName(), index);
        return this;
    }

    @Override
    public RenderPipeline removeStep(RenderStep step) {
        if (steps.remove(step)) {
            logger.debug("Removed render step '{}' from pipeline", step.getName());
        }
        return this;
    }

    @Override
    public RenderPipeline removeStep(int index) {
        if (index < 0 || index >= steps.size()) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for pipeline with " + steps.size() + " steps");
        }
        RenderStep removed = steps.remove(index);
        logger.debug("Removed render step '{}' at index {}", removed.getName(), index);
        return this;
    }

    @Override
    public List<RenderStep> getSteps() {
        return List.copyOf(steps);
    }

    @Override
    public void init(RenderContext context) {
        logger.info("Initializing render pipeline with {} steps", steps.size());
        for (RenderStep step : steps) {
            try {
                step.init(context);
                logger.debug("Initialized render step '{}'", step.getName());
            } catch (Exception e) {
                logger.error("Error initializing render step '{}': {}", step.getName(), e.getMessage(), e);
            }
        }

        OpenGLState.enableDepthTest();
        initialized = true;
    }

    @Override
    public void destroy() {
        logger.info("Destroying render pipeline");
        for (RenderStep step : steps) {
            try {
                step.destroy();
                logger.debug("Destroyed render step '{}'", step.getName());
            } catch (Exception e) {
                logger.error("Error destroying render step '{}': {}", step.getName(), e.getMessage(), e);
            }
        }
        steps.clear();
        initialized = false;
    }
}
