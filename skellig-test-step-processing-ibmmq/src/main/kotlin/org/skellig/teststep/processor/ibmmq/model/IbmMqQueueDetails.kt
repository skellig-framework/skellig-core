package org.skellig.teststep.processor.ibmmq.model

class IbmMqQueueDetails private constructor(
    val id: String,
    val queueName: String,
    val ibmMqManagerDetails: IbmMqManagerDetails) {

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