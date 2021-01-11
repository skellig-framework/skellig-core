package org.skellig.teststep.reader.sts

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import java.net.URISyntaxException
import java.nio.file.Paths

@DisplayName("Read sts-file")
class StsFileParserTest {

    private var stsFileParser: StsFileParser = StsFileParser()

    @Test
    @DisplayName("When test step is simple with parameters, regex and functions")
    @Throws(URISyntaxException::class)
    fun testParseSimpleTestStep() {
        val filePath = Paths.get(javaClass.getResource("/simple-test-steps.sts").toURI())

        val testSteps = stsFileParser.parse(filePath)

        Assertions.assertEquals(2, testSteps.size)
        val firstTestStep = testSteps[0]
        val secondTestStep = testSteps[1]
        Assertions.assertAll(
                Executable { Assertions.assertEquals("Simple test step", firstTestStep["name"]) },
                Executable { Assertions.assertEquals("POST", firstTestStep["method"]) },
                Executable { Assertions.assertEquals("\${baseUrl}/a/b/c", firstTestStep["url"]) },
                Executable { Assertions.assertEquals("\"v 1 2 3\"", getValueFromMap(firstTestStep, "payload", "json", "value")) },
                Executable { Assertions.assertEquals("\"go\"", getValueFromMap(firstTestStep, "payload", "json", "command")) }
        )
        Assertions.assertAll(
                Executable { Assertions.assertEquals("Send \\d{1} message (.*) from csv \\(test\\)", secondTestStep["name"]) },
                Executable { Assertions.assertEquals("POST", secondTestStep["method"]) },
                Executable { Assertions.assertEquals("/a/b/c", secondTestStep["url"]) },
                Executable { Assertions.assertEquals("\${user}", getValueFromMap(secondTestStep, "auth", "username")) },
                Executable { Assertions.assertEquals("\${password}", getValueFromMap(secondTestStep, "auth", "password")) },
                Executable {
                    Assertions.assertEquals("/resources/templates/msg_get(id).ftl",
                            getValueFromMap(secondTestStep, "body", "template", "file"))
                },
                Executable {
                    Assertions.assertEquals("/resources/data/test1.csv",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "file"))
                },
                Executable {
                    Assertions.assertEquals("TABLE",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "convert"))
                },
                Executable {
                    Assertions.assertEquals("test data with valid values",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "row", "name"))
                },
                Executable {
                    Assertions.assertEquals("1",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "row", "group"))
                }
        )
    }

    @Test
    @DisplayName("When value has text enclosed in single quotes")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithQuotes() {
        val filePath = Paths.get(javaClass.getResource("/test-step-with-quotes.sts").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertAll(
                Executable { Assertions.assertEquals("Run command (.*)", firstTestStep["name"]) },
                Executable {
                    Assertions.assertEquals(
                            """{
          command: $1
          value: v1
        }""",
                            firstTestStep["payload"])
                }
        )
    }

    @Test
    @DisplayName("When test step has validations")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithValidations() {
        val filePath = Paths.get(javaClass.getResource("/test-step-with-validations.sts").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertAll(
                Executable { Assertions.assertEquals("Validate response", firstTestStep["name"]) },
                Executable { Assertions.assertEquals("T1", getValueFromMap(firstTestStep, "validate", "fromTest")) },
                Executable {
                    Assertions.assertTrue(
                            (getValueFromMap(firstTestStep, "validate", "contains_expected_values") as List<*>)
                                    .containsAll(listOf(
                                            "equals to something",
                                            "contains(success)",
                                            "contains(go go go)",
                                            "regex(.*get(id).*)")))
                },
                Executable { Assertions.assertEquals("v1", getValueFromMap(firstTestStep, "validate", "has_fields", "f1")) },
                Executable { Assertions.assertEquals("get(id) and more", getValueFromMap(firstTestStep, "validate", "has_fields", "json_path(f1.f2)")) }
        )
    }

    @Test
    @DisplayName("When test step has array of maps")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithArrayOfMaps() {
        val filePath = Paths.get(javaClass.getResource("/test-step-with-array-of-maps.sts").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertAll(
                Executable { Assertions.assertEquals("do something big", firstTestStep["name"]) },
                Executable { Assertions.assertEquals(2, (getValueFromMap(firstTestStep, "data", "values") as List<*>).size) },
                Executable { Assertions.assertEquals(2, (getValueFromMap(firstTestStep, "data", "values", 0) as Map<*, *>).size) },
                Executable { Assertions.assertEquals("v1", getValueFromMap(firstTestStep, "data", "values", 0, "c1")) },
                Executable { Assertions.assertEquals("v2", getValueFromMap(firstTestStep, "data", "values", 0, "c2")) },
                Executable { Assertions.assertEquals(2, (getValueFromMap(firstTestStep, "data", "values", 1) as Map<*, *>).size) },
                Executable { Assertions.assertEquals("v3", getValueFromMap(firstTestStep, "data", "values", 1, "c1")) },
                Executable { Assertions.assertEquals("v4", getValueFromMap(firstTestStep, "data", "values", 1, "c2")) }
        )
    }

    @Test
    @DisplayName("When step is empty")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithEmptyStep() {
        val filePath = Paths.get(javaClass.getResource("/empty-step.sts").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertEquals("Given something", firstTestStep["name"])
    }

    @Test
    @DisplayName("When test step has complex validation details")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithComplexValidation() {
        val filePath = Paths.get(javaClass.getResource("/test-step-with-complex-validations.sts").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertAll(
                Executable { Assertions.assertEquals("T 1 2 3", getValueFromMap(firstTestStep, "validate", "from test")) },
                Executable { Assertions.assertEquals("application/json", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "headers", "content-type")) },  // spaced inside the value must be preserved
                Executable { Assertions.assertEquals("contains(fail  1 )", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "log", "none_match", 0)) },
                Executable { Assertions.assertEquals("contains( error)", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "log", "none_match", 1)) },
                Executable { Assertions.assertEquals("v3", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "body", "regex(.*f3=(\\\\w+).*)")) },
                Executable { Assertions.assertEquals("v2", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "body", "json_path(f1.f3)")) },
                Executable { Assertions.assertEquals("v1", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "body", "json_path(f1.f2)")) },
                Executable { Assertions.assertEquals("200", getValueFromMap(firstTestStep, "validate", "any match", "[srv1, srv2, srv3]", "status")) }
        )
    }

    @Test
    @DisplayName("When test step has validation details with array of maps and properties as indexes")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithArrayValidation() {
        val filePath = Paths.get(javaClass.getResource("/test-step-with-array-validations.sts").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertAll(
                Executable { Assertions.assertEquals("3", getValueFromMap(firstTestStep, "validate", "size")) },
                Executable { Assertions.assertEquals("contains(v1)", getValueFromMap(firstTestStep, "validate", "records", "[0]")) },
                Executable { Assertions.assertEquals("contains(v2)", getValueFromMap(firstTestStep, "validate", "records", "[1]")) },
                Executable { Assertions.assertEquals("v1", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 0)) },
                Executable { Assertions.assertEquals("v2", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 1)) },
                Executable { Assertions.assertEquals("v3", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 2)) },
                Executable { Assertions.assertEquals("v5", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c2", "any_match", 0)) },
                Executable { Assertions.assertEquals("v6", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c2", "any_match", 1)) },
                Executable { Assertions.assertEquals("v2", getValueFromMap(firstTestStep, "validate", "all_match", 1, "c1")) },
                Executable { Assertions.assertEquals("v1", getValueFromMap(firstTestStep, "validate", "[0]", "c1")) }
        )
    }

    private fun getValueFromMap(data: Map<String, Any?>, vararg keys: Any): Any {
        var value: Any = data
        for (key in keys) {
            if (key is String) {
                if (value is Map<*, *>) {
                    value = value[key]!!
                }
            } else if (key is Int) {
                value = (value as List<*>)[key]!!
            }
        }
        return value
    }
}