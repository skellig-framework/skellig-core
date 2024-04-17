package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.processor.TestStepProcessor
import java.io.Closeable
import java.util.concurrent.locks.ReentrantLock

/**
 * Represents the context for task processing from [TaskProcessor].
 *
 * @property parameters The mutable map of parameters.
 */
data class TaskProcessingContext(val parameters: MutableMap<String, Any?>) : Closeable {

    private var lock = ReentrantLock()
    private var testStepsResults: MutableList<TestStepProcessor.TestStepRunResult> = mutableListOf()

    /**
     * Creates a new instance of the [TaskProcessingContext] class by copying only parameters from
     * an existing [TaskProcessingContext] instance. Usually used to pass to threads.
     *
     * @param context The original [TaskProcessingContext] instance to be copied.
     */
    constructor(context: TaskProcessingContext) : this(context.parameters.toMutableMap()) {
        testStepsResults = context.testStepsResults
        lock = context.lock
    }

    /**
     * Adds a test step run result to the common list which can be used to await them later.
     *
     * @param result The test step run result to be added.
     */
    fun addResultFromTestStep(result: TestStepProcessor.TestStepRunResult) {
        lock.lock()
        try {
            testStepsResults.add(result)
        } finally {
            lock.unlock()
        }
    }

    fun getTestStepsResult(): List<TestStepProcessor.TestStepRunResult> = testStepsResults

    override fun close() {
        parameters.clear()
        testStepsResults.clear()
    }
}