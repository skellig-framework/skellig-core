package org.skellig.teststep.reader.model.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.reader.model.TestStep;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Create validation details")
class ValidationDetailsFactoryTest {

    private TestStepFactory validationDetailsFactory;

    @BeforeEach
    void setUp() {
        validationDetailsFactory =
                new DefaultTestStepFactory.Build()
                        .withDefaultFactories()
                        .build();
    }

    @Test
    @DisplayName("When has test id and grouped expected results")
    void test() {
        Map<String, Object> rawValidationDetails = new LinkedHashMap<>();
        rawValidationDetails.put("from_test", "t1");
        rawValidationDetails.put("response has", createMap("f1", "v1", "f2", "v2"));
        rawValidationDetails.put("response also has", createMap("f1", "g1", "f2", "g2"));

        TestStep testStep = validationDetailsFactory.create(createMap("validate", rawValidationDetails));
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertAll(
                () -> assertEquals("t1", validationDetails.getTestStepId().orElse("")),
                () -> assertEquals(2, validationDetails.getValidationEntries().size())
        );
        ValidationDetails.ExpectedTestResult firstExpectedTestResult = validationDetails.getValidationEntries().get(0);
        ValidationDetails.ExpectedTestResult secondExpectedTestResult = validationDetails.getValidationEntries().get(1);

        assertAll(
                () -> assertEquals(ValidationType.TABLE, firstExpectedTestResult.getValidationType()),
                () -> assertEquals("v1", firstExpectedTestResult.getActualExpectedValues().get("f1")),
                () -> assertEquals("v2", firstExpectedTestResult.getActualExpectedValues().get("f2"))
        );

        assertAll(
                () -> assertEquals(ValidationType.TABLE, secondExpectedTestResult.getValidationType()),
                () -> assertEquals("g1", secondExpectedTestResult.getActualExpectedValues().get("f1")),
                () -> assertEquals("g2", secondExpectedTestResult.getActualExpectedValues().get("f2"))
        );
    }

    @Test
    @DisplayName("When has many default and grouped table validation types")
    void test2() {
        Map<String, Object> rawValidationDetails = new LinkedHashMap<>();
        rawValidationDetails.put("from_test", "t1");
        rawValidationDetails.put("expected size", Collections.singletonList("size(1)"));
        rawValidationDetails.put("contains", Collections.singletonList("contains(v1)"));
        rawValidationDetails.put("contains rows",
                Arrays.asList(
                        createMap("c1", "v1", "c2", "v2"),
                        createMap("c1", "g1", "c2", "g2"))
        );

        TestStep testStep = validationDetailsFactory.create(createMap("validate", rawValidationDetails));
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertEquals(4, validationDetails.getValidationEntries().size());

        ValidationDetails.ExpectedTestResult firstExpectedTestResult = validationDetails.getValidationEntries().get(0);
        ValidationDetails.ExpectedTestResult secondExpectedTestResult = validationDetails.getValidationEntries().get(1);
        ValidationDetails.ExpectedTestResult thirdExpectedTestResult = validationDetails.getValidationEntries().get(2);
        ValidationDetails.ExpectedTestResult forthExpectedTestResult = validationDetails.getValidationEntries().get(3);

        assertAll(
                () -> assertEquals(ValidationType.DEFAULT, firstExpectedTestResult.getValidationType()),
                () -> assertEquals("size(1)", ((List) firstExpectedTestResult.getActualExpectedValues().get("expected size")).get(0))
        );

        assertAll(
                () -> assertEquals(ValidationType.DEFAULT, secondExpectedTestResult.getValidationType()),
                () -> assertEquals("contains(v1)", ((List) secondExpectedTestResult.getActualExpectedValues().get("contains")).get(0))
        );

        assertAll(
                () -> assertEquals(ValidationType.TABLE, thirdExpectedTestResult.getValidationType()),
                () -> assertEquals("v1", thirdExpectedTestResult.getActualExpectedValues().get("c1")),
                () -> assertEquals("v2", thirdExpectedTestResult.getActualExpectedValues().get("c2"))
        );

        assertAll(
                () -> assertEquals(ValidationType.TABLE, forthExpectedTestResult.getValidationType()),
                () -> assertEquals("g1", forthExpectedTestResult.getActualExpectedValues().get("c1")),
                () -> assertEquals("g2", forthExpectedTestResult.getActualExpectedValues().get("c2"))
        );
    }

    @Test
    @DisplayName("When has many table validation types")
    void test3() {
        TestStep testStep = validationDetailsFactory.create(
                createMap("table", "t_1",
                        "validate", Arrays.asList(
                                createMap("c1", "v1", "c2", "v2"),
                                createMap("c1", "g1", "c2", "g2"))));
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertEquals(2, validationDetails.getValidationEntries().size());

        ValidationDetails.ExpectedTestResult firstExpectedTestResult = validationDetails.getValidationEntries().get(0);
        ValidationDetails.ExpectedTestResult secondExpectedTestResult = validationDetails.getValidationEntries().get(1);

        assertAll(
                () -> assertEquals(ValidationType.TABLE, firstExpectedTestResult.getValidationType()),
                () -> assertEquals("v1", firstExpectedTestResult.getActualExpectedValues().get("c1")),
                () -> assertEquals("v2", firstExpectedTestResult.getActualExpectedValues().get("c2"))
        );

        assertAll(
                () -> assertEquals(ValidationType.TABLE, secondExpectedTestResult.getValidationType()),
                () -> assertEquals("g1", secondExpectedTestResult.getActualExpectedValues().get("c1")),
                () -> assertEquals("g2", secondExpectedTestResult.getActualExpectedValues().get("c2"))
        );
    }

    @Test
    @DisplayName("When has one table validation type")
    void test4() {
        TestStep testStep =
                validationDetailsFactory.create(createMap("table", "t_1", "validate", createMap("f1", "v1", "f2", "v2")));
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertEquals(2, validationDetails.getValidationEntries().size());

        ValidationDetails.ExpectedTestResult firstExpectedTestResult = validationDetails.getValidationEntries().get(0);
        ValidationDetails.ExpectedTestResult secondExpectedTestResult = validationDetails.getValidationEntries().get(1);

        assertAll(
                () -> assertEquals(ValidationType.TABLE, firstExpectedTestResult.getValidationType()),
                () -> assertEquals("v1", firstExpectedTestResult.getActualExpectedValues().get("f1"))
        );

        assertAll(
                () -> assertEquals(ValidationType.TABLE, secondExpectedTestResult.getValidationType()),
                () -> assertEquals("v2", secondExpectedTestResult.getActualExpectedValues().get("f2"))
        );
    }

    @Test
    @DisplayName("When has one default validation type")
    void test5() {
        Map<String, Object> rawValidationDetails = createMap("table", "t_1",
                "validate", Arrays.asList("contains(a)", "contains(b)"));

        TestStep testStep = validationDetailsFactory.create(rawValidationDetails);
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertEquals(1, validationDetails.getValidationEntries().size());

        ValidationDetails.ExpectedTestResult firstExpectedTestResult = validationDetails.getValidationEntries().get(0);

        assertAll(
                () -> assertEquals(ValidationType.DEFAULT, firstExpectedTestResult.getValidationType()),
                () -> assertEquals("contains(a)", ((List) firstExpectedTestResult.getActualExpectedValues().get("")).get(0)),
                () -> assertEquals("contains(b)", ((List) firstExpectedTestResult.getActualExpectedValues().get("")).get(1))
        );
    }

    private Map<String, Object> createMap(Object... params) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            map.put((String) params[i], params[i + 1]);
        }
        return map;
    }
}