package org.skellig.teststep.processor.rmq.model

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

        fun parameter(name: String, value: Any) = apply {
            if (parameters == null) {
                parameters = hashMapOf()
            }
            parameters!![name] = value
        }

        fun build(): RmqExchangeDetails {
            return RmqExchangeDetails(name!!, type, isDurable, isAutoDelete, createIfNew, parameters)
        }
    }
}