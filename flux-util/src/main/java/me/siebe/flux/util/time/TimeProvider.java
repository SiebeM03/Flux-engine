package me.siebe.flux.util.time;

/**
 * Provides a high-precision time source for timing operations such as delta time and FPS calculation.
 * Implementations may use platform-specific timing (e.g. GLFW) or the JVM's system timer.
 */
@FunctionalInterface
public interface TimeProvider {
    
    /**
     * Returns the current time in seconds.
     *
     * @return the current time in seconds
     */
    double getTimeSeconds();
}
