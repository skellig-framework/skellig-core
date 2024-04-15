package org.skellig.teststep.processor.ibmmq.model

/**
 * Represents the details of an IBM MQ Queue.
 *
 * @property id The ID of the IBM MQ Queue.
 * @property queueName The name of the IBM MQ Queue.
 * @property ibmMqManagerDetails The details of the IBM MQ Manager associated with this queue.
 */
class IbmMqQueueDetails private constructor(
    val id: String,
    val queueName: String,
    val ibmMqManagerDetails: IbmMqManagerDetails) {

    override fun toString(): String {
        return "(id = '$id', queue = '$queueName', IBMMQ Manager $ibmMqManagerDetails)"
    }

    class Builder {
        private var id: String? = null
        private var queueName: String? = null

        private var ibmMqManagerDetails: IbmMqManagerDetails? = null

        fun id(id: String?) = apply {
            this.id = id
        }

        fun name(queueName: String?) = apply {
            this.queueName = queueName
        }

        fun mqManagerDetails(ibmMqManagerDetails: IbmMqManagerDetails?) = apply {
            this.ibmMqManagerDetails = ibmMqManagerDetails
        }
        fun build(): IbmMqQueueDetails {
            return IbmMqQueueDetails(id ?: queueName ?: "",
                                     queueName ?: error("IBMMQ Queue name cannot be null"),
                                     ibmMqManagerDetails ?: error("IBMMQ Queue manager cannot be null"))
        }
    }
}