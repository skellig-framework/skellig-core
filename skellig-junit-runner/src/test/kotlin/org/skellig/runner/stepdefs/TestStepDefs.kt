package org.skellig.runner.stepdefs

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.fail
import org.skellig.teststep.runner.annotation.TestStep

class TestStepDefs {

    @TestStep(name = "Log (.*)")
    fun logResult(value: String?, parameters: Map<String, String?>) {
        Assertions.assertNotNull(value)
        Assertions.assertEquals(1, parameters.size)

        println(value)
    }

    @TestStep(name = "Check user exist")
    fun checkUserExist() {
        fail("User doesn't exist")
    }
}