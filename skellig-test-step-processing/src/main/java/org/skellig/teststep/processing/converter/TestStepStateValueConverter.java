package org.skellig.teststep.processing.converter;

import org.skellig.teststep.processing.exception.TestDataConversionException;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TestStepStateValueConverter implements TestStepValueConverter {

    private static final Pattern GET_PATTERN = Pattern.compile("get\\(([\\w_\\$\\.]+)\\)(\\.(.+\\)))?");

    private TestScenarioState testScenarioState;
    private TestStepValueExtractor valueExtractor;

    TestStepStateValueConverter(TestScenarioState testScenarioState,
                                TestStepValueExtractor valueExtractor) {
        this.testScenarioState = testScenarioState;
        this.valueExtractor = valueExtractor;
    }

    @Override
    public Object convert(String value) {
        Matcher matcher = GET_PATTERN.matcher(value);

        Object result = value;
        while (matcher.find() && result instanceof String) {
            if (hasIdOnly(matcher)) {
                result = extractById(result, matcher);
            } else if (hasExtractFunction(matcher)) {
                result = extractUsingExtractorFunction(result, matcher);
            }
        }

        return result;
    }

    private Object extractById(Object value, Matcher matcher) {
        String key = matcher.group(1);
        Object valueFromState = testScenarioState.get(key).orElseThrow(() -> throwException(key));
        String originalValue = matcher.group(0);

        return originalValue.equals(value) ? valueFromState : replace(value, originalValue, valueFromState);
    }

    private Object extractUsingExtractorFunction(Object value, Matcher matcher) {
        String key = matcher.group(1);
        Object valueFromState = testScenarioState.get(key).orElseThrow(() -> throwException(key));
        String originalValue = matcher.group(0);
        String extractionParameter = matcher.group(3);

        Object extractedValue = valueExtractor.extract(valueFromState, extractionParameter);

        return originalValue.equals(value) ? extractedValue : replace(value, originalValue, extractedValue);
    }

    private String replace(Object value, String toReplace, Object replaceWith) {
        return String.valueOf(value).replace(toReplace, String.valueOf(replaceWith));
    }

    private boolean hasExtractFunction(Matcher matcher) {
        return !hasIdOnly(matcher);
    }

    private boolean hasIdOnly(Matcher matcher) {
        return matcher.group(2) == null;
    }

    private TestDataConversionException throwException(String key) {
        return new TestDataConversionException("No data found in Test Scenario State with key " + key);
    }

}
