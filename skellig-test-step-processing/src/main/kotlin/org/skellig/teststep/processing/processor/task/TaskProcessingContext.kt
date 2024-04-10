package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.processor.TestStepProcessor
import java.io.Closeable
import java.util.concurrent.locks.ReentrantLock

data class TaskProcessingContext(val parameters: MutableMap<String, Any?>) : Closeable {

    private var lock = ReentrantLock()
    private var testStepsResults: MutableList<TestStepProcessor.TestStepRunResult> = mutableListOf()

    constructor(context: TaskProcessingContext) : this(context.parameters.toMutableMap()) {
        testStepsResults = context.testStepsResults
        lock = context.lock
    }

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