package org.skellig.teststep.processing.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.exception.ValidationException;
import org.skellig.teststep.processing.model.ExpectedResult;
import org.skellig.teststep.processing.model.ValidationDetails;
import org.skellig.teststep.processing.model.ValidationType;
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator;
import org.skellig.teststep.processing.valueextractor.DefaultValueExtractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    void testValidateWhenValid() {
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
    void testValidateListOfContainsTextWhenValid() {
        String actualResult = "v1 v2";

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult(null, "contains(v1)", null),
                                new ExpectedResult(null, "contains(v2)", null)),
                        ValidationType.ALL_MATCH);

        validator.validate(expectedResult, actualResult);
    }

//    @Test
    void testValidateListOfContainsTextWhenActualListOfTextsAndNotValid() {
        List<String> actualResult = new ArrayList<>(Arrays.asList("v1 v2", "v3 v4"));

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult("size", 2, null),
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult(null,
                                                        Collections.singletonList(
                                                                new ExpectedResult(null, "contains(v1)", null)
                                                        ),
                                                        ValidationType.ALL_MATCH),
                                                new ExpectedResult(null,
                                                        Collections.singletonList(
                                                                new ExpectedResult(null, "contains(v5)", null)
                                                        ),
                                                        ValidationType.ALL_MATCH)
                                        ),
                                        ValidationType.ALL_MATCH)),
                        ValidationType.ALL_MATCH);


        ValidationException e = assertThrows(ValidationException.class,
                () -> validator.validate(expectedResult, actualResult));

        assertEquals("Validation failed!\n" +
                "result is not valid. Expected: contains(v5) Actual: v1 v2\n" +
                "result is not valid. Expected: contains(v5) Actual: v3 v4\n", e.getMessage());
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
    void testValidateWithArrayOfExpectedResults() {
        Map<String, Object> actualResult = createActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult("k1", "v1", null),
                                                new ExpectedResult("k2", "v2", null)
                                        ),
                                        ValidationType.ALL_MATCH),
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult("k1", "v3", null),
                                                new ExpectedResult("k2", "v4", null)
                                        ),
                                        ValidationType.ALL_MATCH)
                        ),
                        ValidationType.ANY_MATCH);

        validator.validate(expectedResult, Collections.singletonList(actualResult));
    }

//    @Test
    void testValidateWhenActualResultIsListOfMap() {
        Map<String, Object> actualResult1 = createActualResult();
        Map<String, Object> actualResult2 = createAnotherActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult("size", 2, null),
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult(null,
                                                        Arrays.asList(
                                                                new ExpectedResult("k1", "v1", null),
                                                                new ExpectedResult("k2", "v2", null)
                                                        ),
                                                        ValidationType.ALL_MATCH),
                                                new ExpectedResult(null,
                                                        Arrays.asList(
                                                                new ExpectedResult("k1", "v3", null),
                                                                new ExpectedResult("k2", "v4", null)
                                                        ),
                                                        ValidationType.ALL_MATCH)
                                        ),
                                        ValidationType.ALL_MATCH)
                        ),
                        ValidationType.ALL_MATCH);

        validator.validate(expectedResult, new ArrayList<>(Arrays.asList(actualResult1, actualResult2)));
    }

    @Test
    void testValidateWhenActualResultIsListOfMap2() {
        Map<String, Object> actualResult1 = createActualResult();
        Map<String, Object> actualResult2 = createAnotherActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult("k1", "v1", null),
                                                new ExpectedResult("k2", "v2", null)
                                        ),
                                        ValidationType.ALL_MATCH),
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult("k1", "v3", null),
                                                new ExpectedResult("k2", "v4", null)
                                        ),
                                        ValidationType.ALL_MATCH)
                        ),
                        ValidationType.ALL_MATCH);

        validator.validate(expectedResult, new ArrayList<>(Arrays.asList(actualResult1, actualResult2)));
    }

//    @Test
    void testValidateWhenActualResultIsListOfMapWhenNoMatch() {
        Map<String, Object> actualResult1 = createActualResult();
        Map<String, Object> actualResult2 = createAnotherActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult("size", 2, null),
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult(null,
                                                        Arrays.asList(
                                                                new ExpectedResult("k1", "v5", null),
                                                                new ExpectedResult("k2", "v6", null)
                                                        ),
                                                        ValidationType.ALL_MATCH),
                                                new ExpectedResult(null,
                                                        Arrays.asList(
                                                                new ExpectedResult("k1", "v7", null),
                                                                new ExpectedResult("k2", "v8", null)
                                                        ),
                                                        ValidationType.ALL_MATCH)
                                        ),
                                        ValidationType.NONE_MATCH)
                        ),
                        ValidationType.ALL_MATCH);

        validator.validate(expectedResult, new ArrayList<>(Arrays.asList(actualResult1, actualResult2)));
    }

    @Test
    void testValidateWhenActualResultIsListOfMapAndCheckNoMatch() {
        Map<String, Object> actualResult1 = createActualResult();
        Map<String, Object> actualResult2 = createAnotherActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Collections.singletonList(
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult("k1", "v5", null),
                                                new ExpectedResult("k2", "v6", null)
                                        ),
                                        ValidationType.NONE_MATCH)
                        ),
                        ValidationType.ANY_MATCH);

        new ValidationDetails.Builder().withExpectedResult(expectedResult).build();

        validator.validate(expectedResult, new Object[]{actualResult1, actualResult2});
    }

    @Test
    void testValidateWhenActualResultIsListOfMapAndCheckNoMatchWhenNotValid() {
        Map<String, Object> actualResult1 = createActualResult();
        Map<String, Object> actualResult2 = createAnotherActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Collections.singletonList(
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult("k1", "v1", null),
                                                new ExpectedResult("k2", "v2", null)
                                        ),
                                        ValidationType.NONE_MATCH)
                        ),
                        ValidationType.ALL_MATCH);

        new ValidationDetails.Builder().withExpectedResult(expectedResult).build();

        ValidationException e = assertThrows(ValidationException.class,
                () -> validator.validate(expectedResult, Arrays.asList(actualResult1, actualResult2)));

        assertEquals("Validation failed!\n" +
                ".k1 is not valid. Did not expect: v1 Actual: v1\n", e.getMessage());
    }

    @Test
    void testValidateWhenActualResultIsListOfMapWhenNoneMatch() {
        Map<String, Object> actualResult1 = createActualResult();
        Map<String, Object> actualResult2 = createAnotherActualResult();

        ExpectedResult expectedResult =
                new ExpectedResult("",
                        Arrays.asList(
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult("k1", "v3", null),
                                                new ExpectedResult("k2", "v4", null)
                                        ),
                                        ValidationType.ALL_MATCH),
                                new ExpectedResult(null,
                                        Arrays.asList(
                                                new ExpectedResult("k1", "v1", null),
                                                new ExpectedResult("k2", "v2", null)
                                        ),
                                        ValidationType.ALL_MATCH)
                        ),
                        ValidationType.NONE_MATCH);

        ValidationException e = assertThrows(ValidationException.class,
                () -> validator.validate(expectedResult, Arrays.asList(actualResult1, actualResult2)));
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

    private Map<String, Object> createAnotherActualResult() {
        return createMap(
                "k1", "v3",
                "k2", "v4"
        );
    }
}