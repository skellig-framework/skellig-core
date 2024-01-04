package org.skellig.teststep.processor.tcp.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

abstract class BaseTcpTestStep protected constructor(id: String?,
                                                     name: String,
                                                     execution: TestStepExecutionType?,
                                                     timeout: Int,
                                                     delay: Int,
                                                     attempts: Int,
                                                     values: Map<String, Any?>?,
                                                     testData: Any?,
                                                     validationDetails: ValidationNode?,
                                                     scenarioStateUpdaters: List<ScenarioStateUpdater>?,
                                                     val readBufferSize: Int)
    : DefaultTestStep(id, name, execution, timeout, delay, attempts, values, testData, validationDetails, scenarioStateUpdaters) {

    override fun toString(): String {
        return super.toString() + "readBufferSize = $readBufferSize\n"
    }

    abstract class Builder<T : BaseTcpTestStep> : DefaultTestStep.Builder<T>() {

        protected var readBufferSize = 1024 * 1024

        fun readBufferSize(readBufferSize: Int?) = apply {
            readBufferSize?.let { this.readBufferSize = it }
        }
    }
}