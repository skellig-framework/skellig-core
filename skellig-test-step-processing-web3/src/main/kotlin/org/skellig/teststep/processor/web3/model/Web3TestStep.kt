package org.skellig.teststep.processor.web3.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

class Web3TestStep(
    id: String?,
    name: String,
    execution: TestStepExecutionType?,
    timeout: Int,
    delay: Int,
    attempts: Int,
    variables: Map<String, Any?>?,
    testData: Any?,
    validationDetails: ValidationDetails?,
    val nodes: Collection<String>?,
    val method: String?,
    val event: String?,
    val filter: Map<String, Any>?,
    val returns: List<String>?,
    private val params: Any?
) : DefaultTestStep(id, name, execution, timeout, delay, attempts, variables, testData, validationDetails) {

    fun getParams(): List<Any> {
        return if (params is List<*>) {
            params as List<Any>
        } else {
            (params as Map<*, *>).mapNotNull { it.value }.toList()
        }
    }

    class Builder : DefaultTestStep.Builder<Web3TestStep>() {

        private var nodes: Collection<String>? = emptyList()
        private var method: String? = null
        private var event: String? = null
        private var params: Any? = null
        private var returns: List<String>? = null
        private var filter: Map<String, Any>? = null

        fun withNodes(nodes: Collection<String>?) = apply {
            this.nodes = nodes
        }

        fun withMethod(method: String?) = apply {
            this.method = method
        }

        fun withEvent(event: String?) = apply {
            this.event = event
        }

        fun withFilter(filter: Map<String, Any>?) = apply {
            this.filter = filter
        }

        fun withParams(params: Any?) = apply {
            this.params = params
        }

        fun withReturns(returns: List<String>?) = apply {
            this.returns = returns
        }

        override fun build(): Web3TestStep {
            return Web3TestStep(id, name!!, execution, timeout, delay, attempts, variables, testData,
                validationDetails, nodes, method, event, filter, returns, params)
        }
    }
}