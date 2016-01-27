package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.ExpectedResult;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

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

    private static final Pattern GROUPED_PROPERTIES_PATTERN = Pattern.compile("\\[([\\w,\\s]+)\\]");
    private static final Pattern INDEX_PROPERTY_PATTERN = Pattern.compile("\\[\\s*\\d+\\s*\\]");
    private static final Pattern SPLIT_PATTERN = Pattern.compile(",");
    private static final String TEST_STEP_NAME_KEYWORD = "test.step.name";

    private static Set<String> testDataKeywords;
    private static Set<String> validationKeywords;
    private static Set<String> validationTypeKeywords;

    private Properties keywordsProperties;

    public BaseTestStepFactory() {
        this(null);
    }

    public BaseTestStepFactory(Properties keywordsProperties) {
        this.keywordsProperties = keywordsProperties;

        if (testDataKeywords == null) {
            testDataKeywords = Stream.of(getKeywordName("test.step.data", "data"),
                    getKeywordName("test.step.payload", "payload"),
                    getKeywordName("test.step.body", "body"),
                    getKeywordName("test.step.request", "request"),
                    getKeywordName("test.step.response", "response"),
                    getKeywordName("test.step.message", "message"))
                    .collect(Collectors.toSet());
        }
        if (validationKeywords == null) {
            validationKeywords = Stream.of(getKeywordName("test.step.validate", "validate"),
                    getKeywordName("test.step.expected_result", "expected result"),
                    getKeywordName("test.step.expected_response", "expected response"),
                    getKeywordName("test.step.expected_message", "expected message"),
                    getKeywordName("test.step.assert", "assert"))
                    .collect(Collectors.toSet());
        }
        if (validationTypeKeywords == null) {
            validationTypeKeywords = Stream.of(getKeywordName("test.step.all_match", "all_match"),
                    getKeywordName("test.step.any_match", "any_match"),
                    getKeywordName("test.step.none_match", "none_match"),
                    getKeywordName("test.step.any_none_match", "any_none_match"))
                    .collect(Collectors.toSet());
        }
    }

    protected Object getTestData(Map<String, Object> rawTestStep) {
        return testDataKeywords.stream()
                .filter(rawTestStep::containsKey)
                .map(rawTestStep::get)
                .findFirst()
                .orElse(null);
    }

    protected Optional<Object> getValidationDetails(Map<String, Object> rawTestStep) {
        return validationKeywords.stream()
                .map(rawTestStep::get)
                .filter(Objects::nonNull)
                .findFirst();
    }

    protected ValidationDetails createValidationDetails(Map<String, Object> rawTestStep) {

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
                    builder.withExpectedResult(createExpectedResult("", rawExpectedResult));
                } else {
                    builder.withExpectedResult(createExpectedResult("", rawValidationDetails.get()));
                }
            } else if (rawValidationDetails.get() instanceof List) {
                builder.withExpectedResult(createExpectedResult("", rawValidationDetails.get()));
            }

            return builder.build();
        } else {
            return null;
        }

    }

    private ExpectedResult createExpectedResult(String propertyName, Object expectedResult) {
        if (expectedResult instanceof Map) {
            Map<String, Object> expectedResultAsMap = (Map) expectedResult;

            Object matchValue = expectedResultAsMap.entrySet().stream()
                    .filter(entry -> validationTypeKeywords.contains(entry.getKey()))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(expectedResultAsMap);

            if (matchValue == expectedResultAsMap || matchValue instanceof Map) {
                matchValue = createExpectedResults(extendExpectedResultMapIfApplicable((Map<String, Object>) matchValue));
            } else if (matchValue instanceof List) {
                matchValue = createExpectedResults((List<Object>) matchValue);
            }

            return new ExpectedResult(propertyName, matchValue, getValidationType(expectedResultAsMap));
        } else if (expectedResult instanceof List) {
            expectedResult = createExpectedResults((List<Object>) expectedResult);
        }

        return new ExpectedResult(propertyName, expectedResult, null);
    }

    private Object createExpectedResults(Map<String, Object> matchValue) {
        return matchValue.entrySet().stream()
                .map(entry -> createExpectedResult(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Object createExpectedResults(List<Object> expectedResult) {
        return  expectedResult.stream()
                .map(entry -> createExpectedResult(null, entry))
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
        return getKeywordName("test.step.from_test", "from_test");
    }

    protected String getKeywordName(String keywordName, String defaultValue) {
        return keywordsProperties == null ? defaultValue : keywordsProperties.getProperty(keywordName, defaultValue);
    }
}
