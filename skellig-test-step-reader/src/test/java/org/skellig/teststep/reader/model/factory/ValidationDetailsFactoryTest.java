package org.skellig.teststep.reader.model.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.reader.model.ExpectedResult;
import org.skellig.teststep.reader.model.TestStep;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Create validation details")
class ValidationDetailsFactoryTest {

    private TestStepFactory validationDetailsFactory;

    @BeforeEach
    void setUp() {
        validationDetailsFactory =
                new DefaultTestStepFactory.Builder()
                        .withDefaultFactories()
                        .build();
    }

    @Test
    @DisplayName("When has test id and complex expected results with any_match and none_match")
    void test() {
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

        TestStep testStep = validationDetailsFactory.create(createMap("validate", rawValidationDetails));
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

                () -> assertEquals("status", extractExpectedValue(validationDetails.getExpectedResult(), 1, 3).getProperty()),
                () -> assertEquals("200", extractExpectedValue(validationDetails.getExpectedResult(), 1, 3).getExpectedResult())
        );
    }

    ExpectedResult extractExpectedValue(ExpectedResult expectedResult, int... indexPath) {
        for (int index : indexPath) {
            expectedResult = expectedResult.<List<ExpectedResult>>getExpectedResult().get(index);
        }
        return expectedResult;
    }


    private Map<String, Object> createMap(Object... params) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            map.put((String) params[i], params[i + 1]);
        }
        return map;
    }
}