package game.core.demos;

/**
 * An interface that can be used to add self-contained demo classes to {@link game.core.GameApplication}
 */
public interface Demo {
    void init();

    void update();

    void destroy();
}
