package org.skellig.teststep.processing.validation;


import io.restassured.path.json.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.skellig.teststep.processing.validation.comparator.ValueComparator;
import org.skellig.teststep.reader.exception.ValidationException;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.path.json.JsonPath.from;

public class JsonPathTestStepResultValidator extends BaseTestStepResultValidator {

    public JsonPathTestStepResultValidator(ValueComparator valueComparator) {
        super(valueComparator);
    }

    @Override
    protected void validateActualResult(Object actualResult, ValidationDetails.ExpectedTestResult expectedResult) {
        JsonPath json = from((String) actualResult);

        expectedResult.getActualExpectedValues().entrySet().stream()
                .filter(entry -> !StringUtils.isEmpty(entry.getKey()))
                .forEach(entry -> compareJsonPathWithExpected(json, entry.getKey(), entry.getValue(), actualResult));
    }

    private void compareJsonPathWithExpected(JsonPath json, String actualJsonPathValue, Object expectedValue,
                                             Object originalTestResult) {
        String jsonPath = extractJsonPath(actualJsonPathValue, ValidationType.JSON_PATH.getPattern());
        String actualValue = json.getString(jsonPath);
        if (!valueComparator.compare(actualValue, expectedValue)) {
            String errorMessage = String.format("Failed to validate test result '%s' in json path '%s'. Expected: '%s', actual: '%s'",
                    originalTestResult, jsonPath, expectedValue, actualValue);
            throw new ValidationException(errorMessage);
        }
    }

    private String extractJsonPath(String key, Pattern pattern) {
        Matcher matcher = pattern.matcher(key);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return key;
    }

    @Override
    public boolean isApplicableFor(ValidationType validationType) {
        return ValidationType.JSON_PATH.equals(validationType);
    }
}