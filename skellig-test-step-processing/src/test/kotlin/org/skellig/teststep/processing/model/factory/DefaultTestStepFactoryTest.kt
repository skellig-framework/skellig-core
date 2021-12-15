package org.skellig.teststep.processing.model.factory

import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.skellig.teststep.processing.converter.DefaultValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.utils.UnitTestUtils
import org.skellig.teststep.processing.valueextractor.DefaultValueExtractor
import java.util.*

@DisplayName("Create Test Step")
class DefaultTestStepFactoryTest {

    private var testStepFactory: TestStepFactory<DefaultTestStep>? = null
    private var testScenarioState: TestScenarioState? = null

    @BeforeEach
    fun setUp() {
        testScenarioState = Mockito.mock(TestScenarioState::class.java)
        testStepFactory = DefaultTestStepFactory.Builder()
                .withTestStepValueConverter(
                        TestStepFactoryValueConverter(
                            DefaultValueConverter.Builder()
                                .withTestScenarioState(testScenarioState)
                                .withTestStepValueExtractor(DefaultValueExtractor.Builder().build())
                                .build(),
                            null)
                )
                .build()
    }

    @Test
    @DisplayName("With variables And parameters And payload has reference to variable")
    fun testCreateTestStepWithVariablesAndAppliedParameters() {
        val generatedId = "0001"
        whenever(testScenarioState!!.get("gen_id")).thenReturn(generatedId)
        val rawTestStep = UnitTestUtils.createMap("variables",
                UnitTestUtils.createMap(
                        "id", "get(gen_id)",
                        "names", listOf("n1", "\${name:n2}"),
                        "amount", "\${amt:500}"
                ),
                "payload", "\${names}")
        val parameters = Collections.singletonMap("amt", "100")

        val testStep = testStepFactory!!.create("test 1", rawTestStep, parameters)

        Assertions.assertAll(
                { Assertions.assertTrue((testStep.testData as List<*>?)!!.containsAll(listOf("n1", "n2"))) },
                { Assertions.assertEquals(generatedId, testStep.variables!!["id"]) },
                { Assertions.assertTrue((testStep.variables!!["names"] as List<*>?)!!.containsAll(listOf("n1", "n2"))) },
                { Assertions.assertEquals("100", testStep.variables!!["amount"]) }
        )
    }

    @Test
    @DisplayName("When payload is Map with simple fields")
    fun testCreateTestStepWithPayloadAsMap() {
        val generatedId = "0001"
        whenever(testScenarioState!!.get("gen_id")).thenReturn(generatedId)

        val rawTestStep = UnitTestUtils.createMap(
                "variables",
                UnitTestUtils.createMap(
                        "id", generatedId,
                        "rows", listOf(
                        UnitTestUtils.createMap("c1", "v1", "c2", "v2"),
                        UnitTestUtils.createMap("c1", "v3", "c2", "v4")
                )
                ),
                "payload",
                UnitTestUtils.createMap(
                        "id", "\${id}",
                        "size", "2",
                        "rows", "\${rows}"
                )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())

        Assertions.assertAll(
                { Assertions.assertEquals(generatedId, (testStep.testData as Map<*, *>?)!!["id"]) },
                { Assertions.assertEquals("2", (testStep.testData as Map<*, *>?)!!["size"]) },
                { Assertions.assertEquals("v1", (((testStep.testData as Map<*, *>?)!!["rows"] as List<*>?)!![0] as Map<*, *>)["c1"]) },
                { Assertions.assertEquals("v2", (((testStep.testData as Map<*, *>?)!!["rows"] as List<*>?)!![0] as Map<*, *>)["c2"]) },
                { Assertions.assertEquals("v3", (((testStep.testData as Map<*, *>?)!!["rows"] as List<*>?)!![1] as Map<*, *>)["c1"]) },
                { Assertions.assertEquals("v4", (((testStep.testData as Map<*, *>?)!!["rows"] as List<*>?)!![1] as Map<*, *>)["c2"]) }
        )
    }

    @Test
    @DisplayName("With variables And validation details has reference to variable")
    fun testCreateTestStepWithVariablesAndValidationDetails() {
        val generatedId = "0001"
        whenever(testScenarioState!!.get("gen_id")).thenReturn(generatedId)

        val rawTestStep = UnitTestUtils.createMap("variables",
                UnitTestUtils.createMap(
                        "id", generatedId,
                        "row", UnitTestUtils.createMap("c1", "v1")
                ),
                "validate",
                UnitTestUtils.createMap(
                        "id", "\${id}",
                        "result", "\${row}"
                ))

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())
        val validationDetails = testStep.validationDetails

        Assertions.assertAll(
                { Assertions.assertEquals("result", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property) },
                { Assertions.assertEquals("c1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 0).property) },
                { Assertions.assertEquals("v1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 0).expectedResult) },
                { Assertions.assertEquals("id", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).property) },
                { Assertions.assertEquals(generatedId, UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).expectedResult) }
        )
    }

    @Test
    fun testWithNestedParameters() {
        val rawTestStep = UnitTestUtils.createMap("payload", "\${key_1 : \${key_2 : v3}}")

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf(Pair("key_2", "v2")))

        Assertions.assertEquals("v2", testStep.testData)
    }

    @Test
    fun testWithNestedParametersWithReferenceToParameters() {
        val rawTestStep = UnitTestUtils.createMap("payload", "\${key_1}")

        val testStep = testStepFactory!!.create("test 1", rawTestStep,
                mapOf(Pair("key_1", "\${key_2}"), Pair("key_2", "v2")))

        Assertions.assertEquals("v2", testStep.testData)
    }

    @Test
    @DisplayName("With test data as simple string")
    fun testCreateTestStepWithTestDataAsEmptyString() {
        val rawTestStep = UnitTestUtils.createMap("data", "")

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf())

        Assertions.assertEquals("", testStep.testData)
    }

    @Test
    @DisplayName("When field has reference to a parameter")
    fun testFieldWithReferenceToParameter() {
        val fieldName = "field"
        val value = "value"
        val rawTestStep = mapOf(Pair("data", mapOf(Pair("\${a}", value))))

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf(Pair("a", fieldName)))

        Assertions.assertEquals(value, (testStep.testData as Map<*, *>?)!![fieldName])
    }

    @Test
    @DisplayName("With validation of values with special characters")
    fun testCreateTestStepWithValidationOfValueWithSpecialChars() {
        val rawTestStep = mapOf(Pair("assert",
            mapOf(
                Pair("'f.1'.toString()", "'2.0'"),
                Pair("a.b.\"'c.d'\".'#[e]'", "'a.b'.toString().regex('([\\w.]{3})')")
            )))

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf())

        val validationDetails = testStep.validationDetails

        assertAll(
            { assertEquals("'f.1'.toString()", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property) },
            { assertEquals("2.0", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).expectedResult) },
            { assertEquals("a.b.\"'c.d'\".'#[e]'", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).property) },
            { assertEquals("a.b", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).expectedResult) },
        )
    }

    @Test
    @DisplayName("With test data as a content conversion function")
    fun testCreateTestStepWithTestDataAsContentConversionFunction() {
        val data = "something"
        val rawTestStep = UnitTestUtils.createMap("data",
                mapOf(Pair("toBytes", mapOf(Pair("value", data)))))

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf())

        Assertions.assertEquals(data, String(testStep.testData as ByteArray))
    }

    @Test
    @DisplayName("With variables has reference to other variables")
    fun testCreateTestStepWithVariablesReferenceToOtherVariables() {
        val generatedId = "0001"
        whenever(testScenarioState!!.get("gen_id")).thenReturn(generatedId)

        val rawTestStep = UnitTestUtils.createMap("variables",
            UnitTestUtils.createMap(
                "f1", "v1",
                "f2", "\${f1}"
            ))

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())
        val variables = testStep.variables!!

        Assertions.assertAll(
            { Assertions.assertEquals("v1", variables["f2"]) },
            { Assertions.assertEquals(variables["f1"], variables["f2"]) },
        )
    }
}