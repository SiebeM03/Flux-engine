package me.siebe.flux.util.time;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public final class Timer {
    private static final Logger logger = LoggerFactory.getLogger(Timer.class, "timer");

    private double deltaTime;
    private double lastFrameTime;
    private double currentFrameTime;
    private double startTime;
    private long frameCount;

    private final float DEFAULT_PRINT_DELAY = 1.0f;
    private final Delay printDelay;

    // Average the fps over a few files to prevent outliers
    private final int FRAMES_TO_CONSIDER = 100;
    private int newFrameIndex;
    private final double[] frameTimes;

    public Timer() {
        this.deltaTime = 0.0;
        this.startTime = glfwGetTime();
        this.lastFrameTime = startTime;
        this.currentFrameTime = lastFrameTime;
        this.frameCount = 0L;

        this.printDelay = new Delay(DEFAULT_PRINT_DELAY);

        this.newFrameIndex = 0;
        this.frameTimes = new double[FRAMES_TO_CONSIDER];
        logger.debug("Initialized Timer");
    }


    public void update() {
        this.currentFrameTime = glfwGetTime();
        this.deltaTime = this.currentFrameTime - this.lastFrameTime;
        this.lastFrameTime = this.currentFrameTime;
        this.frameCount++;

        printDelay.update((float) deltaTime);
        addFrameToList();
    }

    public double getFps() {
        double averageFrameTime = Arrays.stream(frameTimes).average().orElse(1.0);
        return (1.0 / averageFrameTime);
    }

    private void addFrameToList() {
        this.frameTimes[newFrameIndex++] = deltaTime;
        if (newFrameIndex == FRAMES_TO_CONSIDER) {
            newFrameIndex = 0;
        }
    }

    public void print() {
        if (printDelay.isOver()) {
            logger.trace("FPS: " + getFps());
            printDelay.restart();
        }
    }

    public double getDeltaTime() {
        return this.deltaTime;
    }

    /**
     * Gets the total time since the timer was created.
     *
     * @return the total time in seconds
     */
    public double getTotalTime() {
        return this.currentFrameTime - this.startTime;
    }

    /**
     * Gets the current frame count.
     *
     * @return the number of frames that have been updated
     */
    public long getFrameCount() {
        return this.frameCount;
    }
}
