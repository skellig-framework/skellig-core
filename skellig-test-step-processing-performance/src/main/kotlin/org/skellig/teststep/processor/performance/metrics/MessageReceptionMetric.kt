package org.skellig.teststep.processor.performance.metrics

interface MessageReceptionMetric : TimeSeries {

    fun registerMessageReception()

    fun registerMessageFailed()
}