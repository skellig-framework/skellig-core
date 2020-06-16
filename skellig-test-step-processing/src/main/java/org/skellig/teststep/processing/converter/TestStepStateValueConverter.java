package org.skellig.teststep.processing.converter;

import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.valueextractor.DefaultValueExtractor;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestStepStateValueConverter implements TestStepValueConverter {

    private static final Pattern GET_PATTERN = Pattern.compile("get\\(([\\w_\\$\\.]+)\\)(\\.?(.+))?");

    private TestScenarioState testScenarioState;
    private TestStepValueExtractor valueExtractor;

    protected TestStepStateValueConverter(TestScenarioState testScenarioState,
                                          TestStepValueExtractor valueExtractor) {
        this.testScenarioState = testScenarioState;
        this.valueExtractor = valueExtractor;
    }

    @Override
    public Object convert(String value) {
        Matcher matcher = GET_PATTERN.matcher(value);

        while (matcher.find()) {
            if (hasIdOnly(matcher)) {
                value = extractById(value, matcher);
            } else if (hasExtractFunction(matcher)) {
                value = extractUsingFunction(value, matcher);
            }
        }

        return value;
    }

    private String extractById(String value, Matcher matcher) {
        String key = matcher.group(1);
        if (testScenarioState.get(key).isPresent()) {
            String originalValue = matcher.group(0);
            Object valueFromState = testScenarioState.get(key).get();

            return value.replace(originalValue, String.valueOf(valueFromState));
        } else {
            return value;
        }
    }

    private String extractUsingFunction(String value, Matcher matcher) {
        String key = matcher.group(1);
        if (testScenarioState.get(key).isPresent()) {
            String originalValue = matcher.group(0);
            String extractionParameter = matcher.group(3);
            Object valueFromState = testScenarioState.get(key).get();

            Object extractedValue = valueExtractor.extract(valueFromState, extractionParameter);

            return value.replace(originalValue, String.valueOf(extractedValue));
        } else {
            return value;
        }
    }

    private boolean hasExtractFunction(Matcher matcher) {
        return matcher.groupCount() == 3;
    }

    private boolean hasIdOnly(Matcher matcher) {
        return matcher.groupCount() == 1;
    }

    public static class Builder {

        private DefaultValueExtractor.Builder valueExtractorBuilder;

        public Builder() {
            valueExtractorBuilder = new DefaultValueExtractor.Builder();
        }

        public Builder withValueExtractor(TestStepValueExtractor valueExtractor) {
            this.valueExtractorBuilder.withValueExtractor(valueExtractor);
            return this;
        }

        public TestStepValueConverter build(TestScenarioState testScenarioState) {
            return new TestStepStateValueConverter(testScenarioState, valueExtractorBuilder.build());
        }
    }

}
