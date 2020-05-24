package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidationDetailsFactory {

    private static final String FROM_TEST = "fromTest";
    private static final Set<String> SUPPORTED_VALIDATION_KEYWORDS =
            Stream.of("validate", "expected result", "assert").collect(Collectors.toSet());

    public ValidationDetails create(Map<String, Object> rawTestStep) {

        Optional<Object> rawValidationDetails =
                SUPPORTED_VALIDATION_KEYWORDS.stream()
                        .map(rawTestStep::get)
                        .filter(Objects::nonNull)
                        .findFirst();

        ValidationDetails.Builder builder = new ValidationDetails.Builder();
        if (rawValidationDetails.isPresent()) {
            if (rawValidationDetails.get() instanceof Map) {
                Object fromTestId = ((Map) rawValidationDetails.get()).get(FROM_TEST);
                builder.withTestStepId((String) fromTestId);

                ((Map<String, Object>) rawValidationDetails.get()).entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(FROM_TEST))
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
}
