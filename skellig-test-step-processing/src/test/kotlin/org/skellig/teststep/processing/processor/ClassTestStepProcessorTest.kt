package org.skellig.teststep.processing.processor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.processing.state.DefaultTestScenarioState
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
            "", Pattern.compile("run with n = (.+)"), this, testStepMethod!!,
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
            "", Pattern.compile("run with n = (.+)"), this, testStepMethod!!,
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
            "", Pattern.compile("run with n = (.+)"), this, testStepMethod!!,
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
            "", Pattern.compile("run with"), this, testStepMethod!!,
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
            "", Pattern.compile("run with"), this, testStepMethod!!,
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
        val steps = InjectingSteps()
        val testStep = ClassTestStep(
            "", Pattern.compile("run (.+) and (.+)"), steps, steps.javaClass.methods.find { it.name == "stateInTheMiddle" }!!,
            "run x and y", mapOf("p" to "v", TestScenarioScope.PARAMETER_KEY to ownScope), ownScope
        )

        classTestStepProcessor.process(testStep).subscribe { _, _, e -> assertNull(e) }

        assertEquals(listOf("x", ownScope.state, "y", mapOf("p" to "v")), steps.received)
    }

    @Test
    fun testProcessInjectsScope() {
        val ownScope = TestScenarioScope("own", DefaultTestScenarioState())
        val steps = InjectingSteps()
        val testStep = ClassTestStep(
            "", Pattern.compile("run"), steps, steps.javaClass.methods.find { it.name == "scopeOnly" }!!, "run", emptyMap(), ownScope
        )

        classTestStepProcessor.process(testStep).subscribe { _, _, e -> assertNull(e) }

        assertEquals(listOf(ownScope), steps.received)
    }

    @Test
    fun testProcessDoesNotInjectIntoParameterOfTypeAny() {
        val steps = InjectingSteps()
        val testStep = ClassTestStep(
            "", Pattern.compile("run (.+) (.+)"), steps, steps.javaClass.methods.find { it.name == "anyParameter" }!!, "run x y", emptyMap(), scope
        )

        classTestStepProcessor.process(testStep).subscribe { _, _, e -> assertNull(e) }

        assertEquals(listOf("x", "y"), steps.received)
    }

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
