package org.skellig.teststep.processing.model.factory;

import org.skellig.teststep.processing.model.ExpectedResult;
import org.skellig.teststep.processing.model.ValidationDetails;
import org.skellig.teststep.processing.model.ValidationType;

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

class ValidationDetailsFactory {

    private static final Pattern GROUPED_PROPERTIES_PATTERN = Pattern.compile("\\[([\\w,\\s]+)\\]");
    private static final Pattern INDEX_PROPERTY_PATTERN = Pattern.compile("\\[\\s*\\d+\\s*\\]");
    private static final Pattern SPLIT_PATTERN = Pattern.compile(",");

    private static Set<String> validationKeywords;
    private static Set<String> validationTypeKeywords;

    private Properties keywordsProperties;
    private TestStepFactoryValueConverter testStepFactoryValueConverter;

    ValidationDetailsFactory(Properties keywordsProperties,
                             TestStepFactoryValueConverter testStepFactoryValueConverter) {

        this.keywordsProperties = keywordsProperties;
        this.testStepFactoryValueConverter = testStepFactoryValueConverter;

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

    ValidationDetails create(Map<String, Object> rawTestStep, Map<String, Object> parameters) {

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

    private Optional<Object> getValidationDetails(Map<String, Object> rawTestStep) {
        return validationKeywords.stream()
                .map(rawTestStep::get)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private ExpectedResult createExpectedResult(String propertyName, Object expectedResult, Map<String, Object> parameters) {
        expectedResult = testStepFactoryValueConverter.convertValue(expectedResult, parameters);
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
            return new ExpectedResult(propertyName, expectedResult, null);
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

    private String getFromTestKeyword() {
        return getKeywordName("test.step.keyword.from_test", "from_test");
    }

    protected String getKeywordName(String keywordName, String defaultValue) {
        return keywordsProperties == null ? defaultValue : keywordsProperties.getProperty(keywordName, defaultValue);
    }

}