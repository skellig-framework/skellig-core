package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.state.DefaultTestScenarioState
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