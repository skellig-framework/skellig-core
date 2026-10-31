package org.skellig.teststep.processing.processor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.ParallelGroupContext
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.state.TestScenarioState
import java.util.regex.Pattern

class ClassTestStepProcessorTest {

    private val scope = TestScenarioScope("test", DefaultTestScenarioState())
    private val classTestStepProcessor = ClassTestStepProcessor()

    @Test
    fun testProcessCallingMethodWithOneParam() {
        val testStepMethod = javaClass.methods.find { it.name == "runSomething" }
        val testStep = ClassTestStep(
            "", Pattern.compile("run with n = (.+)"), javaClass, testStepMethod!!,
            "run with n = 10", emptyMap(), scope
        )

        val runResult = classTestStepProcessor.process(testStep)
        runResult.subscribe { t, r, e ->
            assertNull(e)
            assertNull(r)
            assertEquals(t, testStep)
        }
    }

    @Test
    fun testProcessCallingMethodWithOneParamAndMapOfParams() {
        val testStepMethod = javaClass.methods.find { it.name == "runSomethingWithMap" }
        val testStep = ClassTestStep(
            "", Pattern.compile("run with n = (.+)"), javaClass, testStepMethod!!,
            "run with n = 10", mapOf(Pair("p1", "v1"), Pair("p2", "v2")), scope
        )

        val runResult = classTestStepProcessor.process(testStep)
        runResult.subscribe { t, _, e ->
            assertNull(e)
            assertEquals(t, testStep)
        }
    }

    @Test
    fun testProcessCallingMethodWhenMapOfParamsNotSet() {
        val testStepMethod = javaClass.methods.find { it.name == "runSomethingWithMap" }
        val testStep = ClassTestStep(
            "", Pattern.compile("run with n = (.+)"), javaClass, testStepMethod!!,
            "run with n = 10", null, scope
        )

        val runResult = classTestStepProcessor.process(testStep)
        runResult.subscribe { _, _, e ->
            assertTrue(e!!.message!!.contains("Parameter specified as non-null is null"), "Invalid error message")
            assertEquals(TestStepProcessingException::class.java, e.javaClass)
        }
    }

    @Test
    fun testProcessCallingMethodWithPrivateAccess() {
        val testStepMethod = javaClass.declaredMethods.find { it.name == "callPrivateMethod" }
        val testStep = ClassTestStep(
            "", Pattern.compile("run with"), javaClass, testStepMethod!!,
            "run with", emptyMap(), scope
        )

        val runResult = classTestStepProcessor.process(testStep)
        runResult.subscribe { _, _, e ->
            assertEquals(
                "Failed to access non-public method 'ClassTestStepProcessorTest:" +
                        "private final void org.skellig.teststep.processing.processor.ClassTestStepProcessorTest.callPrivateMethod()' " +
                        "of test step 'run with'", e!!.message
            )
            assertEquals(TestStepProcessingException::class.java, e.javaClass)
        }
    }

    @Test
    fun testProcessCallingMethodWhenHasMapArgumentBeforeParameters() {
        val testStepMethod = javaClass.methods.find { it.name == "runSomethingWithMap2" }
        val testStep = ClassTestStep(
            "", Pattern.compile("run with"), javaClass, testStepMethod!!,
            "run with", mapOf(Pair("a", "b"), Pair("c", "d")), scope
        )

        val runResult = classTestStepProcessor.process(testStep)
        runResult.subscribe { _, _, e ->
            assertNull(e, "Got error")
        }
    }

    @Test
    fun testProcessInjectsStateOfStepScopeAndFillsRegexGroupsAndMap() {
        val ownScope = TestScenarioScope("own", DefaultTestScenarioState())
        val testStep = ClassTestStep(
            "", Pattern.compile("run (.+) and (.+)"), InjectingSteps::class.java, InjectingSteps::class.java.methods.find { it.name == "stateInTheMiddle" }!!,
            "run x and y", mapOf("p" to "v", TestScenarioScope.PARAMETER_KEY to ownScope), ownScope
        )

        classTestStepProcessor.process(testStep).subscribe { _, _, e -> assertNull(e) }

        assertEquals(listOf("x", ownScope.state, "y", mapOf("p" to "v")), ownScope.parallelGroup.instanceOf(InjectingSteps::class.java).received)
    }

    @Test
    fun testProcessInjectsScope() {
        val ownScope = TestScenarioScope("own", DefaultTestScenarioState())
        val testStep = ClassTestStep(
            "", Pattern.compile("run"), InjectingSteps::class.java, InjectingSteps::class.java.methods.find { it.name == "scopeOnly" }!!, "run", emptyMap(), ownScope
        )

        classTestStepProcessor.process(testStep).subscribe { _, _, e -> assertNull(e) }

        assertEquals(listOf(ownScope), ownScope.parallelGroup.instanceOf(InjectingSteps::class.java).received)
    }

    @Test
    fun testProcessDoesNotInjectIntoParameterOfTypeAny() {
        val testStep = ClassTestStep(
            "", Pattern.compile("run (.+) (.+)"), InjectingSteps::class.java, InjectingSteps::class.java.methods.find { it.name == "anyParameter" }!!, "run x y", emptyMap(), scope
        )

        classTestStepProcessor.process(testStep).subscribe { _, _, e -> assertNull(e) }

        assertEquals(listOf("x", "y"), scope.parallelGroup.instanceOf(InjectingSteps::class.java).received)
    }

    @Test
    fun testProcessCallsMethodOnTheInstanceOfTheParallelGroupOfTheScope() {
        val group1 = ParallelGroupContext("g1")
        val group2 = ParallelGroupContext("g2")
        val scope1 = TestScenarioScope("s1", DefaultTestScenarioState(), group1)
        val scope2 = TestScenarioScope("s2", DefaultTestScenarioState(), group1)
        val scope3 = TestScenarioScope("s3", DefaultTestScenarioState(), group2)

        listOf(scope1, scope2, scope3).forEach { process("count", it) }

        assertAll(
            { assertEquals(2, group1.instanceOf(CountingSteps::class.java).calls) },
            { assertEquals(1, group2.instanceOf(CountingSteps::class.java).calls) },
        )
    }

    @Test
    fun testProcessKeepsFieldsOfTheInstanceBetweenStepsOfTheSameParallelGroup() {
        val group = ParallelGroupContext("g1")

        process("count", TestScenarioScope("s1", DefaultTestScenarioState(), group))
        process("count", TestScenarioScope("s2", DefaultTestScenarioState(), group))

        assertEquals(listOf(1, 2), group.instanceOf(CountingSteps::class.java).results)
    }

    @Test
    fun testProcessStoresResultInTheStateOfTheScope() {
        val ownScope = TestScenarioScope("own", DefaultTestScenarioState())

        process("count", ownScope)

        assertEquals(1, ownScope.state.get("count_result"))
    }

    @Test
    fun testProcessFailsWhenTheStepHasNoScope() {
        val testStep = ClassTestStep("", Pattern.compile("count"), CountingSteps::class.java, countMethod(), "count", emptyMap(), null)

        val ex = assertThrows(IllegalStateException::class.java) { classTestStepProcessor.process(testStep) }

        assertEquals("Test step 'count' has no scenario scope. A test step must be run with a TestScenarioScope", ex.message)
    }

    @Test
    fun testProcessFailsWhenInstanceOfTheClassCannotBeCreated() {
        val ownScope = TestScenarioScope("own", DefaultTestScenarioState(), ParallelGroupContext("g1"))
        val testStep = ClassTestStep(
            "", Pattern.compile("count"), BrokenSteps::class.java, BrokenSteps::class.java.methods.find { it.name == "step" }!!,
            "count", emptyMap(), ownScope
        )

        val ex = assertThrows(IllegalStateException::class.java) { classTestStepProcessor.process(testStep) }

        assertEquals("Failed to create an instance of class '${BrokenSteps::class.java.name}' in parallel group 'g1'", ex.message)
    }

    private fun process(name: String, scope: TestScenarioScope) {
        val testStep = ClassTestStep("", Pattern.compile(name), CountingSteps::class.java, countMethod(), name, emptyMap(), scope)
        classTestStepProcessor.process(testStep).subscribe { _, _, e -> assertNull(e) }
    }

    private fun countMethod() = CountingSteps::class.java.methods.find { it.name == "count" }!!

    @Test
    fun testGetTestStepClass() {
        assertEquals(ClassTestStep::class.java, classTestStepProcessor.getTestStepClass())
    }

    fun runSomething(p1: String) {
        assertTrue(p1.toInt() == 10)
    }

    private fun callPrivateMethod() {
    }

    fun runSomethingWithMap(p1: String, params: Map<String, String>) {
        runSomething(p1)
        assertEquals("v1", params["p1"])
        assertEquals("v2", params["p2"])
    }

    fun runSomethingWithMap2(p1: String?, someData: Map<Int, String>?, params: Map<String, String>) {
        assertNull(p1)
        assertNull(someData)
        assertEquals("b", params["a"])
        assertEquals("d", params["c"])
    }
}

class InjectingSteps {

    val received = mutableListOf<Any?>()

    fun stateInTheMiddle(a: String, state: TestScenarioState, b: String, parameters: Map<String, Any?>) {
        received.addAll(listOf(a, state, b, parameters))
    }

    fun scopeOnly(scope: TestScenarioScope) {
        received.add(scope)
    }

    fun anyParameter(any: Any?, other: String) {
        received.addAll(listOf(any, other))
    }
}

class CountingSteps {

    var calls = 0
    val results = mutableListOf<Int>()

    fun count(): Int {
        calls++
        results.add(calls)
        return calls
    }
}

class BrokenSteps {

    init {
        throw RuntimeException("cannot be created")
    }

    fun step() {
    }
}
