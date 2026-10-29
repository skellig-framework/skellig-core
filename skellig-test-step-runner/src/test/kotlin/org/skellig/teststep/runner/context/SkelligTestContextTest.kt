package org.skellig.teststep.runner.context

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionRegistryException

class SkelligTestContextTest {

    @Test
    fun `initialize fails when two functions have the same name`() {
        val ex = assertThrows<FunctionRegistryException> {
            SkelligTestContext().initialize(
                javaClass.classLoader,
                listOf("org.skellig.teststep.runner.fixture.duplicate")
            )
        }

        val functionClasses = listOf(
            "org.skellig.teststep.runner.fixture.duplicate.DuplicateFunctionsA.same",
            "org.skellig.teststep.runner.fixture.duplicate.DuplicateFunctionsB.same"
        )
        assertEquals(
            setOf(true),
            functionClasses.map { ex.message!!.contains("'$it'") }.toSet(),
            ex.message
        )
    }
}
