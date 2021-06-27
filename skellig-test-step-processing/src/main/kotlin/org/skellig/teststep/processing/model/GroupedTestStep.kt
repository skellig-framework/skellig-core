package org.skellig.teststep.processing.model

class GroupedTestStep constructor(override val name: String,
                                            val testStepToRun: TestStepRun) : TestStep {

    class TestStepRun(val testStepLazy: () -> TestStep, val passed: TestStepRun?, val failed: TestStepRun?)
}