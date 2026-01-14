package me.siebe.flux.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueUtilsTest {
    @Test
    public void valueWithFallbackIntTest() {
        int value = 1;
        int fallback = 2;

        assertEquals(1, ValueUtils.valueWithFallback(value, () -> fallback));
        assertEquals(2, ValueUtils.valueWithFallback(null, () -> fallback));
    }

    @Test
    public void valueWithFallbackLongTest() {
        long value = 1;
        long fallback = 2;

        assertEquals(1, ValueUtils.valueWithFallback(value, () -> fallback));
        assertEquals(2, ValueUtils.valueWithFallback(null, () -> fallback));
    }

    @Test
    public void valueWithFallbackStringTest() {
        String value = "test";
        String fallback = "fallback";

        assertEquals("test", ValueUtils.valueWithFallback(value, () -> fallback));
        assertEquals("fallback", ValueUtils.valueWithFallback(null, () -> fallback));
    }

    @Test
    public void bottomClampedIntTest() {
        assertEquals(1, ValueUtils.bottomClamped(1, 0));
        assertEquals(0, ValueUtils.bottomClamped(-10, 0));
    }

    @Test
    public void topClampedIntTest() {
        assertEquals(1, ValueUtils.topClamped(1, 5));
        assertEquals(5, ValueUtils.topClamped(10, 5));
    }

    @Test
    public void bottomClampedLongTest() {
        assertEquals(1, ValueUtils.clampedValue(1, 0, 5));
        assertEquals(0, ValueUtils.clampedValue(-10, 0, 5));
        assertEquals(5, ValueUtils.clampedValue(10, 0, 5));
    }

}
