package me.siebe.flux.util.time;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Delay {
    private static final Logger logger = LoggerFactory.getLogger(Delay.class, "timer");

    private final float duration;
    private final TimeUnit unit;
    private float timePassed = 0f;
    private boolean paused = false;

    /** Marks whether the delay auto-resets after the update is done.*/
    private final boolean autoReset;

    /**
     * Creates a delay with the given duration and time unit.
     *
     * @param duration The duration of the delay
     * @param unit     The unit of the duration
     */
    public Delay(final float duration, final TimeUnit unit, final boolean autoReset) {
        this.duration = duration;
        this.unit = unit;
        this.autoReset = autoReset;
    }

    public Delay(final float duration, final TimeUnit unit) {
        this(duration, unit, true);
    }

    /**
     * Returns true if the delay has completed
     */
    public boolean isOver() {
        return timeRemaining() <= 0f;
    }

    /**
     * Returns the remaining time in the same unit as specified.
     */
    public float timeRemaining() {
        float remaining = duration - timePassed;
        return Math.max(remaining, 0f);
    }

    /**
     * Updates the time passed, deltaTime in Duration
     */
    public void update(final float deltaTime) {
        if (paused) return;

        timePassed += convertToUnit(deltaTime, unit);

        if (isOver() && autoReset) {
            reset();
        }
    }

    /**
     * Resets and starts the delay.
     */
    public Delay restart() {
        timePassed = 0f;
        paused = false;
        return this;
    }

    /**
     * Resets the delay but keeps the paused state
     */
    public Delay reset() {
        timePassed = 0f;
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

    /**
     * Returns the remaining time converted to the specified TimeUnit.
     *
     * @param targetUnit The unit in which to get the remaining time
     * @return Remaining time in the specified unit
     */
    public float getTimeRemaining(final TimeUnit targetUnit) {
        if (targetUnit == unit) {
            return timeRemaining();
        }

        final float remainingNanos = convertToUnit(timeRemaining(), TimeUnit.NANOSECONDS);

        return remainingNanos / (float) targetUnit.toNanos(1);
    }

    /**
     * Converts the input time to the Delay's time unit.
     */
    private float convertToUnit(final float time, final TimeUnit inputUnit) {
        final long nanos = inputUnit.toNanos((long) (time * 1_000_000));
        return nanos / (float) unit.toNanos(1);
    }
}
