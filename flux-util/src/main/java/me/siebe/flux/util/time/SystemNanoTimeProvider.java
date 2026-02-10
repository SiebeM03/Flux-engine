package me.siebe.flux.util.time;

public class SystemNanoTimeProvider implements TimeProvider {
    private static final double NANOS_TO_SECONDS = 1.0 / 1_000_000_000.0;

    @Override
    public double getTimeSeconds() {
        return System.nanoTime() * NANOS_TO_SECONDS;
    }
}
