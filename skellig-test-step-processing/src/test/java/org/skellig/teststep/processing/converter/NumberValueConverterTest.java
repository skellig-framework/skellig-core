package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberValueConverterTest {

    private NumberValueConverter numberValueConverter;

    @BeforeEach
    void setUp() {
        numberValueConverter = new NumberValueConverter();
    }

    @Test
    void testConvertInt() {
        assertEquals(450, numberValueConverter.convert("(int)450"));
    }

    @Test
    void testConvertLong() {
        assertEquals(450L, numberValueConverter.convert("(long) 450"));
    }

    @Test
    void testConvertFloat() {
        assertEquals(450f, numberValueConverter.convert("(float) 450.0"));
    }

    @Test
    void testConvertFloat2() {
        assertEquals(1.0032f, numberValueConverter.convert("(float) 1.0032"));
    }

    @Test
    void testConvertDouble() {
        assertEquals(450.0d, numberValueConverter.convert("(double) 450.0"));
    }

    @Test
    void testConvertDouble2() {
        assertEquals(0.00001d, numberValueConverter.convert("(double)0.00001"));
    }
}