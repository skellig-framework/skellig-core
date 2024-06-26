package org.skellig.teststep.reader.sts

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.reader.value.expression.*
import java.net.URL

@DisplayName("Read sts-file")
class StsReaderTest {

    private var stsReader = StsReader()

    @Test
    fun testReturnedDataPreserveOrder() {
        val filePath = getFileUrl("/simple-test-step.sts")

        val testSteps = stsReader.read(filePath.openStream())

        assertTrue(testSteps[0] is LinkedHashMap<*, *>)
    }

    @Test
    @DisplayName("When test step is simple with parameters, regex and functions")
    fun testParseSimpleTestStep() {
        val filePath = getFileUrl("/simple-test-steps.sts")

        val testSteps = stsReader.read(filePath.openStream())

        assertEquals(2, testSteps.size)
        val firstTestStep = testSteps[0]
        val secondTestStep = testSteps[1]
        assertAll({ assertEquals("Simple test step", getValueFromMap(firstTestStep, "name").toString()) },
            { assertEquals("POST", getValueFromMap(firstTestStep, "method").toString()) },
            { assertEquals("\${baseUrl}/a/b/c", firstTestStep[alphaNumOf("url")].toString()) },
            { assertEquals("v 1 2 3", getValueFromMap(firstTestStep, "payload", "json", "value").toString()) },
            { assertEquals("go", getValueFromMap(firstTestStep, "payload", "json", "command").toString()) },
            { assertEquals("\${a, \${b}.length}.size", getValueFromMap(firstTestStep, "payload", "json", "v2").toString()) },
            { assertEquals("\${a, \${b}.a.b}", getValueFromMap(firstTestStep, "payload", "json", "v3").toString()) })
        assertAll({ assertEquals(" Send \\d{1} message (.*) from csv \\(test\\)", getValueFromMap(secondTestStep, "name").toString()) },
            { assertEquals("POST", getValueFromMap(secondTestStep, "method").toString()) },
            { assertEquals("/a/ + b/ + c", getValueFromMap(secondTestStep, "url").toString()) },
            { assertEquals("\${user}", getValueFromMap(secondTestStep, "auth", "username").toString()) },
            { assertEquals("\${password} + \\_", getValueFromMap(secondTestStep, "auth", "password").toString()) },
            {
                assertEquals(
                    "//resources/\"templates\"/msg_get(id).ftl", getValueFromMap(secondTestStep, "body", "template", "file").toString()
                )
            },
            {
                assertEquals(
                    "/resources/data/test1.csv", getValueFromMap(secondTestStep, "body", "template", "csv", "file").toString()
                )
            },
            {
                assertEquals(
                    "TABLE", getValueFromMap(secondTestStep, "body", "template", "csv", "convert").toString()
                )
            },
            {
                assertEquals(
                    "\\\\\\ttest da + tawi + th  + valid +  values", getValueFromMap(secondTestStep, "body", "template", "csv", "row", "name").toString()
                )
            },
            {
                assertEquals(
                    "1", getValueFromMap(secondTestStep, "body", "template", "csv", "row", "group").toString()
                )
            })
    }

    @Test
    @DisplayName("When value has text enclosed in single quotes")
    fun testParseTestStepWithQuotes() {
        val filePath = getFileUrl("/test-step-with-quotes.sts")

        val testSteps = stsReader.read(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll({ assertEquals("Run command (.*)", getValueFromMap(firstTestStep, "name").toString()) }, {
            assertEquals(
                """{          command: ${'$'}1          value: v1        }""", getValueFromMap(firstTestStep, "payload").toString().replace(Regex("[\r\n]+"), "")
            )
        })
    }

    @Test
    @DisplayName("When test step has validations")
    fun testParseTestStepWithValidations() {
        val filePath = getFileUrl("/test-step-with-validations.sts")

        val testSteps = stsReader.read(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll({ assertEquals("Validate response", getValueFromMap(firstTestStep, "name").toString()) },
            { assertEquals("T1", getValueFromMap(firstTestStep, "validate", "fromTest").toString()) },
            {
                assertTrue(
                    (getValueFromMap(firstTestStep, "validate", "contains_expected_values") as ListValueExpression).value.map { it.toString() }.containsAll(
                        listOf(
                            "equals to something", "contains(success)", "contains(go go go)", "match(.*get(\"id\").*)"
                        )
                    )
                )
            },
            { assertEquals("v1", getValueFromMap(firstTestStep, "validate", "has_'fields'", "f1").toString()) },
            { assertEquals("get(id) + and more", getValueFromMap(firstTestStep, "validate", "has_'fields'", "json_path(f1.f2)").toString()) })
    }

    @Test
    @DisplayName("When test step has array of maps")
    fun testParseTestStepWithArrayOfMaps() {
        val filePath = getFileUrl("/test-step-with-array-of-maps.sts")

        val testSteps = stsReader.read(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll({ assertEquals("do something big", getValueFromMap(firstTestStep, "name").toString()) },
            { assertEquals(2, (getValueFromMap(firstTestStep, "data", "values") as ListValueExpression).value.size) },
            { assertEquals(2, (getValueFromMap(firstTestStep, "data", "values", 0) as MapValueExpression).value.size) },
            { assertEquals("v1", getValueFromMap(firstTestStep, "data", "values", 0, "c1").toString()) },
            { assertEquals("v2", getValueFromMap(firstTestStep, "data", "values", 0, "c2").toString()) },
            { assertEquals(2, (getValueFromMap(firstTestStep, "data", "values", 1) as MapValueExpression).value.size) },
            { assertEquals("v3", getValueFromMap(firstTestStep, "data", "values", 1, "c1").toString()) },
            { assertEquals("v4", getValueFromMap(firstTestStep, "data", "values", 1, "c2").toString()) },

            { assertEquals(0, (getValueFromMap(firstTestStep, "data", "values2") as ListValueExpression).value.size) }
        )
    }

    @Test
    @DisplayName("When step is empty")
    fun testParseTestStepWithEmptyStep() {
        val filePath = getFileUrl("/empty-step.sts")

        val testSteps = stsReader.read(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertEquals("Given something", getValueFromMap(firstTestStep, "name").toString())
    }

    @Test
    @DisplayName("When test step has complex validation details")
    fun testParseTestStepWithComplexValidation() {
        val filePath = getFileUrl("/test-step-with-complex-validations.sts")

        val testSteps = stsReader.read(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll({ assertEquals("Validate response", getValueFromMap(firstTestStep, "name").toString()) },
            { assertEquals("T 1 2 3", getValueFromMap(firstTestStep, "validate", "fromTest").toString()) },
            { assertEquals("application/json", getValueFromMap(firstTestStep, "validate", "values().anyMatch", "headers", "content-type").toString()) },  // spaced inside the value must be preserved
            { assertEquals("contains(fail  1 )", getValueFromMap(firstTestStep, "validate", "values().anyMatch", "log", "none_match", 0).toString()) },
            { assertEquals("contains( error)", getValueFromMap(firstTestStep, "validate", "values().anyMatch", "log", "none_match", 1).toString()) },
            { assertEquals("v3", getValueFromMap(firstTestStep, "validate", "values().anyMatch", "body", "regex(.*f3=(\\w+).*)").toString()) },
            { assertEquals("v2", getValueFromMap(firstTestStep, "validate", "values().anyMatch", "body", "json_path(f1.f3)").toString()) },
            { assertEquals("\${p1, \${p2, \${p3, 4}}}", getValueFromMap(firstTestStep, "validate", "values().anyMatch", "body", "json_path(f1.f2)").toString()) },
            { assertEquals("200", getValueFromMap(firstTestStep, "validate", "values().anyMatch", "status").toString()) })
    }

    @Test
    @DisplayName("When test step has validation details with array of maps and properties as indexes")
    fun testParseTestStepWithArrayValidation() {
        val filePath = getFileUrl("/test-step-with-array-validations.sts")

        val testSteps = stsReader.read(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll({ assertTrue((getValueFromMap(firstTestStep, "services") as ListValueExpression).value.map { it.toString() }.contains("srv1"), "Services field does not contain srv1") },
            { assertEquals("3", getValueFromMap(firstTestStep, "validate", "size").toString()) },
            { assertEquals("contains(v1)", getValueFromMap(firstTestStep, "validate", "records", "fromIndex(0)").toString()) },
            { assertEquals("contains(v2)", getValueFromMap(firstTestStep, "validate", "records", "fromIndex(1)").toString()) },
            { assertEquals("contains(v3)", getValueFromMap(firstTestStep, "validate", "records.fromIndex(2).fromIndex(1).toString()").toString()) },
            { assertEquals("v1", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 0).toString()) },
            { assertEquals("v2", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 1).toString()) },
            { assertEquals("v3", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c1", "none_match", 2).toString()) },
            { assertEquals("v5", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c2", "any_match", 0).toString()) },
            { assertEquals("v6", getValueFromMap(firstTestStep, "validate", "all_match", 0, "c2", "any_match", 1).toString()) },
            { assertEquals("v2", getValueFromMap(firstTestStep, "validate", "all_match", 1, "c1").toString()) },
            { assertEquals(" a#b  ", getValueFromMap(firstTestStep, "validate", "fromIndex(0)", stringOf(" c 1 ")).toString()) })
    }

    @Test
    @DisplayName("When test step has null values Then verify null is preserved")
    fun testParseTestStepWithNullValues() {
        val filePath = getFileUrl("/test-step-with-null.sts")

        val testSteps = stsReader.read(filePath.openStream())

        assertAll({ assertNull(getValueFromMap(testSteps[0], "payload", "a")) }, { assertEquals("", getValueFromMap(testSteps[0], "payload", "b").toString()) })
    }

    @Test
    @DisplayName("When test step has complex functions")
    fun testParseSimpleTestStepWithComplexFunctions() {
        val filePath = getFileUrl("/test-step-with-complex-functions.sts")

        val testSteps = stsReader.read(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll(
            { assertEquals("toJson", (getValueFromMap(firstTestStep, "payload") as FunctionCallExpression).name) },
            {
                assertEquals(
                    StringValueExpression("go"), ((getValueFromMap(firstTestStep, "payload") as FunctionCallExpression).args[0] as MapValueExpression).value[AlphanumericValueExpression("command")]
                )
            },
            {
                assertEquals(
                    StringValueExpression("v 1 2 3"), ((getValueFromMap(firstTestStep, "payload") as FunctionCallExpression).args[0] as MapValueExpression).value[AlphanumericValueExpression("value")]
                )
            },

            { assertEquals("toBytes", (getValueFromMap(firstTestStep, "request") as FunctionCallExpression).name) },
            { assertEquals("fromTemplate", ((getValueFromMap(firstTestStep, "request") as FunctionCallExpression).args[0] as FunctionCallExpression).name) },
            {
                assertEquals(
                    StringValueExpression("f1.ftl"), ((getValueFromMap(firstTestStep, "request") as FunctionCallExpression).args[0] as FunctionCallExpression).args[0]
                )
            },
            {
                assertEquals(
                    "toJson", ((((getValueFromMap(
                        firstTestStep, "request"
                    ) as FunctionCallExpression).args[0] as FunctionCallExpression).args[1] as MapValueExpression).value[AlphanumericValueExpression("rawData")] as FunctionCallExpression).name
                )
            },

            { assertEquals("toJson", (getValueFromMap(firstTestStep, "body") as FunctionCallExpression).name) },
            {
                assertEquals(
                    StringValueExpression("v1"),
                    (((getValueFromMap(firstTestStep, "body") as FunctionCallExpression).args[0] as ListValueExpression).value[0] as MapValueExpression).value[AlphanumericValueExpression("f1")]
                )
            },
        )
    }

    @Test
    @DisplayName("When test step has complex functions in validation")
    fun testParseSimpleTestStepWithComplexFunctionsInValidation() {
        val filePath = getFileUrl("/test-step-with-complex-functions-in-validation.sts")

        val testSteps = stsReader.read(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll(
            { assertEquals(NumberValueExpression("0"), getValueFromMap(firstTestStep, "validate", "toJson()", "a")) },
            { assertEquals(NumberValueExpression("10"), getValueFromMap(firstTestStep, "validate", "values(10)", 0, "a", "toJson()", "b")) },
            { assertEquals(NumberValueExpression("100"), getValueFromMap(firstTestStep, "validate", "values(10)", 1, "b", "toJson()", "c")) },
        )
    }

    @Test
    @DisplayName("When test step has inner references in properties")
    fun testParseTestStepWithInnerReferences() {
        val filePath = getFileUrl("/test-step-with-inner-references.sts")

        val testSteps = stsReader.read(filePath.openStream())

        val firstTestStep = testSteps[0]
        assertAll(
            { assertEquals(NumberValueExpression("1"), getValueFromMap(firstTestStep, "values", "i")) },
            { assertEquals(NumberValueExpression("2"), getValueFromMap(firstTestStep, "values", "n_ + \${i}")) },
            { assertEquals(NumberValueExpression("3"), getValueFromMap(firstTestStep, "values", "x_ + \${n_ + \${i}}")) },
        )
    }

    @Test
    @DisplayName("When test step has inner references in properties")
    fun testParseTestStepWithArrayArg() {
        val filePath = getFileUrl("/test-step-with-array-arg.sts")

        val testSteps = stsReader.read(filePath.openStream())

        val listValueExpression = (getValueFromMap(testSteps[0], "values", "i") as FunctionCallExpression).args[0] as ListValueExpression
        val listValueExpression2 =
            ((getValueFromMap(testSteps[0], "values") as MapValueExpression).value.keys.last()
                    as FunctionCallExpression).args[0] as ListValueExpression
        val listValueExpression3 = (getValueFromMap(testSteps[0], "values", "j") as FunctionCallExpression).args[0] as ListValueExpression
        assertAll(
            { assertEquals(NumberValueExpression("1"), listValueExpression.value[0]) },
            { assertEquals(NumberValueExpression("2"), listValueExpression.value[1]) },

            { assertEquals(3, listValueExpression2.value.size) },
            { assertEquals(AlphanumericValueExpression("a"), listValueExpression2.value[0]) },
            { assertEquals(AlphanumericValueExpression("b"), listValueExpression2.value[1]) },
            { assertEquals(AlphanumericValueExpression("c"), listValueExpression2.value[2]) },

            { assertEquals(0, listValueExpression3.value.size) }
        )
    }

    private fun getFileUrl(filePath: String): URL = javaClass.getResource(filePath)!!.toURI().toURL()

    private fun getValueFromMap(data: Map<ValueExpression, ValueExpression?>, vararg keys: Any): Any? {
        var value: Any? = data
        for (key in keys) {
            if (key is String) {
                if (value is Map<*, *> || value is MapValueExpression) {
                    value = if (value is MapValueExpression) value.value else value as Map<*, *>
                    val alphaNumKey = alphaNumOf(key)
                    value = if (value.containsKey(alphaNumKey)) value[alphaNumKey]
                    else value.keys.filter { it.toString() == key }.map { (value as Map<*, *>)[it] }.first()
                }
            } else if (key is Int) {
                value = (value as ListValueExpression).value[key]
            } else if (value is MapValueExpression) {
                value = value.value[key]
            }
        }
        return value
    }

    private fun alphaNumOf(value: String) = AlphanumericValueExpression(value)

    private fun stringOf(value: String) = StringValueExpression(value)
}
