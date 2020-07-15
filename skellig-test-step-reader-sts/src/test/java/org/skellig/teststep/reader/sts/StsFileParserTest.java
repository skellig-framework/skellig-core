package org.skellig.teststep.reader.sts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@DisplayName("Read sts-file")
class StsFileParserTest {

    private StsFileParser stsFileParser;

    @BeforeEach
    void setUp() {
        stsFileParser = new StsFileParser();
    }

    @Test
    @DisplayName("When test step is simple with parameters, regex and functions")
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
    @DisplayName("When value has text enclosed in single quotes")
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
    @DisplayName("When test step has validations")
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
                                        "equals to something",
                                        "contains(success)",
                                        "contains(go go go)",
                                        "regex(.*get(id).*)").collect(Collectors.toList()))),
                () -> assertEquals("v1", getValueFromMap(firstTestStep, "validate", "has_fields", "f1")),
                () -> assertEquals("get(id) and more", getValueFromMap(firstTestStep, "validate", "has_fields", "json_path(f1.f2)"))
        );
    }

    @Test
    @DisplayName("When test step has array of maps")
    void testParseTestStepWithArrayOfMaps() throws URISyntaxException {
        Path filePath = Paths.get(getClass().getResource("/test-step-with-array-of-maps.sts").toURI());

        List<Map<String, Object>> testSteps = stsFileParser.parse(filePath);

        Map<String, Object> firstTestStep = testSteps.get(0);
        assertAll(
                () -> assertEquals("do something big", firstTestStep.get("name")),
                () -> assertEquals(2, ((List) getValueFromMap(firstTestStep, "data", "values")).size()),
                () -> assertEquals(2, ((Map) getValueFromMap(firstTestStep, "data", "values", 0)).size()),
                () -> assertEquals("v1", getValueFromMap(firstTestStep, "data", "values", 0, "c1")),
                () -> assertEquals("v2", getValueFromMap(firstTestStep, "data", "values", 0, "c2")),
                () -> assertEquals(2, ((Map) getValueFromMap(firstTestStep, "data", "values", 1)).size()),
                () -> assertEquals("v3", getValueFromMap(firstTestStep, "data", "values", 1, "c1")),
                () -> assertEquals("v4", getValueFromMap(firstTestStep, "data", "values", 1, "c2"))
        );
    }

    @Test
    @DisplayName("When step is empty")
    void testParseTestStepWithEmptyStep() throws URISyntaxException {
        Path filePath = Paths.get(getClass().getResource("/empty-step.sts").toURI());

        List<Map<String, Object>> testSteps = stsFileParser.parse(filePath);

        Map<String, Object> firstTestStep = testSteps.get(0);
        assertEquals("Given something", firstTestStep.get("name"));
    }

    @Test
    @DisplayName("When test step has complex validation details")
    void testParseTestStepWithComplexValidation() throws URISyntaxException {
        Path filePath = Paths.get(getClass().getResource("/test-step-with-complex-validations.sts").toURI());

        List<Map<String, Object>> testSteps = stsFileParser.parse(filePath);

        Map<String, Object> firstTestStep = testSteps.get(0);
        assertAll(
                () -> assertEquals("T 1 2 3", getValueFromMap(firstTestStep, "validate", "from test")),
                () -> assertEquals("application/json", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "headers", "content-type")),
                // spaced inside the value must be preserved
                () -> assertEquals("contains(fail  1 )", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "log", "none_match", 0)),
                () -> assertEquals("contains( error)", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "log", "none_match", 1)),
                () -> assertEquals("v3", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "body", "regex(.*f3=(\\\\w+).*)")),
                () -> assertEquals("v2", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "body", "json_path(f1.f3)")),
                () -> assertEquals("v1", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "body", "json_path(f1.f2)")),
                () -> assertEquals("200", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "status"))
        );
    }

    @Test
    @DisplayName("When test step has validation details with array of maps and properties as indexes")
    void testParseTestStepWithArrayValidation() throws URISyntaxException {
        Path filePath = Paths.get(getClass().getResource("/test-step-with-array-validations.sts").toURI());

        List<Map<String, Object>> testSteps = stsFileParser.parse(filePath);

        Map<String, Object> firstTestStep = testSteps.get(0);
        assertAll(
                () -> assertEquals("3", getValueFromMap(firstTestStep, "validate", "size")),
                () -> assertEquals("contains(v1)", getValueFromMap(firstTestStep, "validate", "records", "[0]")),
                () -> assertEquals("contains(v2)", getValueFromMap(firstTestStep, "validate", "records", "[1]")),
                () -> assertEquals("v1", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 0)),
                () -> assertEquals("v2", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 1)),
                () -> assertEquals("v3", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 2)),

                () -> assertEquals("v5", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c2", "any_match", 0)),
                () -> assertEquals("v6", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c2", "any_match", 1)),

                () -> assertEquals("v2", getValueFromMap(firstTestStep, "validate", "all_match", 1, "c1")),

                () -> assertEquals("v1", getValueFromMap(firstTestStep, "validate", "[0]", "c1"))
        );
    }

    private Object getValueFromMap(Map<String, Object> data, Object... keys) {
        Object value = data;
        for (Object key : keys) {
            if (key instanceof String) {
                if (value instanceof Map) {
                    value = ((Map) value).get(key);
                }
            } else if (key instanceof Integer) {
                value = ((List) value).get((Integer) key);
            }
        }
        return value;
    }
}