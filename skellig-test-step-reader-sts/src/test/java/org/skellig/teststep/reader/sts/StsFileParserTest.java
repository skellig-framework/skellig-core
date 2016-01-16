package org.skellig.teststep.reader.sts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StsFileParserTest {

    private StsFileParser stsFileParser;

    @BeforeEach
    void setUp() {
        stsFileParser = new StsFileParser();
    }

    @Test
    void testParseSimpleTestStep() throws URISyntaxException {
        Path filePath = Paths.get(getClass().getResource("/simple-test-steps.sts").toURI());

        List<Map<String, Object>> testSteps = stsFileParser.parse(filePath);

        assertEquals(2, testSteps.size());

        Map<String, Object> firstTestStep = testSteps.get(0);
        Map<String, Object> secondTestStep = testSteps.get(1);

        assertAll(
                () -> assertEquals("Simple test step", firstTestStep.get("name")),
                () -> assertEquals("POST", firstTestStep.get("method")),
                () -> assertEquals("${baseUrl}/a/b/c", firstTestStep.get("url")),
                () -> assertEquals("\"v 1 2 3\"", getValueFromMap(firstTestStep, "payload", "json", "value")),
                () -> assertEquals("\"go\"", getValueFromMap(firstTestStep, "payload", "json", "command"))
        );

        assertAll(
                () -> assertEquals("Send \\d{1} message (.*) from csv \\(test\\)", secondTestStep.get("name")),
                () -> assertEquals("POST", secondTestStep.get("method")),
                () -> assertEquals("/a/b/c", secondTestStep.get("url")),
                () -> assertEquals("${user}", getValueFromMap(secondTestStep, "auth", "username")),
                () -> assertEquals("${password}", getValueFromMap(secondTestStep, "auth", "password")),
                () -> assertEquals("/resources/templates/msg_get(id).ftl",
                        getValueFromMap(secondTestStep, "body", "template", "file")),
                () -> assertEquals("/resources/data/test1.csv",
                        getValueFromMap(secondTestStep, "body", "template", "csv", "file")),
                () -> assertEquals("TABLE",
                        getValueFromMap(secondTestStep, "body", "template", "csv", "convert")),
                () -> assertEquals("test data with valid values",
                        getValueFromMap(secondTestStep, "body", "template", "csv", "row", "name")),
                () -> assertEquals("1",
                        getValueFromMap(secondTestStep, "body", "template", "csv", "row", "group"))
        );
    }

    @Test
    void testParseTestStepWithQuotes() throws URISyntaxException {
        Path filePath = Paths.get(getClass().getResource("/test-step-with-quotes.sts").toURI());

        List<Map<String, Object>> testSteps = stsFileParser.parse(filePath);

        Map<String, Object> firstTestStep = testSteps.get(0);
        assertAll(
                () -> assertEquals("Run command (.*)", firstTestStep.get("name")),
                () -> assertEquals(
                        "{\n" +
                                "          command: $1\n" +
                                "          value: v1\n" +
                                "        }",
                        firstTestStep.get("payload"))
        );
    }

    @Test
    void testParseTestStepWithValidations() throws URISyntaxException {
        Path filePath = Paths.get(getClass().getResource("/test-step-with-validations.sts").toURI());

        List<Map<String, Object>> testSteps = stsFileParser.parse(filePath);

        Map<String, Object> firstTestStep = testSteps.get(0);
        assertAll(
                () -> assertEquals("Validate response", firstTestStep.get("name")),
                () -> assertEquals("T1", getValueFromMap(firstTestStep, "validate", "fromTest")),
                () -> assertTrue(
                        ((List) getValueFromMap(firstTestStep, "validate", "contains_expected_values"))
                                .containsAll(Stream.of(
                                        "contains(success)",
                                        "contains(go)",
                                        "regex(.*get(id).*)").collect(Collectors.toList()))),
                () -> assertEquals("v1", getValueFromMap(firstTestStep, "validate", "has_fields", "f1")),
                () -> assertEquals("get(id)", getValueFromMap(firstTestStep, "validate", "has_fields", "json_path(f1.f2)"))
        );
    }

    @Test
    void testParseTestStepWithArrayOfMaps() throws URISyntaxException {
        Path filePath = Paths.get(getClass().getResource("/test-step-with-array-of-maps.sts").toURI());

        List<Map<String, Object>> testSteps = stsFileParser.parse(filePath);

        Map<String, Object> firstTestStep = testSteps.get(0);
        assertAll(
                () -> assertEquals("do something big", firstTestStep.get("name")),
                () -> assertEquals(2, ((List)getValueFromMap(firstTestStep, "data", "values")).size()),
                () -> assertEquals("v1", ((Map)((List)getValueFromMap(firstTestStep, "data", "values")).get(0)).get("c1")),
                () -> assertEquals("v2", ((Map)((List)getValueFromMap(firstTestStep, "data", "values")).get(0)).get("c2")),
                () -> assertEquals("v3", ((Map)((List)getValueFromMap(firstTestStep, "data", "values")).get(1)).get("c1")),
                () -> assertEquals("v4", ((Map)((List)getValueFromMap(firstTestStep, "data", "values")).get(1)).get("c2"))
        );
    }

    private Object getValueFromMap(Map<String, Object> data, String... keys) {
        Object value = data;
        for (String key : keys) {
            if (value instanceof Map) {
                value = ((Map) value).get(key);
            }
        }
        return value;
    }
}