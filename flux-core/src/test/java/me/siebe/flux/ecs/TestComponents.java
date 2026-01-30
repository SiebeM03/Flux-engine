package me.siebe.flux.ecs;

import java.util.Objects;

/**
 * Test component classes for comprehensive testing of the ECS system.
 */
public class TestComponents {
    private TestComponents() {}

    /**
     * Simple position component for testing.
     */
    public static class Position {
        public float x;
        public float y;

        public Position() {
            this(0, 0);
        }

        public Position(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Position position)) return false;
            return Float.compare(x, position.x) == 0 && Float.compare(y, position.y) == 0;
        }

        @Override
        public int hashCode() {
            return 31 * Float.hashCode(x) + Float.hashCode(y);
        }
    }


    /**
     * Simple velocity component for testing.
     */
    public static class Velocity {
        public float dx;
        public float dy;

        public Velocity() {
            this(0, 0);
        }

        public Velocity(float dx, float dy) {
            this.dx = dx;
            this.dy = dy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Velocity velocity)) return false;
            return Float.compare(velocity.dx, dx) == 0 && Float.compare(velocity.dy, dy) == 0;
        }

        @Override
        public int hashCode() {
            return 31 * Float.hashCode(dx) + Float.hashCode(dy);
        }
    }


    /**
     * Simple health component for testing.
     */
    public static class Health {
        public int current;
        public int max;

        public Health() {
            this(100, 100);
        }

        public Health(int current, int max) {
            this.current = current;
            this.max = max;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Health health)) return false;
            return current == health.current && max == health.max;
        }

        @Override
        public int hashCode() {
            return 31 * current + max;
        }
    }


    /**
     * Simple name component for testing.
     */
    public static class Name {
        public String value;

        public Name() {
            this("");
        }

        public Name(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Name name)) return false;
            return Objects.equals(value, name.value);
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }


    /**
     * Tag component (empty, just for marking entities).
     */
    public static class PlayerTag {
    }

    /**
     * Another tag component for testing.
     */
    public static class EnemyTag {
    }

    /**
     * Component with mutable state for testing modifications.
     */
    public static class Counter {
        public int count;

        public Counter() {
            this(0);
        }

        public Counter(int count) {
            this.count = count;
        }

        public void increment() {
            count++;
        }
    }


    /**
     * Complex component for testing with multiple fields.
     */
    public static class Transform {
        public float posX;
        public float posY;
        public float posZ;
        public float rotation;
        public float scaleX;
        public float scaleY;

        public Transform() {
            this(0, 0, 0, 0, 1, 1);
        }

        public Transform(float posX, float posY, float posZ, float rotation, float scaleX, float scaleY) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.rotation = rotation;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
        }
    }
}
