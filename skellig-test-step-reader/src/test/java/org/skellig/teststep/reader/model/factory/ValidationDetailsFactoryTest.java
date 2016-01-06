package org.skellig.teststep.reader.model.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Create validation details")
class ValidationDetailsFactoryTest {

    private ValidationDetailsFactory validationDetailsFactory;

    @BeforeEach
    void setUp() {
        validationDetailsFactory = new ValidationDetailsFactory();
    }

    @Test
    @DisplayName("When has test id and grouped expected results")
    void test() {
        Map<String, Object> rawValidationDetails = new LinkedHashMap<>();
        rawValidationDetails.put("fromTest", "t1");
        rawValidationDetails.put("response has", createMap("f1", "v1", "f2", "v2"));
        rawValidationDetails.put("response also has", createMap("f1", "g1", "f2", "g2"));

        ValidationDetails validationDetails = validationDetailsFactory.create(createMap("validate", rawValidationDetails));

        assertAll(
                () -> assertEquals("t1", validationDetails.getTestStepId().orElse("")),
                () -> assertEquals(2, validationDetails.getValidationEntries().size())
        );
        ValidationDetails.ValidationEntry firstValidationEntry = validationDetails.getValidationEntries().get(0);
        ValidationDetails.ValidationEntry secondValidationEntry = validationDetails.getValidationEntries().get(1);

        assertAll(
                () -> assertEquals(ValidationType.TABLE, firstValidationEntry.getValidationType()),
                () -> assertEquals("v1", firstValidationEntry.getActualExpectedValues().get("f1")),
                () -> assertEquals("v2", firstValidationEntry.getActualExpectedValues().get("f2"))
        );

        assertAll(
                () -> assertEquals(ValidationType.TABLE, secondValidationEntry.getValidationType()),
                () -> assertEquals("g1", secondValidationEntry.getActualExpectedValues().get("f1")),
                () -> assertEquals("g2", secondValidationEntry.getActualExpectedValues().get("f2"))
        );
    }

    @Test
    @DisplayName("When has many default and grouped table validation types")
    void test2() {
        Map<String, Object> rawValidationDetails = new LinkedHashMap<>();
        rawValidationDetails.put("expected size", Collections.singletonList("size(1)"));
        rawValidationDetails.put("contains", Collections.singletonList("contains(v1)"));
        rawValidationDetails.put("contains rows",
                Arrays.asList(
                        createMap("c1", "v1", "c2", "v2"),
                        createMap("c1", "g1", "c2", "g2"))
        );

        ValidationDetails validationDetails = validationDetailsFactory.create(createMap("validate", rawValidationDetails));

        assertEquals(4, validationDetails.getValidationEntries().size());

        ValidationDetails.ValidationEntry firstValidationEntry = validationDetails.getValidationEntries().get(0);
        ValidationDetails.ValidationEntry secondValidationEntry = validationDetails.getValidationEntries().get(1);
        ValidationDetails.ValidationEntry thirdValidationEntry = validationDetails.getValidationEntries().get(2);
        ValidationDetails.ValidationEntry forthValidationEntry = validationDetails.getValidationEntries().get(3);

        assertAll(
                () -> assertEquals(ValidationType.DEFAULT, firstValidationEntry.getValidationType()),
                () -> assertEquals("size(1)", ((List) firstValidationEntry.getActualExpectedValues().get("expected size")).get(0))
        );

        assertAll(
                () -> assertEquals(ValidationType.DEFAULT, secondValidationEntry.getValidationType()),
                () -> assertEquals("contains(v1)", ((List) secondValidationEntry.getActualExpectedValues().get("contains")).get(0))
        );

        assertAll(
                () -> assertEquals(ValidationType.TABLE, thirdValidationEntry.getValidationType()),
                () -> assertEquals("v1", thirdValidationEntry.getActualExpectedValues().get("c1")),
                () -> assertEquals("v2", thirdValidationEntry.getActualExpectedValues().get("c2"))
        );

        assertAll(
                () -> assertEquals(ValidationType.TABLE, forthValidationEntry.getValidationType()),
                () -> assertEquals("g1", forthValidationEntry.getActualExpectedValues().get("c1")),
                () -> assertEquals("g2", forthValidationEntry.getActualExpectedValues().get("c2"))
        );
    }

    @Test
    @DisplayName("When has many table validation types")
    void test3() {
        ValidationDetails validationDetails = validationDetailsFactory.create(createMap("validate",
                Arrays.asList(
                        createMap("c1", "v1", "c2", "v2"),
                        createMap("c1", "g1", "c2", "g2"))));

        assertEquals(2, validationDetails.getValidationEntries().size());

        ValidationDetails.ValidationEntry firstValidationEntry = validationDetails.getValidationEntries().get(0);
        ValidationDetails.ValidationEntry secondValidationEntry = validationDetails.getValidationEntries().get(1);

        assertAll(
                () -> assertEquals(ValidationType.TABLE, firstValidationEntry.getValidationType()),
                () -> assertEquals("v1", firstValidationEntry.getActualExpectedValues().get("c1")),
                () -> assertEquals("v2", firstValidationEntry.getActualExpectedValues().get("c2"))
        );

        assertAll(
                () -> assertEquals(ValidationType.TABLE, secondValidationEntry.getValidationType()),
                () -> assertEquals("g1", secondValidationEntry.getActualExpectedValues().get("c1")),
                () -> assertEquals("g2", secondValidationEntry.getActualExpectedValues().get("c2"))
        );
    }

    @Test
    @DisplayName("When has one table validation type")
    void test4() {
        ValidationDetails validationDetails =
                validationDetailsFactory.create(createMap("validate", createMap("f1", "v1", "f2", "v2")));

        assertEquals(2, validationDetails.getValidationEntries().size());

        ValidationDetails.ValidationEntry firstValidationEntry = validationDetails.getValidationEntries().get(0);
        ValidationDetails.ValidationEntry secondValidationEntry = validationDetails.getValidationEntries().get(1);

        assertAll(
                () -> assertEquals(ValidationType.TABLE, firstValidationEntry.getValidationType()),
                () -> assertEquals("v1", firstValidationEntry.getActualExpectedValues().get("f1"))
        );

        assertAll(
                () -> assertEquals(ValidationType.TABLE, secondValidationEntry.getValidationType()),
                () -> assertEquals("v2", secondValidationEntry.getActualExpectedValues().get("f2"))
        );
    }

    @Test
    @DisplayName("When has one default validation type")
    void test5() {
        ValidationDetails validationDetails = validationDetailsFactory.create(createMap("validate",
                Arrays.asList("contains(a)", "contains(b)")));

        assertEquals(1, validationDetails.getValidationEntries().size());

        ValidationDetails.ValidationEntry firstValidationEntry = validationDetails.getValidationEntries().get(0);

        assertAll(
                () -> assertEquals(ValidationType.DEFAULT, firstValidationEntry.getValidationType()),
                () -> assertEquals("contains(a)", ((List) firstValidationEntry.getActualExpectedValues().get("")).get(0)),
                () -> assertEquals("contains(b)", ((List) firstValidationEntry.getActualExpectedValues().get("")).get(1))
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