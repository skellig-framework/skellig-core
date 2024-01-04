package org.skellig.teststep.processing.model

/**
 * A group of test steps to run based on a result from previous test step (ex. fail or pass).
 * The structure is similar to a Binary Tree where it runs a root test step, then goes
 * down based on its result:
 * - pass: if `passed` property not null, then processing goes to this test step.
 * - fail: if `failed` property not null, then processing goes to this test step.
 *
 * Then the process is repeated until it reaches `null` for `passed` or `failed`
 * depending on the previous outcome.
 */
class GroupedTestStep(
    override val name: String,
    val testStepToRun: TestStepRun
) : TestStep {

    /**
     * This structure is a node in the `GroupedTestStep` tree.
     * `testStepLazy` is a function because the `TestStep` must be initialized
     * at the point where it goes to its appropriate processor.
     */
    class TestStepRun(val testStepLazy: () -> TestStep, val passed: TestStepRun?, val failed: TestStepRun?)

    override fun toString(): String {
        return ""
    }
}