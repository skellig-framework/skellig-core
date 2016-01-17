package org.skellig.teststep.reader.model.factory;

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
                builder.withTestStepId((String) fromTestId);

                ((Map<String, Object>) rawValidationDetails.get()).entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(getFromTestKeyword()))
                        .forEach(entry -> buildValidationEntry(builder, entry.getKey(), entry.getValue()));
            } else if (rawValidationDetails.get() instanceof List) {
                buildValidationEntry(builder, "", rawValidationDetails.get());
            }

            return builder.build();
        } else {
            return null;
        }

    }

    private void buildValidationEntry(ValidationDetails.Builder builder, String actualValue, Object expectedValue) {
        if (expectedValue instanceof List) {
            List expectedValueAsList = (List) expectedValue;
            if (!expectedValueAsList.isEmpty() && expectedValueAsList.get(0) instanceof Map) {
                expectedValueAsList.stream()
                        .forEach(v -> buildValidationEntry(builder, actualValue, v));
            } else {
                builder.withActualAndExpectedValues(ValidationType.DEFAULT, actualValue, expectedValue);
            }
        } else if (expectedValue instanceof Map) {
            Map<String, Object> expectedValues = (Map<String, Object>) expectedValue;
            String validationFunction = expectedValues.keySet().stream().findFirst().orElse("");
            builder.withActualAndExpectedValues(ValidationType.getValidationTypeFor(validationFunction), expectedValues);
        } else {
            builder.withActualAndExpectedValues(ValidationType.getValidationTypeFor(actualValue), actualValue, expectedValue);
        }
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
