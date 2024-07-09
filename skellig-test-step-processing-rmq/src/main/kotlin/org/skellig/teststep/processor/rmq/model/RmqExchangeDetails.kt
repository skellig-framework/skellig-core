package org.skellig.teststep.processor.rmq.model

/**
 * Represents the details of an exchange in RabbitMQ.
 *
 * @property name The name of the exchange.
 * @property type The type of the exchange. Can be null.
 * @property isDurable Specifies if the exchange is durable or not.
 * @property isAutoDelete Specifies if the exchange is auto-delete or not.
 * @property isCreateIfNew Specifies if the exchange should be created if it doesn't exist.
 * @property parameters Additional parameters for the exchange. Can be null.
 */
class RmqExchangeDetails(val name: String,
                         val type: String?,
                         val isDurable: Boolean,
                         val isAutoDelete: Boolean,
                         val isCreateIfNew: Boolean,
                         val parameters: Map<String, Any>?) {

    override fun toString(): String {
        return name
    }

    class Builder {
        private var name: String? = null
        private var type: String? = null
        private var isDurable = false
        private var isAutoDelete = false
        private var createIfNew = false
        private var parameters: MutableMap<String, Any>? = null

        fun name(name: String?) = apply {
            this.name = name
        }

        fun type(type: String?) = apply {
            this.type = type
        }

        fun durable(durable: Boolean) = apply {
            isDurable = durable
        }

        fun autoDelete(autoDelete: Boolean) = apply {
            isAutoDelete = autoDelete
        }

        fun createIfNew(createIfNew: Boolean) = apply {
            this.createIfNew = createIfNew
        }

        fun parameters(parameters: MutableMap<String, Any>?) = apply {
            this.parameters = parameters
        }

        fun build(): RmqExchangeDetails {
            return RmqExchangeDetails(name!!, type, isDurable, isAutoDelete, createIfNew, parameters)
        }
    }
}