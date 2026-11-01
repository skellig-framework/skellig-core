package org.skellig.runner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.RunWith
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.singleinstance.SingleInstanceStepDefs

/**
 * Without a parallel run configured, all scenarios run in one default parallel group, so a step definition class,
 * with its hooks and functions, has exactly one instance, as before parallel groups.
 */
class DefaultParallelGroupTest {

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["single-instance-feature"],
        testSteps = ["single-instance-feature", "org.skellig.runner.singleinstance"],
        config = "test.conf"
    )
    class SingleInstanceRunner

    @Before
    fun resetRecordedInstances() {
        SingleInstanceStepDefs.stepDefExecutionRecorder.clear()
    }

    @Test
    fun testHooksStepsAndFunctionsOfAllScenariosRunOnOneInstance() {
        val result = JUnitCore().run(SingleInstanceRunner::class.java)

        assertTrue(result.failures.joinToString { it.message ?: it.toString() }, result.wasSuccessful())
        // a hook and a step in each of 2 scenarios, and a function in the first one
        assertEquals(5, SingleInstanceStepDefs.stepDefExecutionRecorder.size)
        // make sure all instances are the same
        assertEquals(1, SingleInstanceStepDefs.stepDefExecutionRecorder.map { System.identityHashCode(it) }.toSet().size)
    }
}
