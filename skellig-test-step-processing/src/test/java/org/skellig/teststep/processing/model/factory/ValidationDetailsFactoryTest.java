package org.skellig.teststep.processing.model.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.ValidationDetails;
import org.skellig.teststep.processing.model.ValidationType;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.skellig.teststep.processing.utils.UnitTestUtils.createMap;
import static org.skellig.teststep.processing.utils.UnitTestUtils.extractExpectedValue;

@DisplayName("Create validation details")
class ValidationDetailsFactoryTest {

    private TestStepFactory validationDetailsFactory;

    @BeforeEach
    void setUp() {
        validationDetailsFactory =
                new DefaultTestStepFactory.Builder()
                        .withTestStepValueConverter(v -> v)
                        .build();
    }

    @Test
    @DisplayName("When has few expected values")
    void testSimpleExpectedResult() {
        Map<String, Object> rawValidationDetails =
                createMap("status", "200", "log", "");

        TestStep testStep = createTestStepWithoutParameters(rawValidationDetails);
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertAll(
                () -> assertEquals(ValidationType.ALL_MATCH, validationDetails.getExpectedResult().getValidationType()),
                () -> assertEquals("log", extractExpectedValue(validationDetails.getExpectedResult(), 0).getProperty()),
                () -> assertEquals("", extractExpectedValue(validationDetails.getExpectedResult(), 0).getExpectedResult()),

                () -> assertEquals("status", extractExpectedValue(validationDetails.getExpectedResult(), 1).getProperty()),
                () -> assertEquals("200", extractExpectedValue(validationDetails.getExpectedResult(), 1).getExpectedResult())

        );
    }

    @Test
    @DisplayName("When has properties as index Then verify these properties are not changed")
    void testWithIndexedProperties() {
        Map<String, Object> rawValidationDetails =
                createMap("records",
                        createMap("[0]", "v1",
                                "[1]", createMap("a2", "v2")
                        ),
                        "records[2]", "v3");

        TestStep testStep = createTestStepWithoutParameters(rawValidationDetails);
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertAll(
                () -> assertEquals(ValidationType.ALL_MATCH, validationDetails.getExpectedResult().getValidationType()),
                () -> assertEquals("records", extractExpectedValue(validationDetails.getExpectedResult(), 0).getProperty()),
                () -> assertEquals("[1]", extractExpectedValue(validationDetails.getExpectedResult(), 0, 0).getProperty()),
                () -> assertEquals("a2", extractExpectedValue(validationDetails.getExpectedResult(), 0, 0, 0).getProperty()),
                () -> assertEquals("v2", extractExpectedValue(validationDetails.getExpectedResult(), 0, 0, 0).getExpectedResult()),

                () -> assertEquals("[0]", extractExpectedValue(validationDetails.getExpectedResult(), 0, 1).getProperty()),
                () -> assertEquals("v1", extractExpectedValue(validationDetails.getExpectedResult(), 0, 1).getExpectedResult()),

                () -> assertEquals("records[2]", extractExpectedValue(validationDetails.getExpectedResult(), 1).getProperty()),
                () -> assertEquals("v3", extractExpectedValue(validationDetails.getExpectedResult(), 1).getExpectedResult())
        );
    }

    @Test
    @DisplayName("When has array of expected values as Map ")
    void testWithArrayOfMaps() {
        Map<String, Object> rawValidationDetails =
                createMap("all_match",
                        Arrays.asList(
                                createMap("a1", "v1", "a2", "v2"),
                                createMap("b1", "v1", "b2", "v2")
                        ));

        TestStep testStep = createTestStepWithoutParameters(rawValidationDetails);
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertAll(
                () -> assertEquals(ValidationType.ALL_MATCH, validationDetails.getExpectedResult().getValidationType()),
                () -> assertEquals("", validationDetails.getExpectedResult().getProperty()),

                () -> assertNull(extractExpectedValue(validationDetails.getExpectedResult(), 0).getProperty()),
                () -> assertEquals("a1", extractExpectedValue(validationDetails.getExpectedResult(), 0, 0).getProperty()),
                () -> assertEquals("v1", extractExpectedValue(validationDetails.getExpectedResult(), 0, 0).getExpectedResult()),
                () -> assertEquals("a2", extractExpectedValue(validationDetails.getExpectedResult(), 0, 1).getProperty()),
                () -> assertEquals("v2", extractExpectedValue(validationDetails.getExpectedResult(), 0, 1).getExpectedResult()),

                () -> assertNull(extractExpectedValue(validationDetails.getExpectedResult(), 1).getProperty()),
                () -> assertEquals("b2", extractExpectedValue(validationDetails.getExpectedResult(), 1, 0).getProperty()),
                () -> assertEquals("v2", extractExpectedValue(validationDetails.getExpectedResult(), 1, 0).getExpectedResult()),
                () -> assertEquals("b1", extractExpectedValue(validationDetails.getExpectedResult(), 1, 1).getProperty()),
                () -> assertEquals("v1", extractExpectedValue(validationDetails.getExpectedResult(), 1, 1).getExpectedResult())

        );
    }

    /*
    * This test is to check the following structure from test step file:
    *
    * any_match {

      srv1 {
         status = 200
         body {
            json_path(f1.f2) = v1
            json_path(f1.f3) = v2
            regex(.*f3=(\\w+).*) = v3
         }
         headers {
            content-type = application/json
         }
         log {
            none_match [
              contains(fail)
              contains(error)
            ]
         }
      }

      srv2 {
         body {
           json_path(f1.f2) = v1
         }
         headers {
            content-type = application/json
         }
         log = contains(success)
      }
    }
    * */

    @Test
    @DisplayName("When has test id and complex expected results with any_match and none_match")
    void testComplexExpectedResult() {
        Map<String, Object> rawValidationDetails = new LinkedHashMap<>();
        rawValidationDetails.put("from_test", "t1");
        rawValidationDetails.put("any_match",
                createMap("srv1",
                        createMap("status", "200",
                                "body",
                                createMap("json_path(f1.f2)", "v1",
                                        "json_path(f1.f3)", "v2",
                                        "regex(.*f3=(\\\\w+).*)", "v3"),
                                "headers",
                                createMap("content-type", "application/json"),
                                "log",
                                createMap("none_match", Arrays.asList("contains(fail)", "contains(error)"))
                        ),
                        "srv2",
                        createMap("status", "200",
                                "body",
                                createMap("json_path(f1.f2)", "v1"),
                                "headers",
                                createMap("content-type", "application/json"),
                                "log", Arrays.asList("contains(success)")
                        )
                )
        );

        TestStep testStep = createTestStepWithoutParameters(rawValidationDetails);
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertAll(
                () -> assertEquals(ValidationType.ANY_MATCH, validationDetails.getExpectedResult().getValidationType()),
                () -> assertEquals("srv1", extractExpectedValue(validationDetails.getExpectedResult(), 0).getProperty()),
                () -> assertEquals(ValidationType.ALL_MATCH, extractExpectedValue(validationDetails.getExpectedResult(), 0).getValidationType()),
                () -> assertEquals("headers", extractExpectedValue(validationDetails.getExpectedResult(), 0, 0).getProperty()),
                () -> assertEquals(ValidationType.ALL_MATCH, extractExpectedValue(validationDetails.getExpectedResult(), 0, 0).getValidationType()),
                () -> assertEquals("content-type", extractExpectedValue(validationDetails.getExpectedResult(), 0, 0, 0).getProperty()),
                () -> assertEquals("application/json", extractExpectedValue(validationDetails.getExpectedResult(), 0, 0, 0).getExpectedResult()),

                () -> assertEquals("log", extractExpectedValue(validationDetails.getExpectedResult(), 0, 1).getProperty()),
                () -> assertEquals(ValidationType.NONE_MATCH, extractExpectedValue(validationDetails.getExpectedResult(), 0, 1).getValidationType()),
                () -> assertNull(extractExpectedValue(validationDetails.getExpectedResult(), 0, 1, 0).getProperty()),
                () -> assertEquals("contains(fail)", extractExpectedValue(validationDetails.getExpectedResult(), 0, 1, 0).getExpectedResult()),
                () -> assertNull(extractExpectedValue(validationDetails.getExpectedResult(), 0, 1, 1).getProperty()),
                () -> assertEquals("contains(error)", extractExpectedValue(validationDetails.getExpectedResult(), 0, 1, 1).getExpectedResult()),

                () -> assertEquals("body", extractExpectedValue(validationDetails.getExpectedResult(), 0, 2).getProperty()),
                () -> assertEquals("regex(.*f3=(\\\\w+).*)", extractExpectedValue(validationDetails.getExpectedResult(), 0, 2, 0).getProperty()),
                () -> assertEquals("v3", extractExpectedValue(validationDetails.getExpectedResult(), 0, 2, 0).getExpectedResult()),
                () -> assertEquals("json_path(f1.f3)", extractExpectedValue(validationDetails.getExpectedResult(), 0, 2, 1).getProperty()),
                () -> assertEquals("v2", extractExpectedValue(validationDetails.getExpectedResult(), 0, 2, 1).getExpectedResult()),
                () -> assertEquals("json_path(f1.f2)", extractExpectedValue(validationDetails.getExpectedResult(), 0, 2, 2).getProperty()),
                () -> assertEquals("v1", extractExpectedValue(validationDetails.getExpectedResult(), 0, 2, 2).getExpectedResult()),

                () -> assertEquals("status", extractExpectedValue(validationDetails.getExpectedResult(), 0, 3).getProperty()),
                () -> assertEquals("200", extractExpectedValue(validationDetails.getExpectedResult(), 0, 3).getExpectedResult()),


                () -> assertEquals("srv2", extractExpectedValue(validationDetails.getExpectedResult(), 1).getProperty()),
                () -> assertEquals(ValidationType.ALL_MATCH, extractExpectedValue(validationDetails.getExpectedResult(), 1).getValidationType()),

                () -> assertEquals("headers", extractExpectedValue(validationDetails.getExpectedResult(), 1, 0).getProperty()),
                () -> assertEquals(ValidationType.ALL_MATCH, extractExpectedValue(validationDetails.getExpectedResult(), 1, 0).getValidationType()),
                () -> assertEquals("content-type", extractExpectedValue(validationDetails.getExpectedResult(), 1, 0, 0).getProperty()),
                () -> assertEquals("application/json", extractExpectedValue(validationDetails.getExpectedResult(), 1, 0, 0).getExpectedResult()),

                () -> assertEquals("log", extractExpectedValue(validationDetails.getExpectedResult(), 1, 1).getProperty()),
                () -> assertNull(extractExpectedValue(validationDetails.getExpectedResult(), 1, 1, 0).getProperty()),
                () -> assertEquals("contains(success)", extractExpectedValue(validationDetails.getExpectedResult(), 1, 1, 0).getExpectedResult()),

                () -> assertEquals("body", extractExpectedValue(validationDetails.getExpectedResult(), 1, 2).getProperty()),
                () -> assertEquals(ValidationType.ALL_MATCH, extractExpectedValue(validationDetails.getExpectedResult(), 1, 2).getValidationType()),
                () -> assertEquals("json_path(f1.f2)", extractExpectedValue(validationDetails.getExpectedResult(), 1, 2, 0).getProperty()),
                () -> assertEquals("v1", extractExpectedValue(validationDetails.getExpectedResult(), 1, 2, 0).getExpectedResult()),


                () -> assertEquals("200", extractExpectedValue(validationDetails.getExpectedResult(), 1, 3).getExpectedResult())
        );
    }
    /*
    * This test is to check the following structure from test step file:
    *
    * any_match {
       [srv1, srv2, srv3] {
           status = 200
       }
       log = contains(success)
      }
    * */

    @Test
    @DisplayName("When has grouped properties Then verify these properties are split")
    void testWithGroupedProperties() {
        Map<String, Object> rawValidationDetails =
                createMap("any_match",
                        createMap("[srv1,srv2, srv3]", createMap("status", "200"),
                                "log", "contains(success)")
                );

        TestStep testStep = createTestStepWithoutParameters(rawValidationDetails);
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertAll(
                () -> assertEquals(ValidationType.ANY_MATCH, validationDetails.getExpectedResult().getValidationType()),
                () -> assertEquals("log", extractExpectedValue(validationDetails.getExpectedResult(), 0).getProperty()),
                () -> assertEquals("contains(success)", extractExpectedValue(validationDetails.getExpectedResult(), 0).getExpectedResult()),

                () -> assertEquals("srv3", extractExpectedValue(validationDetails.getExpectedResult(), 1).getProperty()),
                () -> assertEquals("status", extractExpectedValue(validationDetails.getExpectedResult(), 1, 0).getProperty()),
                () -> assertEquals("200", extractExpectedValue(validationDetails.getExpectedResult(), 1, 0).getExpectedResult()),

                () -> assertEquals("srv1", extractExpectedValue(validationDetails.getExpectedResult(), 2).getProperty()),
                () -> assertEquals("status", extractExpectedValue(validationDetails.getExpectedResult(), 2, 0).getProperty()),
                () -> assertEquals("200", extractExpectedValue(validationDetails.getExpectedResult(), 2, 0).getExpectedResult()),

                () -> assertEquals("srv2", extractExpectedValue(validationDetails.getExpectedResult(), 3).getProperty()),
                () -> assertEquals("status", extractExpectedValue(validationDetails.getExpectedResult(), 3, 0).getProperty()),
                () -> assertEquals("200", extractExpectedValue(validationDetails.getExpectedResult(), 3, 0).getExpectedResult())

        );
    }



    private TestStep createTestStepWithoutParameters(Map<String, Object> rawValidationDetails) {
        return validationDetailsFactory.create("step1", createMap("validate", rawValidationDetails), Collections.emptyMap());
    }
}