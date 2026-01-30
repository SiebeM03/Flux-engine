package me.siebe.flux.util.time;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

public class Delay {
    private static final Logger logger = LoggerFactory.getLogger(Delay.class, "timer");
    private final float duration;
    private final boolean autoReset;

    private float timePassed = 0.0f;
    private boolean paused = false;

    public Delay(float duration, boolean autoReset) {
        this.duration = duration;
        this.autoReset = autoReset;
    }

    public Delay(float duration) {
        this(duration, true);
    }

    public boolean isOver() {
        return timePassed >= duration;
    }

    public float timeRemaining() {
        return duration - timePassed;
    }

    public void update(float deltaTime) {
        if (paused) return;
        if (isOver() && !autoReset) {
            return;
        }
        reset();
        timePassed += deltaTime;
    }

    public Delay restart() {
        timePassed = 0;
        paused = false;
        return this;
    }

    public Delay reset() {
        timePassed = 0;
        return this;
    }

    public Delay stop() {
        paused = true;
        return this;
    }

    public Delay start() {
        paused = false;
        return this;
    }

    public boolean isAutoReset() {
        return autoReset;
    }
}
