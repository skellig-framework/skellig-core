package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.ExpectedResult;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseTestStepFactory implements TestStepFactory {

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

    ExpectedResult createExpectedResult(String propertyName, Object expectedResult) {
        if (expectedResult instanceof Map) {
            Map<String, Object> expectedResultAsMap = (Map) expectedResult;
            ValidationType validationType = getValidationType(expectedResultAsMap);

            Object matchValue = expectedResultAsMap.entrySet().stream()
                    .filter(entry -> validationTypeKeywords.contains(entry.getKey()))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(expectedResultAsMap);

            if (matchValue == expectedResultAsMap || matchValue instanceof Map) {
                matchValue = ((Map<String, Object>) matchValue).entrySet().stream()
                        .map(entry -> createExpectedResult(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
            } else if (matchValue instanceof List) {
                matchValue = ((List<Object>) matchValue).stream()
                        .map(entry -> createExpectedResult(null, entry))
                        .collect(Collectors.toList());
            }

            return new ExpectedResult(propertyName, matchValue, validationType);
        } else if (expectedResult instanceof List) {
            expectedResult = ((List<Object>) expectedResult).stream()
                    .map(entry -> createExpectedResult(null, entry))
                    .collect(Collectors.toList());
        }

        return new ExpectedResult(propertyName, expectedResult, null);
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
