package org.skellig.teststep.processing.model.factory;

import org.apache.commons.lang3.StringUtils;
import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.ExpectedResult;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.ValidationDetails;
import org.skellig.teststep.processing.model.ValidationType;
import org.skellig.teststep.processing.utils.CachedPattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseTestStepFactory implements TestStepFactory {

    private static final Pattern PARAMETER_REGEX = Pattern.compile("\\$\\{([\\w-_]+)(\\s*:\\s*(.+))?\\}");
    private static final Pattern GROUPED_PROPERTIES_PATTERN = Pattern.compile("\\[([\\w,\\s]+)\\]");
    private static final Pattern INDEX_PROPERTY_PATTERN = Pattern.compile("\\[\\s*\\d+\\s*\\]");
    private static final Pattern SPLIT_PATTERN = Pattern.compile(",");
    private static final String TEST_STEP_NAME_KEYWORD = "test.step.keyword.name";
    private static final String VARIABLES_KEYWORD = "test.step.keyword.variables";

    private static Set<String> testDataKeywords;
    private static Set<String> validationKeywords;
    private static Set<String> validationTypeKeywords;

    private Properties keywordsProperties;
    private TestStepValueConverter testStepValueConverter;
    private TestDataConverter testDataConverter;

    public BaseTestStepFactory(Properties keywordsProperties,
                               TestStepValueConverter testStepValueConverter,
                               TestDataConverter testDataConverter) {

        this.keywordsProperties = keywordsProperties;
        this.testStepValueConverter = testStepValueConverter;
        this.testDataConverter = testDataConverter;

        if (testDataKeywords == null) {
            testDataKeywords = Stream.of(
                    getKeywordName("test.step.keyword.data", "data"),
                    getKeywordName("test.step.keyword.payload", "payload"),
                    getKeywordName("test.step.keyword.body", "body"),
                    getKeywordName("test.step.keyword.request", "request"),
                    getKeywordName("test.step.keyword.response", "response"),
                    getKeywordName("test.step.keyword.message", "message"))
                    .collect(Collectors.toSet());
        }
        if (validationKeywords == null) {
            validationKeywords = Stream.of(
                    getKeywordName("test.step.keyword.validate", "validate"),
                    getKeywordName("test.step.keyword.expected_result", "expected result"),
                    getKeywordName("test.step.keyword.expected_response", "expected response"),
                    getKeywordName("test.step.keyword.expected_message", "expected message"),
                    getKeywordName("test.step.keyword.assert", "assert"))
                    .collect(Collectors.toSet());
        }
        if (validationTypeKeywords == null) {
            validationTypeKeywords = Stream.of(
                    getKeywordName("test.step.keyword.all_match", "all_match"),
                    getKeywordName("test.step.keyword.any_match", "any_match"),
                    getKeywordName("test.step.keyword.none_match", "none_match"),
                    getKeywordName("test.step.keyword.any_none_match", "any_none_match"))
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public TestStep create(String testStepName, Map<String, Object> rawTestStep, Map<String, String> parameters) {
        Map<String, Object> additionalParameters = new HashMap<>(parameters);

        Map<String, String> parametersFromTestName = extractParametersFromTestStepName(rawTestStep, testStepName);
        if (parametersFromTestName != null) {
            additionalParameters.putAll(parametersFromTestName);
        }

        Map<String, Object> variables = extractVariables(rawTestStep, additionalParameters);
        if (variables != null) {
            additionalParameters.putAll(variables);
        }

        return createTestStep(rawTestStep).create(
                getId(rawTestStep),
                testStepName,
                getTestData(rawTestStep, additionalParameters),
                createValidationDetails(rawTestStep, additionalParameters),
                additionalParameters,
                variables);
    }

    protected abstract CreateTestStepDelegate createTestStep(Map<String, Object> rawTestStep);

    private Map<String, Object> extractVariables(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        Object rawVariables = rawTestStep.get(getKeywordName(VARIABLES_KEYWORD, "variables"));
        Object convertedVariables = convertHierarchicalData(rawVariables, parameters);
        return convertedVariables instanceof Map ? (Map<String, Object>) convertedVariables : null;
    }

    private Object getTestData(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        return testDataKeywords.stream()
                .filter(rawTestStep::containsKey)
                .map(keyword -> testDataConverter.convert(convertHierarchicalData(rawTestStep.get(keyword), parameters)))
                .findFirst()
                .orElse(null);
    }

    private Object convertHierarchicalData(Object data, Map<String, Object> parameters) {
        if (data instanceof Map) {
            return ((Map<String, Object>) data).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertHierarchicalData(entry.getValue(), parameters)));
        } else if (data instanceof List) {
            return ((List) data).stream()
                    .map(item -> convertHierarchicalData(item, parameters))
                    .collect(Collectors.toList());
        } else {
            return convertValue(data, parameters);
        }
    }

    private Optional<Object> getValidationDetails(Map<String, Object> rawTestStep) {
        return validationKeywords.stream()
                .map(rawTestStep::get)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private Map<String, String> extractParametersFromTestStepName(Map<String, Object> rawTestStep, String testStepName) {
        Map<String, String> parameters = null;
        Matcher matcher = CachedPattern.compile(getName(rawTestStep)).matcher(testStepName);
        if (matcher.find()) {
            parameters = new HashMap<>();
            for (int i = 1; i <= matcher.groupCount(); i++) {
                parameters.put("$" + i, matcher.group(i));
            }
        }
        return parameters;
    }

    private ValidationDetails createValidationDetails(Map<String, Object> rawTestStep, Map<String, Object> parameters) {

        Optional<Object> rawValidationDetails = getValidationDetails(rawTestStep);

        ValidationDetails.Builder builder = new ValidationDetails.Builder();
        if (rawValidationDetails.isPresent()) {
            if (rawValidationDetails.get() instanceof Map) {
                Object fromTestId = ((Map) rawValidationDetails.get()).get(getFromTestKeyword());
                if (fromTestId != null) {
                    builder.withTestStepId((String) fromTestId);

                    Map<String, Object> rawExpectedResult =
                            ((Map<String, Object>) rawValidationDetails.get()).entrySet().stream()
                                    .filter(entry -> !entry.getKey().equals(getFromTestKeyword()))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    builder.withExpectedResult(createExpectedResult("", rawExpectedResult, parameters));
                } else {
                    builder.withExpectedResult(createExpectedResult("", rawValidationDetails.get(), parameters));
                }
            } else if (rawValidationDetails.get() instanceof List) {
                builder.withExpectedResult(createExpectedResult("", rawValidationDetails.get(), parameters));
            }

            return builder.build();
        } else {
            return null;
        }
    }

    private ExpectedResult createExpectedResult(String propertyName, Object expectedResult, Map<String, Object> parameters) {
        if (expectedResult instanceof Map) {
            Map<String, Object> expectedResultAsMap = (Map) expectedResult;

            Object matchValue = expectedResultAsMap.entrySet().stream()
                    .filter(entry -> validationTypeKeywords.contains(entry.getKey()))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(expectedResultAsMap);

            if (matchValue == expectedResultAsMap || matchValue instanceof Map) {
                matchValue = createExpectedResults(extendExpectedResultMapIfApplicable((Map<String, Object>) matchValue), parameters);
            } else if (matchValue instanceof List) {
                matchValue = createExpectedResults((List<Object>) matchValue, parameters);
            }

            return new ExpectedResult(propertyName, matchValue, getValidationType(expectedResultAsMap));
        } else if (expectedResult instanceof List) {
            expectedResult = createExpectedResults((List<Object>) expectedResult, parameters);
            return new ExpectedResult(propertyName, expectedResult, ValidationType.ALL_MATCH);
        } else {
            return new ExpectedResult(propertyName, convertValue(expectedResult, parameters), null);
        }
    }

    private Object createExpectedResults(Map<String, Object> matchValue, Map<String, Object> parameters) {
        return matchValue.entrySet().stream()
                .map(entry -> createExpectedResult(entry.getKey(), entry.getValue(), parameters))
                .collect(Collectors.toList());
    }

    private Object createExpectedResults(List<Object> expectedResult, Map<String, Object> parameters) {
        return expectedResult.stream()
                .map(entry -> createExpectedResult(null, entry, parameters))
                .collect(Collectors.toList());
    }

    private Map<String, Object> extendExpectedResultMapIfApplicable(Map<String, Object> expectedResultAsMap) {
        if (hasSplitProperties(expectedResultAsMap)) {
            Map<String, Object> extendedExpectedResultAsMap = new HashMap<>();

            expectedResultAsMap.forEach((key, value) -> {
                Matcher matcher = GROUPED_PROPERTIES_PATTERN.matcher(key);
                if (matcher.find()) {
                    for (String newPropertyName : SPLIT_PATTERN.split(matcher.group(1))) {
                        extendedExpectedResultAsMap.put(newPropertyName.trim(), value);
                    }
                } else {
                    extendedExpectedResultAsMap.put(key, value);
                }
            });
            return extendedExpectedResultAsMap;
        } else {
            return expectedResultAsMap;
        }
    }

    private boolean hasSplitProperties(Map<String, Object> expectedResultAsMap) {
        return expectedResultAsMap.keySet().stream()
                .anyMatch(key -> key.startsWith("[") && !INDEX_PROPERTY_PATTERN.matcher(key).matches());
    }

    private ValidationType getValidationType(Map expectedResultAsMap) {
        return validationTypeKeywords.stream()
                .filter(expectedResultAsMap::containsKey)
                .findFirst()
                .map(ValidationType::getValidationTypeFor)
                .orElse(ValidationType.ALL_MATCH);
    }

    protected String getId(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey("id") ? String.valueOf(rawTestStep.get("id")) : null;
    }

    protected String getName(Map<String, Object> rawTestStep) {
        return String.valueOf(rawTestStep.get(getKeywordName(TEST_STEP_NAME_KEYWORD, "name")));
    }

    protected String getFromTestKeyword() {
        return getKeywordName("test.step.keyword.from_test", "from_test");
    }

    protected String getKeywordName(String keywordName, String defaultValue) {
        return keywordsProperties == null ? defaultValue : keywordsProperties.getProperty(keywordName, defaultValue);
    }

    protected <T> T convertValue(Object value, Map<String, Object> parameters) {
        Object result = value;
        if (isString(value)) {
            result = applyParameters(String.valueOf(value), parameters);
            if (isString(result)) {
                result = testStepValueConverter.convert(String.valueOf(result));
            }
        }
        return (T) result;
    }

    private Object applyParameters(String valueAsString, Map<String, Object> parameters) {
        Matcher matcher = PARAMETER_REGEX.matcher(valueAsString);
        Object result = valueAsString;
        if (matcher.find()) {
            String parameterName = matcher.group(1);
            Object parameterValue = parameters.getOrDefault(parameterName, null);
            boolean hasDefaultValue = matcher.group(3) != null;
            if (isString(parameterValue)) {
                String parameterValueAsString = String.valueOf(parameterValue);
                if (StringUtils.isNotEmpty(parameterValueAsString) || !hasDefaultValue) {
                    result = valueAsString.replace(matcher.group(0), parameterValueAsString);
                } else {
                    String defaultValue = matcher.group(3);
                    defaultValue = String.valueOf(convertValue(defaultValue, parameters));

                    result = valueAsString.replace(matcher.group(0), defaultValue);
                }
            } else {
                if (parameterValue != null || !hasDefaultValue) {
                    result = parameterValue;
                } else {
                    result = convertValue(matcher.group(3), parameters);
                }
            }
        }
        return result;
    }

    private boolean isString(Object value) {
        return value != null && value.getClass().equals(String.class);
    }

    protected interface CreateTestStepDelegate {

        TestStep create(String id, String name, Object testData, ValidationDetails validationDetails,
                        Map<String, Object> parameters, Map<String, Object> variables);
    }

}
