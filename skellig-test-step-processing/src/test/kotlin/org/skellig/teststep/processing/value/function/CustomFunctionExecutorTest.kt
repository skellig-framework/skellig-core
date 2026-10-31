package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.fixture.instances.CountingFunctions
import org.skellig.teststep.processing.fixture.lazyfunctions.LazyFunctions
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.ParallelGroupContext
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import org.skellig.teststep.processing.value.exception.FunctionRegistryException

class CustomFunctionExecutorTest {

    @Test
    fun `register functions from class with default constructor`() {
        val converter = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.value.function"))

        assertTrue(converter.execute("lessThan", null, arrayOf("1", "2")) as Boolean)
        assertFalse(converter.execute("isEmpty", null, arrayOf("gggg", "")) as Boolean)
        assertNull(converter.execute("runSomething", null, emptyArray())) // no return means null
        assertThrows(FunctionExecutionException::class.java) { converter.execute("runNotExisting", null, emptyArray()) }
    }

    @Test
    fun `register functions from class with no default constructor`() {
        val ex = assertThrows<FunctionRegistryException> {
            CustomFunctionExecutor(listOf("org.skellig.teststep.processing.invalid"))
        }
        assertEquals("Failed to instantiate class 'org.skellig.teststep.processing.invalid.InvalidCustomFunctions'", ex.message)
    }

    @Test
    fun `execute the annotated overload of a function`() {
        val executor = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.overload"))

        assertEquals("annotated:ab", executor.execute("combine", null, arrayOf("a", "b")))
    }

    @Test
    fun `fail when two functions have the same name`() {
        val ex = assertThrows<FunctionRegistryException> {
            CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.duplicate"))
        }
        val expected = "Function 'same' is defined more than once: "
        assertTrue(ex.message!!.startsWith(expected), ex.message)
        assertTrue(ex.message!!.contains("'org.skellig.teststep.processing.fixture.duplicate.DuplicateFunctionsA.same'"), ex.message)
        assertTrue(ex.message!!.contains("'org.skellig.teststep.processing.fixture.duplicate.DuplicateFunctionsB.same'"), ex.message)
    }

    @Test
    fun `inject the state into a function at any position and fill the rest by arguments in order`() {
        val state = DefaultTestScenarioState().apply { set("k", "-v-") }
        val scope = TestScenarioScope("s1", state)
        val executor = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.injection"))

        assertAll(
            { assertEquals("-v-", executor.execute("stateFirst", null, arrayOf("k"), scope)) },
            { assertEquals("a-v-b", executor.execute("stateInMiddle", null, arrayOf("a", "b"), scope)) },
            { assertEquals("a-s1", executor.execute("scopeLast", null, arrayOf("a"), scope)) },
        )
    }

    @Test
    fun `function without injected parameters works with and without scope`() {
        val executor = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.injection"))

        assertAll(
            { assertEquals("ab", executor.execute("noInjection", null, arrayOf("a", "b"))) },
            { assertEquals("ab", executor.execute("noInjection", null, arrayOf("a", "b"), TestScenarioScope("s1", DefaultTestScenarioState()))) },
        )
    }

    @Test
    fun `parameter of type Any is not injected`() {
        val executor = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.injection"))
        val scope = TestScenarioScope("s1", DefaultTestScenarioState())

        assertEquals("arg", executor.execute("anyIsNotInjected", null, arrayOf("arg"), scope))
    }

    @Test
    fun `fail when function needs the scenario scope but there is none`() {
        val executor = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.injection"))

        val ex = assertThrows<FunctionExecutionException> { executor.execute("stateFirst", null, arrayOf("k")) }

        assertEquals("Function 'stateFirst' requires the scope of a test scenario but it is called outside of one", ex.message)
    }

    @Test
    fun `call a function on the instance of the parallel group of the scope`() {
        val executor = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.instances"))
        val group1 = ParallelGroupContext("g1")
        val group2 = ParallelGroupContext("g2")
        val scopeOfGroup1 = TestScenarioScope("s1", DefaultTestScenarioState(), group1)
        val otherScopeOfGroup1 = TestScenarioScope("s2", DefaultTestScenarioState(), group1)
        val scopeOfGroup2 = TestScenarioScope("s3", DefaultTestScenarioState(), group2)

        val results = listOf(scopeOfGroup1, otherScopeOfGroup1, scopeOfGroup2).map { executor.execute("nextCall", null, emptyArray(), it) }

        assertEquals(listOf(1, 2, 1), results)
    }

    @Test
    fun `functions of the same class share the instance within a parallel group`() {
        val executor = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.instances"))
        val scope = TestScenarioScope("s1", DefaultTestScenarioState(), ParallelGroupContext("g1"))

        val results = listOf("nextCall", "otherFunctionOfTheSameClass", "nextCall").map { executor.execute(it, null, emptyArray(), scope) }

        assertEquals(listOf(1, 2, 3), results)
    }

    @Test
    fun `function shares the instance with the class used through the parallel group`() {
        val executor = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.instances"))
        val group = ParallelGroupContext("g1")
        val scope = TestScenarioScope("s1", DefaultTestScenarioState(), group)

        executor.execute("nextCall", null, emptyArray(), scope)

        assertEquals(2, group.instanceOf(CountingFunctions::class.java).nextCall())
    }

    @Test
    fun `functions called without a scope use the default parallel group of the executor`() {
        val defaultGroup = ParallelGroupContext(ParallelGroupContext.DEFAULT_KEY)
        val executor = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.instances"), defaultGroup)

        val results = (1..2).map { executor.execute("nextCall", null, emptyArray()) }

        assertAll(
            { assertEquals(listOf(1, 2), results) },
            { assertEquals(3, defaultGroup.instanceOf(CountingFunctions::class.java).nextCall()) },
        )
    }

    @Test
    fun `function classes are not instantiated until a function is called`() {
        LazyFunctions.created.set(0)
        val group = ParallelGroupContext("g1")

        val executor = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.fixture.lazyfunctions"))
        assertEquals(0, LazyFunctions.created.get())

        executor.execute("something", null, emptyArray(), TestScenarioScope("s1", DefaultTestScenarioState(), group))
        executor.execute("something", null, emptyArray(), TestScenarioScope("s2", DefaultTestScenarioState(), group))

        assertEquals(1, LazyFunctions.created.get())
    }

    @Test
    fun `get function name as empty`() {
        assertEquals("", CustomFunctionExecutor(listOf("org.skellig.teststep.processing.value.function")).getFunctionName())
    }
}

class CustomFunctions {

    @Function
    fun lessThan(a: String, b: String): Boolean = a.toInt() < b.toInt()

    @Function
    fun isEmpty(a: String, empty: String): Boolean = a == empty

    @Function
    fun runSomething() {

    }
}