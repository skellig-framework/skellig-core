package org.skellig.teststep.processing.state

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class TestScenarioScopeTest {

    private val state = DefaultTestScenarioState()

    @Test
    fun testHoldsIdAndState() {
        val scope = TestScenarioScope("s1", state)

        assertAll(
            { assertEquals("s1", scope.id) },
            { assertSame(state, scope.state) },
        )
    }

    @Test
    fun testPrintsAsShortScopeId() {
        assertEquals("scope(s1)", TestScenarioScope("s1", state).toString())
    }

    @Test
    fun testReservedParameterKey() {
        assertEquals("skellig.scope", TestScenarioScope.PARAMETER_KEY)
    }

    @Test
    fun testFindScopeInParameters() {
        val scope = TestScenarioScope("s1", state)

        assertAll(
            { assertSame(scope, TestScenarioScope.from(mapOf(TestScenarioScope.PARAMETER_KEY to scope))) },
            { assertNull(TestScenarioScope.from(mapOf("a" to "b"))) },
            { assertNull(TestScenarioScope.from(null)) },
            { assertNull(TestScenarioScope.from(mapOf(TestScenarioScope.PARAMETER_KEY to "not a scope"))) },
        )
    }

    @Test
    fun testAddScopeToParametersWithoutChangingTheOriginalMap() {
        val scope = TestScenarioScope("s1", state)
        val parameters = mapOf<String, Any?>("a" to "b")

        val result = TestScenarioScope.addTo(parameters, scope)

        assertAll(
            { assertEquals(mapOf("a" to "b", TestScenarioScope.PARAMETER_KEY to scope), result) },
            { assertEquals(mapOf("a" to "b"), parameters) },
        )
    }

    @Test
    fun testCallersScopeWinsOverScopeInParameters() {
        val callerScope = TestScenarioScope("caller", state)
        val otherScope = TestScenarioScope("other", DefaultTestScenarioState())

        val result = TestScenarioScope.addTo(mapOf(TestScenarioScope.PARAMETER_KEY to otherScope), callerScope)

        assertSame(callerScope, result[TestScenarioScope.PARAMETER_KEY])
    }

    @Test
    fun testRemoveScopeFromParameters() {
        val scope = TestScenarioScope("s1", state)
        val parameters = mapOf<String, Any?>("a" to "b", TestScenarioScope.PARAMETER_KEY to scope)

        val result = TestScenarioScope.removeFrom(parameters)

        assertAll(
            { assertEquals(mapOf("a" to "b"), result) },
            { assertEquals(2, parameters.size) },
            { assertSame(result, TestScenarioScope.removeFrom(result)) },
        )
    }

    @Test
    fun testParametersAreUnchangedWhenNoScope() {
        val parameters = mapOf<String, Any?>("a" to "b")

        assertSame(parameters, TestScenarioScope.addTo(parameters, null))
    }
}
