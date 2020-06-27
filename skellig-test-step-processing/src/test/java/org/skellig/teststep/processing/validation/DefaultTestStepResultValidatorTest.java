package org.skellig.teststep.processing.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.exception.ValidationException;
import org.skellig.teststep.processing.model.ExpectedResult;
import org.skellig.teststep.processing.model.ValidationDetails;
import org.skellig.teststep.processing.model.ValidationType;
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator;
import org.skellig.teststep.processing.valueextractor.DefaultValueExtractor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.skellig.teststep.processing.utils.UnitTestUtils.createMap;

class DefaultTestStepResultValidatorTest {

    private TestStepResultValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DefaultTestStepResultValidator.Builder()
                .withValueComparator(new DefaultValueComparator.Builder().build())
                .withValueExtractor(new DefaultValueExtractor.Builder().build())
                .build();
    }

    @Test
    void testValidate() {
        Map<String, Object> actualResult = createActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult("k1", "v1", null),
                                new ExpectedResult("k2", "v2", null)),
                        ValidationType.ALL_MATCH);

        validator.validate(expectedResult, actualResult);
    }

    @Test
    void testValidateWhenNotValid() {
        Map<String, Object> actualResult = createActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult("k1", "v2", null),
                                new ExpectedResult("k2", "v2", null)),
                        ValidationType.ALL_MATCH);

        ValidationException e = assertThrows(ValidationException.class,
                () -> validator.validate(expectedResult, actualResult));

        assertEquals("Validation failed!\nk1 is not valid. Expected: v2 Actual: v1\n", e.getMessage());
    }

    @Test
    void testValidateAnyMatch() {
        Map<String, Object> actualResult = createActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult("k1", "v2", null),
                                new ExpectedResult("k2", "v2", null)),
                        ValidationType.ANY_MATCH);

        validator.validate(expectedResult, actualResult);
    }

    @Test
    void testValidateAnyMatchWhenNotValid() {
        Map<String, Object> actualResult = createActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult("k1", "v3", null),
                                new ExpectedResult("k2", "v3", null)),
                        ValidationType.ANY_MATCH);

        ValidationException e = assertThrows(ValidationException.class,
                () -> validator.validate(expectedResult, actualResult));

        assertEquals("Validation failed!\n" +
                "k1 is not valid. Expected: v3 Actual: v1\n" +
                "k2 is not valid. Expected: v3 Actual: v2\n", e.getMessage());
    }

    @Test
    void testValidateNoneMatchWhenNotValid() {
        Map<String, Object> actualResult = createActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult("k1", "v1", null),
                                new ExpectedResult("k2", "v2", null)),
                        ValidationType.NONE_MATCH);

        new ValidationDetails.Builder().withExpectedResult(expectedResult).build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> validator.validate(expectedResult, actualResult));

        assertEquals("Validation failed!\nk1 is not valid. Did not expect: v1 Actual: v1\n", e.getMessage());
    }

    @Test
    void testValidateComplexValueWhenNotValid() {
        Map<String, Object> actualResult = createMap("k1", createMap("k2", "v2"));

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Collections.singletonList(new ExpectedResult("k1",
                                Collections.singletonList(new ExpectedResult("k2", "v3", null)),
                                ValidationType.ALL_MATCH)),
                        ValidationType.ALL_MATCH);

        new ValidationDetails.Builder().withExpectedResult(expectedResult).build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> validator.validate(expectedResult, actualResult));

        assertEquals("Validation failed!\n" +
                "k1.k2 is not valid. Expected: v3 Actual: v2\n", e.getMessage());
    }

    private Map<String, Object> createActualResult() {
        return createMap(
                "k1", "v1",
                "k2", "v2"
        );
    }
}