package org.skellig.teststep.processing.model.factory

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.GroupedValidationNode
import org.skellig.teststep.processing.model.PairValidationNode
import org.skellig.teststep.processing.model.ValidationNodes
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.ValueExpressionContextFactoryTest
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.value.expression.*
import java.math.BigDecimal
import java.util.*

@DisplayName("Create Test Step")
class DefaultTestStepFactoryTest {

    private var testStepFactory: TestStepFactory<DefaultTestStep>? = null
    private var testScenarioState = mock<TestScenarioState>()
    private val testStepRegistry = mock<TestStepRegistry>()

    @BeforeEach
    fun setUp() {
        testStepFactory = DefaultTestStepFactory.Builder()
            .withValueExpressionContextFactory(
                ValueExpressionContextFactory(
                    DefaultFunctionValueExecutor.Builder()
                        .withTestScenarioState(testScenarioState)
                        .withClassLoader(ValueExpressionContextFactoryTest::class.java.classLoader)
                        .build(),
                    DefaultPropertyExtractor(null)
                )
            )
            .withTestStepRegistry(testStepRegistry)
            .build()
    }

    @Test
    @DisplayName("With values And parameters And payload has reference to variable")
    fun testCreateTestStepWithValuesAndAppliedParameters() {
        val generatedId = "0001"
        whenever(testScenarioState.get("gen_id")).thenReturn(generatedId)
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("values"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("id"), FunctionCallExpression("get", arrayOf(AlphanumericValueExpression("gen_id")))),
                        Pair(AlphanumericValueExpression("names"), ListValueExpression(listOf(AlphanumericValueExpression("n1"), PropertyValueExpression("name", AlphanumericValueExpression("n2"))))),
                        Pair(
                            AlphanumericValueExpression("amount"), PropertyValueExpression("amt", NumberValueExpression("500"))
                        )
                    )
                )
            ),
            Pair(AlphanumericValueExpression("payload"), PropertyValueExpression("names", null))
        )
        val parameters = Collections.singletonMap("amt", "100")

        val testStep = testStepFactory!!.create("test 1", rawTestStep, parameters)

        assertAll(
            { assertTrue((testStep.testData as List<*>?)!!.containsAll(listOf("n1", "n2"))) },
            { assertEquals(generatedId, testStep.values!!["id"]) },
            { assertTrue((testStep.values!!["names"] as List<*>?)!!.containsAll(listOf("n1", "n2"))) },
            { assertEquals("100", testStep.values!!["amount"]) }
        )
    }

    @Test
    @DisplayName("With parameters in name Then check they are collected correctly")
    fun testCreateTestStepWithParamsInName() {
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(AlphanumericValueExpression("name"), StringValueExpression("Book seats (.+) of the event\\s*(.*)")),
            Pair(
                AlphanumericValueExpression("values"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("seats"), PropertyValueExpression("1", null)),
                        Pair(AlphanumericValueExpression("event"), PropertyValueExpression("2", null))
                    )
                )
            )
        )
        val testStep = testStepFactory!!.create("Book seats s1 of the event", rawTestStep, emptyMap())

        assertAll(
            { assertEquals("s1", testStep.values!!["seats"]) },
            { assertNull(testStep.values!!["event"]) },
        )
    }

    @Test
    @DisplayName("When payload is Map with simple fields")
    fun testCreateTestStepWithPayloadAsMap() {
        val generatedId = "0001"
        whenever(testScenarioState.get("gen_id")).thenReturn(generatedId)

        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("values"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("id"), AlphanumericValueExpression(generatedId)),
                        Pair(
                            AlphanumericValueExpression("rows"), ListValueExpression(
                                listOf(
                                    MapValueExpression(
                                        mapOf(
                                            Pair(AlphanumericValueExpression("c1"), AlphanumericValueExpression("v1")),
                                            Pair(AlphanumericValueExpression("c2"), AlphanumericValueExpression("v2"))
                                        )
                                    ),
                                    MapValueExpression(
                                        mapOf(
                                            Pair(AlphanumericValueExpression("c1"), AlphanumericValueExpression("v3")),
                                            Pair(AlphanumericValueExpression("c2"), AlphanumericValueExpression("v4"))
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            Pair(
                AlphanumericValueExpression("payload"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("id"), PropertyValueExpression("id", null)),
                        Pair(AlphanumericValueExpression("size"), NumberValueExpression("2")),
                        Pair(AlphanumericValueExpression("rows"), PropertyValueExpression("rows", null))
                    )
                )
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())

        assertAll(
            { assertEquals(generatedId, (testStep.testData as Map<*, *>?)!!["id"]) },
            { assertEquals(BigDecimal("2"), (testStep.testData as Map<*, *>?)!!["size"]) },
            { assertEquals("v1", (((testStep.testData as Map<*, *>?)!!["rows"] as List<*>?)!![0] as Map<*, *>)["c1"]) },
            { assertEquals("v2", (((testStep.testData as Map<*, *>?)!!["rows"] as List<*>?)!![0] as Map<*, *>)["c2"]) },
            { assertEquals("v3", (((testStep.testData as Map<*, *>?)!!["rows"] as List<*>?)!![1] as Map<*, *>)["c1"]) },
            { assertEquals("v4", (((testStep.testData as Map<*, *>?)!!["rows"] as List<*>?)!![1] as Map<*, *>)["c2"]) }
        )
    }

    @Test
    @DisplayName("With values And validation details has reference to variable")
    fun testCreateTestStepWithValuesAndValidationDetails() {
        val generatedId = "0001"
        whenever(testScenarioState.get("gen_id")).thenReturn(generatedId)

        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("values"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("id"), AlphanumericValueExpression(generatedId)),
                        Pair(
                            AlphanumericValueExpression("row"),
                            MapValueExpression(mapOf(Pair(AlphanumericValueExpression("c1"), AlphanumericValueExpression("v1"))))
                        )
                    )
                )
            ),
            Pair(
                AlphanumericValueExpression("validate"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("id"), PropertyValueExpression("id", null)),
                        Pair(AlphanumericValueExpression("result"), PropertyValueExpression("row", null))
                    )
                )
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())
        val validationDetails = (testStep.validationDetails as ValidationNodes).nodes

        // check that refs are not applied because they are processed when actual validation happens
        assertAll(
            { assertEquals(AlphanumericValueExpression("id"), (validationDetails[0] as PairValidationNode).actual) },
            { assertEquals(PropertyValueExpression("id", null), (validationDetails[0] as PairValidationNode).expected) },
            { assertEquals(AlphanumericValueExpression("result"), (validationDetails[1] as PairValidationNode).actual) },
            { assertEquals(PropertyValueExpression("row", null), (validationDetails[1] as PairValidationNode).expected) },
        )
    }

    @Test
    @DisplayName("With validation details having many xpaths Then check each is preserved")
    fun testCreateTestStepWithValidationDetailsOfManyXpaths() {
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("validate"),
                MapValueExpression(
                    mapOf(
                        Pair(
                            CallChainExpression(listOf(AlphanumericValueExpression("local"), FunctionCallExpression("toString", emptyArray()))),
                            MapValueExpression(
                                mapOf(
                                    Pair(FunctionCallExpression("xpath", arrayOf(StringValueExpression("/a/b"))), NumberValueExpression("1")),
                                    Pair(FunctionCallExpression("xpath", arrayOf(StringValueExpression("/c/d"))), NumberValueExpression("2"))
                                )
                            )
                        )
                    )
                )
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())
        val validationDetails = (testStep.validationDetails as ValidationNodes).nodes

        assertAll(
            { assertEquals("local.toString()", (validationDetails[0] as GroupedValidationNode).actual.toString()) },
            { assertEquals("xpath(/a/b)", (((validationDetails[0] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).actual.toString()) },
            { assertEquals("xpath(/c/d)", (((validationDetails[0] as GroupedValidationNode).items as ValidationNodes).nodes[1] as PairValidationNode).actual.toString()) },
        )
    }

    @Test
    fun testWithNestedParameters() {
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("payload"),
                PropertyValueExpression("key_1", PropertyValueExpression("key_2", AlphanumericValueExpression("v3")))
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf(Pair("key_2", "v2")))

        assertEquals("v2", testStep.testData)
    }

    @Test
    @DisplayName("With test data as simple string")
    fun testCreateTestStepWithTestDataAsEmptyString() {
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(Pair(AlphanumericValueExpression("data"), StringValueExpression("")))

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf())

        assertEquals("", testStep.testData)
    }

    @Test
    @DisplayName("When field has reference to a parameter")
    fun testFieldWithReferenceToParameter() {
        val fieldName = "field"
        val value = "value"
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("data"),
                MapValueExpression(mapOf(Pair(PropertyValueExpression("a", null), AlphanumericValueExpression(value))))
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf(Pair("a", fieldName)))

        assertEquals(value, (testStep.testData as Map<*, *>?)!![fieldName])
    }

    @Test
    @DisplayName("With validation of values with special characters")
    fun testCreateTestStepWithValidationOfValueWithSpecialChars() {
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("validate"),
                MapValueExpression(
                    mapOf(
                        Pair(
                            CallChainExpression(listOf(StringValueExpression("f.1"), FunctionCallExpression("toString", emptyArray()))),
                            StringValueExpression("2.0")
                        ),
                        Pair(
                            CallChainExpression(listOf(AlphanumericValueExpression("a"), AlphanumericValueExpression("b"), StringValueExpression("c.d"), StringValueExpression("e"))),
                            CallChainExpression(
                                listOf(
                                    StringValueExpression("a.b"),
                                    FunctionCallExpression("toString", emptyArray()),
                                    FunctionCallExpression("regex", arrayOf(StringValueExpression("([\\w.]{3})")))
                                )
                            )
                        )
                    )
                )
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf())

        val validationDetails = (testStep.validationDetails as ValidationNodes).nodes

        assertAll(
            { assertEquals("f.1.toString()", (validationDetails[0] as PairValidationNode).actual.toString()) },
            { assertEquals("2.0", (validationDetails[0] as PairValidationNode).expected.toString()) },
            { assertEquals("a.b.c.d.e", (validationDetails[1] as PairValidationNode).actual.toString()) },
            { assertEquals("a.b.toString().regex(([\\w.]{3}))", (validationDetails[1] as PairValidationNode).expected.toString()) }
        )
    }

    @Test
    @DisplayName("With test data as a content conversion function")
    fun testCreateTestStepWithTestDataAsContentConversionFunction() {
        val data = "something"
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("data"),
                CallChainExpression(listOf(AlphanumericValueExpression(data), FunctionCallExpression("toBytes", emptyArray())))
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf())

        assertEquals(data, String(testStep.testData as ByteArray))
    }

    @Test
    @DisplayName("With values has reference to other values")
    fun testCreateTestStepWithValuesReferenceToOtherValues() {
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("values"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("f1"), AlphanumericValueExpression("v1")),
                        Pair(AlphanumericValueExpression("f2"), PropertyValueExpression("f1", null)),
                        Pair(AlphanumericValueExpression("f3"), PropertyValueExpression("f2", null)),
                    )
                )
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())
        val values = testStep.values!!

        assertAll(
            { assertEquals("v1", values["f2"]) },
            { assertEquals(values["f1"], values["f2"]) },
            { assertEquals(values["f1"], values["f3"]) },
        )
    }

    @Test
    @DisplayName("With values has reference to other values and inner references")
    fun testCreateTestStepWithValuesReferenceToOtherValuesAndInnerReferences() {
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("values"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("i"), NumberValueExpression("1")),
                        Pair(MathOperationExpression("+", AlphanumericValueExpression("n_"), PropertyValueExpression("i")), NumberValueExpression("2")),
                        Pair(MathOperationExpression("+", AlphanumericValueExpression("x_"),
                            PropertyValueExpression(MathOperationExpression("+", AlphanumericValueExpression("n_"), PropertyValueExpression("i")))), NumberValueExpression("3")),
                    )
                )
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap<String, String>())
        val values = testStep.values!!

        assertAll(
            { assertEquals(BigDecimal("1"), values["i"]) },
            { assertEquals(BigDecimal("2"), values["n_1"]) },
            { assertEquals(BigDecimal("3"), values["x_2"]) },
        )
    }

    @Test
    @DisplayName("When has reference to parent test step Then check all data merged")
    fun testCreateTestStepWithRefToParent() {
        val id = AlphanumericValueExpression("stepA")
        val parentPayload = StringValueExpression("parent payload")
        val testStepA = mapOf<ValueExpression, ValueExpression?>(
            Pair(AlphanumericValueExpression("id"), id),
            Pair(AlphanumericValueExpression("payload"), parentPayload)
        )

        whenever(testStepRegistry.getById("stepA")).thenReturn(testStepA)

        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(AlphanumericValueExpression("parent"), id),
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf(Pair("1", "v2")))

        assertAll(
            { assertEquals(id.toString(), testStep.id) },  // id is replaced by the latest parent
            { assertEquals(parentPayload.toString(), testStep.testData) }
        )
    }

    @Test
    @DisplayName("When has reference to parent test steps Then check all data merged")
    fun testCreateTestStepWithRefToParents() {
        val idA = AlphanumericValueExpression("stepA")
        val testStepA = mapOf<ValueExpression, ValueExpression?>(
            Pair(AlphanumericValueExpression("id"), idA),
            Pair(AlphanumericValueExpression("values"), MapValueExpression(mapOf(Pair(AlphanumericValueExpression("f1"), AlphanumericValueExpression("v1"))))),
            Pair(AlphanumericValueExpression("payload"), StringValueExpression("to replace by child"))
        )
        val idB = AlphanumericValueExpression("stepB")
        val testStepB = mapOf<ValueExpression, ValueExpression?>(
            Pair(AlphanumericValueExpression("id"), idB),
            Pair(AlphanumericValueExpression("validate"), MapValueExpression(mapOf(Pair(AlphanumericValueExpression("f4"), AlphanumericValueExpression("v4"))))),
        )
        whenever(testStepRegistry.getById("stepA")).thenReturn(testStepA)
        whenever(testStepRegistry.getById("stepB")).thenReturn(testStepB)

        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(AlphanumericValueExpression("parent"), ListValueExpression(listOf(idA, idB))),
            Pair(
                AlphanumericValueExpression("payload"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("new_f1"), PropertyValueExpression("f1", null)),  // this reference must be taken from testStepA vars
                        Pair(AlphanumericValueExpression("f2"), PropertyValueExpression("f2", null)),
                        Pair(AlphanumericValueExpression("f3"), AlphanumericValueExpression("something")),
                    )
                )
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, mapOf(Pair("f2", "v2")))
        val validationDetails = (testStep.validationDetails as ValidationNodes).nodes[0]

        assertAll(
            { assertEquals(idB.toString(), testStep.id) },  // id is replaced by the latest parent
            { assertEquals("v1", testStep.values!!["f1"]) },
            { assertEquals("v1", (testStep.testData as Map<*, *>?)!!["new_f1"]) },
            { assertEquals("v2", (testStep.testData as Map<*, *>?)!!["f2"]) },
            { assertEquals("something", (testStep.testData as Map<*, *>?)!!["f3"]) },
            { assertEquals("v4", (validationDetails as PairValidationNode).expected.toString()) }
        )
    }

    @Test
    @DisplayName("When value has if statement Then check it assigns value correctly")
    fun testCreateTestStepWithIfStatement() {
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("values"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("f1"), AlphanumericValueExpression("v1")),
                        Pair(
                            AlphanumericValueExpression("f2"), MapValueExpression(
                                mapOf<ValueExpression, ValueExpression?>(
                                    Pair(AlphanumericValueExpression("a"), NumberValueExpression("1")),
                                    Pair(AlphanumericValueExpression("b"), NumberValueExpression("2"))
                                )
                            )
                        ),
                    )
                )
            ),
            Pair(
                AlphanumericValueExpression("payload"), MapValueExpression(
                    mapOf(
                        Pair(
                            AlphanumericValueExpression("c"),
                            FunctionCallExpression(
                                "if", arrayOf(
                                    ValueComparisonExpression("==", NumberValueExpression("1"), NumberValueExpression("1")),
                                    PropertyValueExpression("f2", null), PropertyValueExpression("f1", null)
                                )
                            )
                        )
                    )
                )
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap())

        assertEquals(
            mapOf(
                Pair("a", BigDecimal("1")),
                Pair("b", BigDecimal("2"))
            ),
            (testStep.testData as Map<*, *>?)!!["c"]
        )
    }

    @Test
    @DisplayName("With state update details")
    fun testCreateTestStepWithStateUpdate() {
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(
            Pair(
                AlphanumericValueExpression("state"),
                MapValueExpression(
                    mapOf(
                        Pair(AlphanumericValueExpression("a"), CallChainExpression(listOf(PropertyValueExpression("amt", null), FunctionCallExpression("toBigDecimal", emptyArray())))),
                        Pair(AlphanumericValueExpression("b"), MathOperationExpression("+", FunctionCallExpression("get", arrayOf(AlphanumericValueExpression("a"))), NumberValueExpression("500"))),
                    )
                )
            )
        )

        val testStep = testStepFactory!!.create("test 1", rawTestStep, emptyMap())

        assertAll(
            { assertEquals("a = \${amt}.toBigDecimal()", testStep.scenarioStateUpdaters!![0].toString()) },
            { assertEquals("b = get(a) + 500", testStep.scenarioStateUpdaters!![1].toString()) },
        )
    }
}