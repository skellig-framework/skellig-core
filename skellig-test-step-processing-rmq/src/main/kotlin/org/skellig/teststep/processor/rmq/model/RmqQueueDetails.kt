package org.skellig.teststep.processor.rmq.model


/**
 * Represents the details of a RabbitMQ queue.
 *
 * @property id The unique identifier of the queue.
 * @property name The name of the queue.
 * @property routingKey The routing key of the queue.
 * @property isDurable Indicates whether the queue is durable or not.
 * @property isExclusive Indicates whether the queue is exclusive or not.
 * @property isAutoDelete Indicates whether the queue is auto-deleted or not.
 * @property isCreateIfNew Indicates whether the queue should be created if it doesn't exist.
 * @property parameters The additional parameters of the queue.
 */
class RmqQueueDetails(val id: String,
                      val name: String,
                      val routingKey: String?,
                      val isDurable: Boolean,
                      val isExclusive: Boolean,
                      val isAutoDelete: Boolean,
                      val isCreateIfNew: Boolean,
                      val parameters: Map<String, Any>?) {

    override fun toString(): String {
        return "$id:${routingKey ?: "#"}"
    }

    class Builder {
        private var id: String? = null
        private var name: String? = null
        private var routingKey: String? = null
        private var isDurable = true
        private var isExclusive = false
        private var isAutoDelete = false
        private var createIfNew = false
        private var parameters: MutableMap<String, Any>? = null

        fun id(id: String?) = apply {
            this.id = id
        }

        fun name(name: String?) = apply {
            this.name = name
        }

        fun routingKey(routingKey: String?) = apply {
            this.routingKey = routingKey
        }

        fun durable(isDurable: Boolean) = apply {
            this.isDurable = isDurable
        }

        fun exclusive(isExclusive: Boolean) = apply {
            this.isExclusive = isExclusive
        }

        fun autoDelete(isAutoDelete: Boolean) = apply {
            this.isAutoDelete = isAutoDelete
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

        fun build(): RmqQueueDetails {
            return RmqQueueDetails(id ?: name ?: "",
                                   name?:error("RMQ Queue name cannot be null"),
                                   routingKey, isDurable, isExclusive, isAutoDelete, createIfNew, parameters)
        }

    }
}