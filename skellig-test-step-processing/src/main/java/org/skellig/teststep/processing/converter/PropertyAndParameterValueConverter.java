package org.skellig.teststep.processing.converter;

import org.apache.commons.lang3.StringUtils;
import org.skellig.teststep.processing.state.TestScenarioState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PropertyAndParameterValueConverter implements TestStepValueConverter {

    private static final Pattern PARAMETER_REGEX = Pattern.compile("\\$\\{([\\w-_]+)(\\s*:\\s*(.+))?\\}");
    private static final String PARAMETERS_KEY = "parameters";

    private TestScenarioState testScenarioState;
    private Function<String, String> propertyExtractorFunction;
    private List<TestStepValueConverter> valueConverters;

    PropertyAndParameterValueConverter(TestScenarioState testScenarioState,
                                       List<TestStepValueConverter> valueConverters,
                                       Function<String, String> propertyExtractorFunction) {
        this.testScenarioState = testScenarioState;
        this.propertyExtractorFunction = propertyExtractorFunction;
        // It's important to add this converter at the beginning of the list
        // to make sure that parameters and other properties are processes first before other functions
        if (valueConverters.stream().noneMatch(item -> getClass().equals(item.getClass()))) {
            this.valueConverters = new ArrayList<>();
            this.valueConverters.add(this);
            this.valueConverters.addAll(valueConverters);
        }
    }

    @Override
    public Object convert(String value) {
        Matcher matcher = PARAMETER_REGEX.matcher(value);
        if (matcher.find()) {
            String propertyValue = getPropertyValue(matcher.group(1));
            if (StringUtils.isNotEmpty(propertyValue) || !hasDefaultValue(matcher)) {
                value = value.replace(matcher.group(0), propertyValue);
            } else if (hasDefaultValue(matcher)) {
                String defaultValue = matcher.group(3);
                for (TestStepValueConverter valueConverter : valueConverters) {
                    defaultValue = String.valueOf(valueConverter.convert(defaultValue));
                }
                value = value.replace(matcher.group(0), defaultValue);
            }
        }
        return value;
    }

    private String getPropertyValue(String propertyKey) {
        String propertyValue = null;
        Map parameters = (Map) testScenarioState.get(PARAMETERS_KEY).orElse(Collections.emptyMap());

        if (parameters.containsKey(propertyKey)) {
            propertyValue = String.valueOf(parameters.get(propertyKey));
        }

        if (propertyValue == null && propertyExtractorFunction != null) {
            propertyValue = propertyExtractorFunction.apply(propertyKey);
        }
        if (propertyValue == null) {
            propertyValue = System.getProperty(propertyKey);
        }
        if (propertyValue == null) {
            propertyValue = System.getenv(propertyKey);
        }

        return propertyValue == null ? "" : propertyValue;
    }

    private boolean hasDefaultValue(Matcher matcher) {
        return matcher.group(3) != null;
    }
}
