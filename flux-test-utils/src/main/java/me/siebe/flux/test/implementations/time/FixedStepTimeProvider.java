package me.siebe.flux.test.implementations.time;

import me.siebe.flux.util.time.TimeProvider;

public final class FixedStepTimeProvider implements TimeProvider {
    private final double stepSeconds;
    private double currentSeconds;

    public FixedStepTimeProvider(double stepSeconds) {
        this(stepSeconds, 0.0);
    }

    public FixedStepTimeProvider(double stepSeconds, double startSeconds) {
        if (stepSeconds <= 0) {
            throw new IllegalArgumentException("stepSeconds must be positive");
        }
        this.stepSeconds = stepSeconds;
        this.currentSeconds = startSeconds;
    }

    /**
     * Advances time by one step. Call once per simulated frame.
     */
    public void tick() {
        currentSeconds += stepSeconds;
    }

    @Override
    public double getTimeSeconds() {
        return currentSeconds;
    }

    public double getStepSeconds() {
        return stepSeconds;
    }
}
