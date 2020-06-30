package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PropertyValueConverterTest {

    private static final String CUSTOM_PROPERTY_KEY = "custom_properties";
    private static final String DEFAULT_CUSTOM_PROPERTY_VALUE = "from custom properties";

    private PropertyValueConverter valueConverter;
    private TestScenarioState testScenarioState;
    private TestStepValueExtractor valueExtractor;

    @BeforeEach
    void setUp() {
        testScenarioState = mock(TestScenarioState.class);

        valueExtractor = mock(TestStepValueExtractor.class);

        valueConverter = new PropertyValueConverter(
                new ArrayList<TestStepValueConverter>() {
                    {
                        add(new TestStepStateValueConverter(testScenarioState, valueExtractor));
                        add(new DateTimeValueConverter());
                    }
                },
                (key) -> CUSTOM_PROPERTY_KEY.equals(key) ? DEFAULT_CUSTOM_PROPERTY_VALUE : null
        );
    }

    @Test
    void testSimpleParameterWithNoValue() {
        assertEquals("", valueConverter.convert("${1}"));
    }

    @Test
    void testSimpleParameterWithNoValueAndDefault() {
        assertEquals("def", valueConverter.convert("${1:def}"));
    }

    @Test
    void testWithNestedParametersAndDefault() {
        assertEquals("v3", valueConverter.convert("${key_1 : ${key_2 : v3}}"));
    }

    @Test
    void testWithComplexNestedParametersAndAttachedText() {
        assertEquals("id:v3_end", valueConverter.convert("${key_1 : id:${key_2 : ${id:v3}}_end}"));
    }

    @Test
    void testWithNestedFunctionsWithAttachedText() {
        when(testScenarioState.get("id")).thenReturn(Optional.of("10"));

        assertEquals("_10_", valueConverter.convert("${key_1 : _get(id)_}"));
    }

    @Test
    void testWithNestedFunctionReturningObject() {
        assertEquals(String.class, valueConverter.convert("${key_1 : now(UTC)}").getClass());
    }

    @Test
    void testWithCustomProperties() {
        assertEquals(DEFAULT_CUSTOM_PROPERTY_VALUE, valueConverter.convert("${" + CUSTOM_PROPERTY_KEY + "}"));
    }

    @Test
    void testWithSystemProperties() {
        String key = "key1";
        System.setProperty(key, "v1");

        assertEquals(System.getProperty(key), valueConverter.convert("${" + key + "}"));
    }
}