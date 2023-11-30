package org.skellig.teststep.processor.tcp.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.validation.ValidationNode

abstract class BaseTcpTestStep protected constructor(id: String?,
                                                     name: String,
                                                     execution: TestStepExecutionType?,
                                                     timeout: Int,
                                                     delay: Int,
                                                     attempts: Int,
                                                     variables: Map<String, Any?>?,
                                                     testData: Any?,
                                                     validationDetails: ValidationNode?,
                                                     val readBufferSize: Int)
    : DefaultTestStep(id, name, execution, timeout, delay, attempts, variables, testData, validationDetails) {

    abstract class Builder<T : BaseTcpTestStep> : DefaultTestStep.Builder<T>() {

        protected var readBufferSize = 1024 * 1024

        fun readBufferSize(readBufferSize: Int?) = apply {
            readBufferSize?.let { this.readBufferSize = it }
        }
    }
}