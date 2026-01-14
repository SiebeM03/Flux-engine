package me.siebe.flux.util.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTest {
    @Test
    public void notNullTest() {
        assertDoesNotThrow(() -> Validator.notNull("Test"));
        assertThrows(NullPointerException.class, () -> Validator.notNull(null));

        assertEquals("Test", Validator.notNull("Test"));
    }

    @Test
    public void assertGreaterThanTest() {
        assertThrows(IllegalArgumentException.class, () -> Validator.assertGreaterThan(-10, 0, ""));
        assertDoesNotThrow(() -> Validator.assertGreaterThan(10, 0, ""));
    }

    @Test
    public void assertGreaterThanOrEqualTest() {
        assertThrows(IllegalArgumentException.class, () -> Validator.assertGreaterThanOrEqual(-10, 0, ""));
        assertDoesNotThrow(() -> Validator.assertGreaterThanOrEqual(10, 0, ""));
        assertDoesNotThrow(() -> Validator.assertGreaterThanOrEqual(0, 0, ""));
    }

    @Test
    public void assertLessThanTest() {
        assertThrows(IllegalArgumentException.class, () -> Validator.assertLessThan(10, 0, ""));
        assertDoesNotThrow(() -> Validator.assertLessThan(-10, 0, ""));
    }

    @Test
    public void assertLessThanOrEqualTest() {
        assertThrows(IllegalArgumentException.class, () -> Validator.assertLessThanOrEqual(10, 0, ""));
        assertDoesNotThrow(() -> Validator.assertLessThanOrEqual(-10, 0, ""));
        assertDoesNotThrow(() -> Validator.assertLessThanOrEqual(0, 0, ""));
    }
}
