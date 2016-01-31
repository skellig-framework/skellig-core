package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PropertyAndParameterValueConverterTest {

    private static final String CUSTOM_PROPERTY_KEY = "custom_properties";
    private static final String DEFAULT_CUSTOM_PROPERTY_VALUE = "from custom properties";

    private PropertyAndParameterValueConverter valueConverter;
    private TestScenarioState testScenarioState;
    private TestStepValueExtractor valueExtractor;

    @BeforeEach
    void setUp() {
        testScenarioState = mock(TestScenarioState.class);
        when(testScenarioState.get("parameters")).thenReturn(Optional.empty());

        valueExtractor = mock(TestStepValueExtractor.class);

        valueConverter = new PropertyAndParameterValueConverter(
                testScenarioState,
                new ArrayList<TestStepValueConverter>() {
                    {
                        add(new TestStepStateValueConverter(testScenarioState, valueExtractor));
                        add(new DateValueConverter());
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
    void testSimpleParameterWithValue() {
        when(testScenarioState.get("parameters")).thenReturn(Optional.of(Collections.singletonMap("1", "v1")));

        assertEquals("v1", valueConverter.convert("${1}"));
    }

    @Test
    void testSimpleParameterWithNoValueAndDefault() {
        assertEquals("def", valueConverter.convert("${1:def}"));
    }

    @Test
    void testSimpleParameterWithValueAndDefault() {
        when(testScenarioState.get("parameters")).thenReturn(Optional.of(Collections.singletonMap("key_1", "v1")));

        assertEquals("v1", valueConverter.convert("${key_1:def}"));
    }

    @Test
    void testWithNestedParameters() {
        when(testScenarioState.get("parameters")).thenReturn(Optional.of(Collections.singletonMap("key_2", "v2")));

        assertEquals("v2", valueConverter.convert("${key_1 : ${key_2 : v3}}"));
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
    void testWithComplexNestedParametersAndManyAttachedTexts() {
        when(testScenarioState.get("parameters")).thenReturn(Optional.of(Collections.singletonMap("key_2", "v2")));

        assertEquals("p1_p2_v2_end", valueConverter.convert("p1_${key_1 : p2_${key_2 : ${id:v3}}_end}"));
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