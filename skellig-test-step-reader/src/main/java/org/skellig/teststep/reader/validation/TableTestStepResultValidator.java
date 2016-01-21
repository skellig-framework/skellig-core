package org.skellig.teststep.reader.validation;


import org.skellig.teststep.reader.exception.ValidationException;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;
import org.skellig.teststep.reader.validation.comparator.ValueComparator;

import java.util.Collections;
import java.util.Map;

public class TableTestStepResultValidator extends BaseTestStepResultValidator {

    public TableTestStepResultValidator(ValueComparator valueComparator) {
        super(valueComparator);
    }

    @Override
    protected void validateActualResult(Object actualResult, ValidationDetails.ExpectedTestResult expectedResult) {
        Map actualResultAsMap = actualResult instanceof Map ? (Map) actualResult : Collections.emptyMap();

        expectedResult.getActualExpectedValues().entrySet()
                .forEach(entry -> compareWithExpected(actualResultAsMap, entry));
    }

    private void compareWithExpected(Map actualResultAsMap, Map.Entry<String, Object> expectedResult) {
        if (actualResultAsMap.containsKey(expectedResult.getKey())) {
            Object actualValue = actualResultAsMap.get(expectedResult.getKey());
            if (!valueComparator.compare(actualValue, expectedResult.getValue())) {
                throw new ValidationException(String.format("Failed validation of field '%s'. Expected: '%s', actual: '%s'",
                        expectedResult.getKey(), expectedResult.getValue(), actualValue));
            }
        } else {
            throw new ValidationException(String.format("Field '%s' is not found in the test result", expectedResult.getKey()));
        }
    }

    @Override
    public boolean isApplicableFor(ValidationType validationType) {
        return ValidationType.TABLE.equals(validationType);
    }
}