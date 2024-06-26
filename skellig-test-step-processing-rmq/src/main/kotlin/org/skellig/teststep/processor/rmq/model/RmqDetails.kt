package org.skellig.teststep.processor.rmq.model


/**
 * Represents the details of an RMQ connection, including the host details, exchange details, and queue details.
 *
 * @property hostDetails The details of the RMQ host.
 * @property exchange The details of the RMQ exchange.
 * @property queue The details of the RMQ queue.
 */
class RmqDetails private constructor(val hostDetails: RmqHostDetails,
                                     val exchange: RmqExchangeDetails,
                                     val queue: RmqQueueDetails) {

    override fun toString(): String {
        return "$hostDetails:$exchange:$queue"
    }

    class Builder {
        private var hostDetails: RmqHostDetails? = null
        private var exchange: RmqExchangeDetails? = null

        private var queue: RmqQueueDetails? = null

        fun hostDetails(hostDetails: RmqHostDetails?) = apply {
            this.hostDetails = hostDetails
        }

        fun exchange(exchange: RmqExchangeDetails?) = apply {
            this.exchange = exchange
        }

        fun queue(queue: RmqQueueDetails?) = apply {
            this.queue = queue
        }

        fun build(): RmqDetails {
            return RmqDetails(hostDetails!!, exchange!!, queue!!)
        }
    }
}