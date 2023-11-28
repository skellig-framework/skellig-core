package org.skellig.teststep.processor.rmq.model

import com.rabbitmq.client.AMQP
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.validation.ValidationNode
import java.util.*

open class BaseRmqTestStep protected constructor(id: String?,
                                                 name: String?,
                                                 execution: TestStepExecutionType?,
                                                 timeout: Int,
                                                 delay: Int,
                                                 attempts: Int,
                                                 variables: Map<String, Any?>?,
                                                 testData: Any?,
                                                 validationDetails: ValidationNode?,
                                                 val routingKey: String?,
                                                 val properties: Map<String, Any?>?)
    : DefaultTestStep(id, name!!, execution, timeout, delay, attempts, variables, testData, validationDetails) {

    fun getAmqpProperties(): AMQP.BasicProperties? =
        properties?.let {
            AMQP.BasicProperties(
                (it["content_type"] ?: "text/plain").toString(),
                null as String?, null, 1, 0, null as String?, null as String?, null as String?,
                null as String?, null as Date?, null as String?, null as String?, null as String?, null as String?
            )
        }

    abstract class Builder<T : BaseRmqTestStep> : DefaultTestStep.Builder<T>() {

        protected var routingKey: String? = null
        protected var properties: Map<String, Any?>? = null

        fun routingKey(routingKey: String?) = apply {
            this.routingKey = routingKey
        }

        fun properties(properties: Map<String, Any?>?) = apply {
            this.properties = properties
        }

    }
}