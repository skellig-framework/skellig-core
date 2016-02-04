package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestStepStateValueConverterTest {

    private TestStepStateValueConverter testStepStateValueConverter;
    private TestScenarioState testScenarioState;
    private TestStepValueExtractor valueExtractor;

    @BeforeEach
    void setUp() {
        testScenarioState = mock(TestScenarioState.class);
        valueExtractor = mock(TestStepValueExtractor.class);
        testStepStateValueConverter = new TestStepStateValueConverter(testScenarioState, valueExtractor);
    }

    @Test
    void testGetSimpleValueFromState() {
        String expectedResult = "v1";
        when(testScenarioState.get("key")).thenReturn(Optional.of(expectedResult));

        assertEquals(expectedResult, testStepStateValueConverter.convert("get(key)"));
    }

    @Test
    void testGetObjectValueFromState() {
        Object expectedResult = new Object();
        when(testScenarioState.get("key")).thenReturn(Optional.of(expectedResult));

        assertEquals(expectedResult, testStepStateValueConverter.convert("get(key)"));
    }

    @Test
    void testGetValueFromStateWithAttachedString() {
        when(testScenarioState.get("key")).thenReturn(Optional.of(Collections.singletonList("_")));

        assertEquals("^[_]^", testStepStateValueConverter.convert("^get(key)^"));
    }

    @Test
    void testGetValueFromStateWithExtractorAndAttachedString() {
        List<String> value = Collections.singletonList("_");
        when(valueExtractor.extract(value, "([0])")).thenReturn(value.get(0));
        when(testScenarioState.get("key")).thenReturn(Optional.of(value));

        assertEquals("^_^", testStepStateValueConverter.convert("^get(key).([0])^"));
    }

    @Test
    void testGetValueFromStateWithExtractor() {
        String value = "value";
        when(valueExtractor.extract(value, "(length)")).thenReturn(value.length());
        when(testScenarioState.get("key")).thenReturn(Optional.of(value));

        assertEquals(value.length(), testStepStateValueConverter.convert("get(key).(length)"));
    }
}