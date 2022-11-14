package org.skellig.teststep.reader.sts

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.URISyntaxException

@DisplayName("Read sts-file")
class StsFileParserTest {

    private var stsFileParser = StsFileParser()

    @Test
    fun testReturnedDataPreserveOrder() {
        val filePath = javaClass.getResource("/simple-test-step.sts").toURI().toURL()

        val testSteps = stsFileParser.parse(filePath.openStream())

        assertTrue(testSteps[0] is LinkedHashMap)
    }

    @Test
    @DisplayName("When test step is simple with parameters, regex and functions")
    @Throws(URISyntaxException::class)
    fun testParseSimpleTestStep() {
        val filePath = javaClass.getResource("/simple-test-steps.sts").toURI().toURL()

        val testSteps = stsFileParser.parse(filePath.openStream())

        assertEquals(2, testSteps.size)
        val firstTestStep = testSteps[0]
        val secondTestStep = testSteps[1]
        assertAll(
                { assertEquals("Simple test step", firstTestStep["name"]) },
                { assertEquals("POST", firstTestStep["method"]) },
                { assertEquals("\${baseUrl}/a/b/c", firstTestStep["url"]) },
                { assertEquals("v 1 2 3", getValueFromMap(firstTestStep, "payload", "json", "value")) },
                { assertEquals("go", getValueFromMap(firstTestStep, "payload", "json", "command")) },
                { assertEquals("#[\${a : #[\${b}.length]}.size]", getValueFromMap(firstTestStep, "payload", "json", "v2")) },
                { assertEquals("\${a : \${b}.'a.b' }", getValueFromMap(firstTestStep, "payload", "json", "v3")) }
        )
        assertAll(
                { assertEquals("Send \\d{1} message (.*) from csv \\(test\\)", secondTestStep["name"]) },
                { assertEquals("POST", secondTestStep["method"]) },
                { assertEquals("/'a/'b/'c'", secondTestStep["url"]) },
                { assertEquals("\${user}", getValueFromMap(secondTestStep, "auth", "username")) },
                { assertEquals("\${password}\\_", getValueFromMap(secondTestStep, "auth", "password")) },
                {
                    assertEquals("//resources/\"templates\"/msg_get(id).ftl",
                            getValueFromMap(secondTestStep, "body", "template", "file"))
                },
                {
                    assertEquals("/resources/data/test1.csv",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "file"))
                },
                {
                    assertEquals("TABLE",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "convert"))
                },
                {
                    assertEquals("\\\ttest da'ta wi'th 'valid' values",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "row", "name"))
                },
                {
                    assertEquals("1",
                            getValueFromMap(secondTestStep, "body", "template", "csv", "row", "group"))
                }
        )
    }

    @Test
    @DisplayName("When value has text enclosed in single quotes")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithQuotes() {
        val filePath = javaClass.getResource("/test-step-with-quotes.sts").toURI().toURL()

        val testSteps = stsFileParser.parse(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll(
            { assertEquals("Run command (.*)", firstTestStep["name"]) },
            {
                assertEquals(
                    """{          command: ${'$'}1          value: v1        }""",
                    firstTestStep["payload"].toString().replace(Regex("[\r\n]+"), "")
                )
            }
        )
    }

    @Test
    @DisplayName("When test step has validations")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithValidations() {
        val filePath = javaClass.getResource("/test-step-with-validations.sts").toURI().toURL()

        val testSteps = stsFileParser.parse(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll(
                { assertEquals("Validate response", firstTestStep["name"]) },
                { assertEquals("T1", getValueFromMap(firstTestStep, "validate", "fromTest")) },
                {
                    assertTrue(
                            (getValueFromMap(firstTestStep, "validate", "contains_expected_values") as List<*>)
                                    .containsAll(listOf(
                                            "equals to something",
                                            "contains(success)",
                                            "contains(go go go)",
                                            "match('.*get(\\\"id\\\").*')")))
                },
                { assertEquals("v1", getValueFromMap(firstTestStep, "validate", "has_\\'fields\\'", "f1")) },
                { assertEquals("get(id) and more", getValueFromMap(firstTestStep, "validate", "has_\\'fields\\'", "json_path('f1.f2')")) }
        )
    }

    @Test
    @DisplayName("When test step has array of maps")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithArrayOfMaps() {
        val filePath = javaClass.getResource("/test-step-with-array-of-maps.sts").toURI().toURL()

        val testSteps = stsFileParser.parse(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll(
                { assertEquals("do something big", firstTestStep["name"]) },
                { assertEquals(2, (getValueFromMap(firstTestStep, "data", "values") as List<*>).size) },
                { assertEquals(2, (getValueFromMap(firstTestStep, "data", "values", 0) as Map<*, *>).size) },
                { assertEquals("v1", getValueFromMap(firstTestStep, "data", "values", 0, "c1")) },
                { assertEquals("v2", getValueFromMap(firstTestStep, "data", "values", 0, "c2")) },
                { assertEquals(2, (getValueFromMap(firstTestStep, "data", "values", 1) as Map<*, *>).size) },
                { assertEquals("v3", getValueFromMap(firstTestStep, "data", "values", 1, "c1")) },
                { assertEquals("v4", getValueFromMap(firstTestStep, "data", "values", 1, "c2")) }
        )
    }

    @Test
    @DisplayName("When step is empty")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithEmptyStep() {
        val filePath = javaClass.getResource("/empty-step.sts").toURI().toURL()

        val testSteps = stsFileParser.parse(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertEquals("Given something", firstTestStep["name"])
    }

    @Test
    @DisplayName("When test step has complex validation details")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithComplexValidation() {
        val filePath = javaClass.getResource("/test-step-with-complex-validations.sts").toURI().toURL()

        val testSteps = stsFileParser.parse(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll(
                { assertEquals("Validate response", firstTestStep["name"]) },
                { assertEquals("'T 1 2 3'", getValueFromMap(firstTestStep, "validate", "fromTest")) },
                { assertEquals("application/json", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "headers", "content-type")) },  // spaced inside the value must be preserved
                { assertEquals("contains(fail  1 )", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "log", "none_match", 0)) },
                { assertEquals("contains( error)", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "log", "none_match", 1)) },
                { assertEquals("v3", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "body", "regex('.*f3=(\\w+).*')")) },
                { assertEquals("v2", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "body", "json_path(f1.f3)")) },
                { assertEquals("\${p1 : \${p2: \${p3 : 4}}}", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "body", "json_path(f1.f2)")) },
                { assertEquals("200", getValueFromMap(firstTestStep, "validate", "any_match", "[srv1, srv2, srv3]", "status")) }
        )
    }

    @Test
    @DisplayName("When test step has validation details with array of maps and properties as indexes")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithArrayValidation() {
        val filePath = javaClass.getResource("/test-step-with-array-validations.sts").toURI().toURL()

        val testSteps = stsFileParser.parse(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll(
                { assertTrue((firstTestStep["services"] as Collection<*>).contains("srv1"), "Services field does not contain srv1") },
                { assertEquals("3", getValueFromMap(firstTestStep, "validate", "size")) },
                { assertEquals("contains(v1)", getValueFromMap(firstTestStep, "validate", "records", "fromIndex(0)")) },
                { assertEquals("contains(v2)", getValueFromMap(firstTestStep, "validate", "records", "fromIndex(1)")) },
                { assertEquals("contains(v3)", getValueFromMap(firstTestStep, "validate", "records.fromIndex(2).fromIndex(1).toString()")) },
                { assertEquals("v1", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 0)) },
                { assertEquals("v2", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 1)) },
                { assertEquals("v3", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 2)) },
                { assertEquals("v5", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c2", "any_match", 0)) },
                { assertEquals("v6", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c2", "any_match", 1)) },
                { assertEquals("v2", getValueFromMap(firstTestStep, "validate", "all_match", 1, "c1")) },
                { assertEquals(" a#b  ", getValueFromMap(firstTestStep, "validate", "fromIndex(0)", " c 1 ")) }
        )
    }

    @Test
    @DisplayName("When test step has null values Then verify null is preserved")
    @Throws(URISyntaxException::class)
    fun testParseTestStepWithNullValues() {
        val filePath = javaClass.getResource("/test-step-with-null.sts").toURI().toURL()

        val testSteps = stsFileParser.parse(filePath.openStream())

        assertAll(
                { assertNull(getValueFromMap(testSteps[0], "payload", "a")) },
                { assertEquals("", getValueFromMap(testSteps[0], "payload", "b")) }
        )
    }

    private fun getValueFromMap(data: Map<String, Any?>, vararg keys: Any): Any? {
        var value: Any? = data
        for (key in keys) {
            if (key is String) {
                if (value is Map<*, *>) {
                    value = value[key]
                }
            } else if (key is Int) {
                value = (value as List<*>)[key]
            }
        }
        return value
    }
}