package me.siebe.flux.util.time;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.time.Duration;

public class Delay {
    private static final Logger logger = LoggerFactory.getLogger(Delay.class, "timer");
    private final Duration duration;
    private Duration timePassed = Duration.ZERO;
    private boolean paused = false;

    public Delay(final Duration duration) {
        this.duration = duration;
    }

    /**
     * Returns true if the delay has completed
     */
    public boolean isOver() {
        return !timeRemaining().isPositive() || timeRemaining().isZero();
    }

    /**
     * Returns the remaining time as a Duration
     */
    public Duration timeRemaining() {
        return duration.minus(timePassed).isNegative() ? Duration.ZERO : duration.minus(timePassed);
    }

    /**
     * Updates the time passed, deltaTime in Duration
     */
    public void update(final Duration deltaTime) {
        if (paused) return;
        if (isOver()) {
            reset();
        }
        timePassed = timePassed.plus(deltaTime);
    }

    /**
     * Updates the time passed, using a float for the delta time.
     */
    public void update(final float dt) {
        update(Duration.ofMillis((long) (dt * 1000)));
    }

    /**
     * Resets and starts the delay
     */
    public Delay restart() {
        timePassed = Duration.ZERO;
        paused = false;
        return this;
    }

    /**
     * Resets the delay but keeps the paused state
     */
    public Delay reset() {
        timePassed = Duration.ZERO;
        return this;
    }

    /**
     * Pauses the delay
     */
    public Delay stop() {
        paused = true;
        return this;
    }

    /**
     * Resumes the delay
     */
    public Delay start() {
        paused = false;
        return this;
    }
}
