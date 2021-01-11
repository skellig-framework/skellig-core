package org.skellig.teststep.reader.sts

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.URISyntaxException
import java.nio.file.Paths

@DisplayName("Read sts-file")
class StsFileParserTest {

    private var stsFileParser: StsFileParser = StsFileParser()

    @Test
    @DisplayName("When test step is simple with parameters, regex and functions")
    @Throws(URISyntaxException::class)
    fun testParseSimpleTestStep() {
        val filePath = Paths.get(javaClass.getResource("/simple-test-steps.std").toURI())

        val testSteps = stsFileParser.parse(filePath)

        Assertions.assertEquals(2, testSteps.size)
        val firstTestStep = testSteps[0]
        val secondTestStep = testSteps[1]
        Assertions.assertAll(
                { Assertions.assertEquals("Simple test step", firstTestStep["name"]) },
                { Assertions.assertEquals("POST", firstTestStep["method"]) },
                { Assertions.assertEquals("\${baseUrl}/a/b/c", firstTestStep["url"]) },
                { Assertions.assertEquals("v 1 2 3", getValueFromMap(firstTestStep, "payload", "json", "value")) },
                { Assertions.assertEquals("go", getValueFromMap(firstTestStep, "payload", "json", "command")) }
        )
        Assertions.assertAll(
                { Assertions.assertEquals("Send \\d{1} message (.*) from csv \\(test\\)", secondTestStep["name"]) },
                { Assertions.assertEquals("POST", secondTestStep["method"]) },
                { Assertions.assertEquals("/a/b/c", secondTestStep["url"]) },
                { Assertions.assertEquals("\${user}", getValueFromMap(secondTestStep, "auth", "username")) },
                { Assertions.assertEquals("\${password}\\_", getValueFromMap(secondTestStep, "auth", "password")) },
                {
                    Assertions.assertEquals("/resources/\"templates\"/msg_get(id).ftl",
                            getValueFromMap(secondTestStep, "body", "template", "file"))
                },
                {
                    Assertions.assertEquals("/resources/data/test1.csv",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "file"))
                },
                {
                    Assertions.assertEquals("TABLE",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "convert"))
                },
                {
                    Assertions.assertEquals("\\\ttest da\'ta wi\'th \'valid\' values",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "row", "name"))
                },
                {
                    Assertions.assertEquals("1",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "row", "group"))
                }
        )
    }

    @Test
    @DisplayName("When value has text enclosed in single quotes")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithQuotes() {
        val filePath = Paths.get(javaClass.getResource("/test-step-with-quotes.std").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertAll(
                { Assertions.assertEquals("Run command (.*)", firstTestStep["name"]) },
                {
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
        val filePath = Paths.get(javaClass.getResource("/test-step-with-validations.std").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertAll(
                { Assertions.assertEquals("Validate response", firstTestStep["name"]) },
                { Assertions.assertEquals("T1", getValueFromMap(firstTestStep, "validate", "fromTest")) },
                {
                    Assertions.assertTrue(
                            (getValueFromMap(firstTestStep, "validate", "contains_expected_values") as List<*>)
                                    .containsAll(listOf(
                                            "equals to something",
                                            "contains(success)",
                                            "contains(go go go)",
                                            "regex(.*get(id).*)")))
                },
                { Assertions.assertEquals("v1", getValueFromMap(firstTestStep, "validate", "has_fields", "f1")) },
                { Assertions.assertEquals("get(id) and more", getValueFromMap(firstTestStep, "validate", "has_fields", "json_path(f1.f2)")) }
        )
    }

    @Test
    @DisplayName("When test step has array of maps")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithArrayOfMaps() {
        val filePath = Paths.get(javaClass.getResource("/test-step-with-array-of-maps.std").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertAll(
                { Assertions.assertEquals("do something big", firstTestStep["name"]) },
                { Assertions.assertEquals(2, (getValueFromMap(firstTestStep, "data", "values") as List<*>).size) },
                { Assertions.assertEquals(2, (getValueFromMap(firstTestStep, "data", "values", 0) as Map<*, *>).size) },
                { Assertions.assertEquals("v1", getValueFromMap(firstTestStep, "data", "values", 0, "c1")) },
                { Assertions.assertEquals("v2", getValueFromMap(firstTestStep, "data", "values", 0, "c2")) },
                { Assertions.assertEquals(2, (getValueFromMap(firstTestStep, "data", "values", 1) as Map<*, *>).size) },
                { Assertions.assertEquals("v3", getValueFromMap(firstTestStep, "data", "values", 1, "c1")) },
                { Assertions.assertEquals("v4", getValueFromMap(firstTestStep, "data", "values", 1, "c2")) }
        )
    }

    @Test
    @DisplayName("When step is empty")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithEmptyStep() {
        val filePath = Paths.get(javaClass.getResource("/empty-step.std").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertEquals("Given something", firstTestStep["name"])
    }

    @Test
    @DisplayName("When test step has complex validation details")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithComplexValidation() {
        val filePath = Paths.get(javaClass.getResource("/test-step-with-complex-validations.std").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertAll(
                { Assertions.assertEquals("Validate response", firstTestStep["name"]) },
                { Assertions.assertEquals("T 1 2 3", getValueFromMap(firstTestStep, "validate", "fromTest")) },
                { Assertions.assertEquals("application/json", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "headers", "content-type")) },  // spaced inside the value must be preserved
                { Assertions.assertEquals("contains(fail  1 )", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "log", "none_match", 0)) },
                { Assertions.assertEquals("contains( error)", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "log", "none_match", 1)) },
                { Assertions.assertEquals("v3", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "body", "regex(.*f3=(\\w+).*)")) },
                { Assertions.assertEquals("v2", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "body", "json_path(f1.f3)")) },
                { Assertions.assertEquals("v1", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "body", "json_path(f1.f2)")) },
                { Assertions.assertEquals("200", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "status")) }
        )
    }

    @Test
    @DisplayName("When test step has validation details with array of maps and properties as indexes")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithArrayValidation() {
        val filePath = Paths.get(javaClass.getResource("/test-step-with-array-validations.std").toURI())

        val testSteps = stsFileParser.parse(filePath)

        val firstTestStep = testSteps[0]
        Assertions.assertAll(
                { Assertions.assertEquals("3", getValueFromMap(firstTestStep, "validate", "size")) },
                { Assertions.assertEquals("contains(v1)", getValueFromMap(firstTestStep, "validate", "records", "[0]")) },
                { Assertions.assertEquals("contains(v2)", getValueFromMap(firstTestStep, "validate", "records", "[1]")) },
                { Assertions.assertEquals("v1", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 0)) },
                { Assertions.assertEquals("v2", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 1)) },
                { Assertions.assertEquals("v3", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 2)) },
                { Assertions.assertEquals("v5", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c2", "any_match", 0)) },
                { Assertions.assertEquals("v6", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c2", "any_match", 1)) },
                { Assertions.assertEquals("v2", getValueFromMap(firstTestStep, "validate", "all_match", 1, "c1")) },
                { Assertions.assertEquals("v1", getValueFromMap(firstTestStep, "validate", "[0]", "c1")) }
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