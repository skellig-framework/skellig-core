package org.skellig.teststep.processor.rmq.model

class RmqQueueDetails(val name: String,
                      val routingKey: String?,
                      val isDurable: Boolean,
                      val isExclusive: Boolean,
                      val isAutoDelete: Boolean,
                      val isCreateIfNew: Boolean,
                      val parameters: Map<String, Any>?) {

    class Builder {
        private var name: String? = null
        private var routingKey: String? = null
        private var isDurable = true
        private var isExclusive = false
        private var isAutoDelete = false
        private var createIfNew = false
        private var parameters: MutableMap<String, Any>? = null

        fun withName(name: String?) = apply {
            this.name = name
        }

        fun withRoutingKey(routingKey: String?) = apply {
            this.routingKey = routingKey
        }

        fun withDurable(isDurable: Boolean) = apply {
            this.isDurable = isDurable
        }

        fun withExclusive(isExclusive: Boolean) = apply {
            this.isExclusive = isExclusive
        }

        fun withAutoDelete(isAutoDelete: Boolean) = apply {
            this.isAutoDelete = isAutoDelete
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

        fun build(): RmqQueueDetails {
            return RmqQueueDetails(name!!, routingKey, isDurable, isExclusive, isAutoDelete, createIfNew, parameters)
        }
    }
}