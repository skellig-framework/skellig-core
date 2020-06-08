package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncrementValueConverterTest {

    private IncrementValueConverter converter;

    @BeforeEach
    void setUp() {
        converter = new IncrementValueConverter();
    }

    @AfterEach
    void cleanUp() throws IOException {
        Files.delete(Paths.get(IncrementValueConverter.FILE_NAME));
    }

    @Test
    @DisplayName("Increment value first time Then verify it returned default value with replaced zeros")
    void testIncrementSimpleValueFirstTime() {
        Object result = converter.convert("inc(id,10)");

        assertEquals("0000000001", result);
    }

    @Test
    @DisplayName("Increment value second time Then verify it returned incremented value by 1")
    void testIncrementSimpleValueSecondTime() {
        String value = "inc(id,10)";

        converter.convert(value);
        Object result = converter.convert(value);

        assertEquals("0000000002", result);
    }

    @Test
    @DisplayName("Increment value several times Then verify it returned correct incremented value")
    void testIncrementSimpleValueSeveralTimes() {
        String value = "inc(id,3)";

        Object result = null;
        for (int i = 0; i < 10; i++) {
            result = converter.convert(value);
        }

        assertEquals("010", result);
    }

    @Test
    @DisplayName("Increment value more times than defined in parameter Then verify it does not overflow")
    void testIncrementValueMoreThanLengthOfRegex() {
        String value = "inc(id,1)";

        Object result = null;
        for (int i = 0; i < 10; i++) {
            result = converter.convert(value);
        }

        assertEquals("9", result);
    }

    @Test
    @DisplayName("Increment value with different limits several times Then verify it does not overflow and increment where possible")
    void testIncrementValueWithDifferentLimits() {
        String value = "inc(id ,4)";
        String valueWithId = "inc(id)";

        converter.convert(value);
        Object result = converter.convert(valueWithId);

        assertEquals("0002", result);
    }

    @Test
    @DisplayName("Increment different values Then verify it takes correct values from file and increment them")
    void testIncrementDifferentValues() {
        String value1 = "inc(id, 5)";
        String value2 = "inc(3)";
        String value3 = "inc()";

        Object result1 = null;
        Object result2 = null;
        Object result3 = null;
        for (int i = 0; i < 5; i++) {
            IncrementValueConverter converter = new IncrementValueConverter();

            result1 = converter.convert(value1);
            result2 = converter.convert(value2);
            result3 = converter.convert(value3);
        }

        assertEquals("00005", result1);
        assertEquals("005", result2);
        assertEquals("5", result3);
    }

}