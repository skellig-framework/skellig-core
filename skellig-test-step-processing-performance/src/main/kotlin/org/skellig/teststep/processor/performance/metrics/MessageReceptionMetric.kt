package org.skellig.teststep.processor.performance.metrics

/**
 * Represents a metric for measuring message reception.
 */
interface MessageReceptionMetric : TimeSeries {

    /**
     * Registers a successful message reception in the message reception metric.
     *
     * This method should be called when a message is successfully received.
     * It increments the counter in the message reception metric, indicating that a message has been received.
     *
     * @see MessageReceptionMetric
     */
    fun registerMessageReception()

    /**
     * Registers a failed message reception in the message reception metric.
     *
     * This method should be called when a message fails to be received.
     * It increments the counter in the message reception metric, indicating that a message has failed to be received.
     *
     * @see MessageReceptionMetric
     */
    fun registerMessageFailed()
}