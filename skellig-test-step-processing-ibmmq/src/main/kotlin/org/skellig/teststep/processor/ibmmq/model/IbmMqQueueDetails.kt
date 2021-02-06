package org.skellig.teststep.processor.ibmmq.model

class IbmMqQueueDetails private constructor(val queueName: String,
                                            val ibmMqManagerDetails: IbmMqManagerDetails) {

    class Builder {
        private var queueName: String? = null
        private var ibmMqManagerDetails: IbmMqManagerDetails? = null

        fun name(queueName: String?) = apply {
            this.queueName = queueName
        }

        fun mqManagerDetails(ibmMqManagerDetails: IbmMqManagerDetails?) = apply {
            this.ibmMqManagerDetails = ibmMqManagerDetails
        }

        fun build(): IbmMqQueueDetails {
            return IbmMqQueueDetails(queueName ?: error("Queue name cannot be null"),
                    ibmMqManagerDetails ?: error("Queue manager cannot be null"))
        }
    }
}