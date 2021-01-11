package org.skellig.teststep.processor.rmq.model

class RmqExchangeDetails(val name: String,
                         val type: String?,
                         val isDurable: Boolean,
                         val isAutoDelete: Boolean,
                         val isCreateIfNew: Boolean,
                         val parameters: Map<String, Any>?) {

    class Builder {
        private var name: String? = null
        private var type: String? = null
        private var isDurable = false
        private var isAutoDelete = false
        private var createIfNew = false
        private var parameters: MutableMap<String, Any>? = null

        fun withName(name: String?) = apply {
            this.name = name
        }

        fun withType(type: String?) = apply {
            this.type = type
        }

        fun withDurable(durable: Boolean) = apply {
            isDurable = durable
        }

        fun withAutoDelete(autoDelete: Boolean) = apply {
            isAutoDelete = autoDelete
        }

        fun withCreateIfNew(createIfNew: Boolean) = apply {
            this.createIfNew = createIfNew
        }

        fun withParameters(parameters: MutableMap<String, Any>?) = apply {
            this.parameters = parameters
        }

        fun withParameter(name: String, value: Any) = apply {
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