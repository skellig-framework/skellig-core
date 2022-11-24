package org.skellig.teststep.processing.model.factory

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ExpectedResult
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.utils.UnitTestUtils
import org.skellig.teststep.processing.value.chunk.RawValueProcessingVisitor
import org.skellig.teststep.processing.value.extractor.DefaultValueExtractor
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import java.util.*

@DisplayName("Create Test Step")
class DefaultTestStepFactoryTest {

    private var testStepFactory: TestStepFactory<DefaultTestStep>? = null
    private var testScenarioState: TestScenarioState? = null
    private val testStepRegistry = mock<TestStepRegistry>()

    @BeforeEach
    fun setUp() {
        testScenarioState = Mockito.mock(TestScenarioState::class.java)
        testStepFactory = DefaultTestStepFactory.Builder()
            .withTestStepValueConverter(
                TestStepFactoryValueConverter.Builder()
                    .withValueProcessingVisitor(
                        RawValueProcessingVisitor(
                            DefaultFunctionValueExecutor.Builder()
                                .withTestScenarioState(testScenarioState)
                                .withTestStepValueExtractor(DefaultValueExtractor.Builder().build())
                                .build(),
                            DefaultValueExtractor.Builder().build(),
                            mock(),
                            DefaultPropertyExtractor(null)
                        )
                    )
                    .build()
            )
            .withTestStepRegistry(testStepRegistry)
            .build()
    }

    @Test
    @DisplayName("With variables And parameters And payload has reference to variable")
    fun testCreateTestStepWithVariablesAndAppliedParameters() {
        val generatedId = "0001"
        whenever(testScenarioState!!.get("gen_id")).thenReturn(generatedId)
        val rawTestStep = UnitTestUtils.createMap(
            "variables",
            UnitTestUtils.createMap(
                "id", "get(gen_id)",
                "names", listOf("n1", "\${name:n2}"),
                "amount", "\${amt:500}"
            ),
            "payload", "\${names}"
        )
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
    @DisplayName("With parameters in name Then check they are collected corectly")
    fun testCreateTestStepWithParamsInName() {
        val rawTestStep = mapOf(
            Pair("name", "Book seats (.+) of the event\\s*(.*)"),
            Pair(
                "variables",
                mapOf(
                    Pair("seats", "\${1}"),
                    Pair("event", "\${2}")
                )
            )
        )
        val testStep = testStepFactory!!.create("Book seats s1 of the event", rawTestStep, emptyMap())

        Assertions.assertAll(
            { Assertions.assertEquals("s1", testStep.variables!!["seats"]) },
            { Assertions.assertNull(testStep.variables!!["event"]) },
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

        val rawTestStep = UnitTestUtils.createMap(
            "variables",
            UnitTestUtils.createMap(
                "id", generatedId,
                "row", UnitTestUtils.createMap("c1", "v1")
            ),
            "validate",
            UnitTestUtils.createMap(
                "id", "\${id}",
                "result", "\${row}"
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())
        val validationDetails = testStep.validationDetails

        // check that refs are not applied because they are processed when actual validation happens
        Assertions.assertAll(
            { Assertions.assertEquals("result", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property.toString()) },
            { Assertions.assertEquals("\${row}", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).expectedResult.toString()) },
            { Assertions.assertEquals("id", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).property.toString()) },
            { Assertions.assertEquals("\${id}", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).expectedResult.toString()) }
        )
    }

    @Test
    @DisplayName("With validation details having many xpaths Then check each is preserved")
    fun testCreateTestStepWithValidationDetailsOfManyXpaths() {
        val rawTestStep =  mapOf(
            Pair("validate",
            mapOf(
                Pair("local.toString()", mapOf(
                    Pair("xpath(/a/b)", "1"),
                    Pair("xpath(/c/d)", "2")
                ))
        )))

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())
        val validationDetails = testStep.validationDetails

        Assertions.assertAll(
            { Assertions.assertEquals("local.toString()", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property.toString()) },
            { Assertions.assertEquals("xpath(/a/b)", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 0).property.toString()) },
            { Assertions.assertEquals("xpath(/c/d)", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 1).property.toString()) },
        )
    }

    @Test
    fun testWithNestedParameters() {
        val rawTestStep = UnitTestUtils.createMap("payload", "\${key_1 : \${key_2 : v3}}")

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf(Pair("key_2", "v2")))

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
        val rawTestStep = mapOf(
            Pair(
                "assert",
                mapOf(
                    Pair("'f.1'.toString()", "'2.0'"),
                    Pair("a.b.'c.d'.'#[e]'", "'a.b'.toString().regex('([\\\\w.]{3})')")
                )
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf())

        val validationDetails = testStep.validationDetails

        assertAll(
            { assertEquals("f.1.toString()", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property.toString()) },
            { assertEquals("2.0", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).expectedResult.toString()) },
            { assertEquals("a.b.c.d.#[e]", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).property.toString()) },
            {
                assertEquals(
                    "a.b.toString().regex(([\\w.]{3}))",
                    UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).expectedResult.toString()
                )
            },
        )
    }

    @Test
    @DisplayName("With test data as a content conversion function")
    fun testCreateTestStepWithTestDataAsContentConversionFunction() {
        val data = "something"
        val rawTestStep = mapOf(Pair("data", "$data.toBytes()"))

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf())

        Assertions.assertEquals(data, String(testStep.testData as ByteArray))
    }

    @Test
    @DisplayName("With variables has reference to other variables")
    fun testCreateTestStepWithVariablesReferenceToOtherVariables() {
        val generatedId = "0001"
        whenever(testScenarioState!!.get("gen_id")).thenReturn(generatedId)

        val rawTestStep = UnitTestUtils.createMap(
            "variables",
            UnitTestUtils.createMap(
                "f1", "v1",
                "f2", "\${f1}",
                "f3", "\${f2}",
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())
        val variables = testStep.variables!!

        Assertions.assertAll(
            { Assertions.assertEquals("v1", variables["f2"]) },
            { Assertions.assertEquals(variables["f1"], variables["f2"]) },
            { Assertions.assertEquals(variables["f1"], variables["f3"]) },
        )
    }

    @Test
    @DisplayName("When has reference to parent test step Then check all data merged")
    fun testCreateTestStepWithRefToParent() {
        val testStepA = UnitTestUtils.createMap(
            "id", "stepA",
            "payload", "parent payload"
        )

        whenever(testStepRegistry.getById(testStepA["id"] as String)).thenReturn(testStepA)

        val rawTestStep = UnitTestUtils.createMap(
            "parent", testStepA["id"],
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf(Pair("1", "v2")))

        Assertions.assertAll(
            { Assertions.assertEquals(testStepA["id"], testStep.id) },  // id is replaced by the latest parent
            { Assertions.assertEquals(testStepA["payload"], testStep.testData) }
        )
    }

    @Test
    @DisplayName("When has reference to parent test steps Then check all data merged")
    fun testCreateTestStepWithRefToParents() {
        val testStepA = UnitTestUtils.createMap(
            "id", "stepA",
            "variables", UnitTestUtils.createMap("f1", "v1"),
            "payload", "to replace by child"
        )
        val testStepB = UnitTestUtils.createMap(
            "id", "stepB",
            "assert", UnitTestUtils.createMap("f4", "v4"),
        )
        whenever(testStepRegistry.getById(testStepA["id"] as String)).thenReturn(testStepA)
        whenever(testStepRegistry.getById(testStepB["id"] as String)).thenReturn(testStepB)

        val rawTestStep = UnitTestUtils.createMap(
            "parent", listOf(testStepA["id"], testStepB["id"]),
            "payload",
            UnitTestUtils.createMap(
                "new_f1", "\${f1}",  // this reference must be taken from testStepA vars
                "f2", "\${f2}",
                "f3", "something",
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf(Pair("f2", "v2")))

        Assertions.assertAll(
            { Assertions.assertEquals(testStepB["id"], testStep.id) },  // id is replaced by the latest parent
            { Assertions.assertEquals(testStepA["variables"], testStep.variables) },
            { Assertions.assertEquals("v1", (testStep.testData as Map<*, *>?)!!["new_f1"]) },
            { Assertions.assertEquals("v2", (testStep.testData as Map<*, *>?)!!["f2"]) },
            { Assertions.assertEquals("something", (testStep.testData as Map<*, *>?)!!["f3"]) },
            {
                Assertions.assertEquals(
                    "v4",
                    ((testStep.validationDetails?.expectedResult?.expectedResult as List<*>)[0] as ExpectedResult).expectedResult.toString()
                )
            }
        )
    }

    @Test
    @DisplayName("When value has if statement Then check it assigns value correctly")
    fun testCreateTestStepWithIfStatement() {
        val expectedValue = mapOf(Pair("a", 1), Pair("b", 2))
        val rawTestStep = mapOf(
            Pair(
                "variables",
                mapOf(
                    Pair("f1", "v1"),
                    Pair("f2", expectedValue),
                )
            ),
            Pair("payload", mapOf(Pair("c", "if(1==1,\${f2}, \${f1}")))
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap())

        Assertions.assertEquals(expectedValue, (testStep.testData as Map<*, *>?)!!["c"])
    }
}