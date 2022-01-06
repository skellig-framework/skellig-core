package org.skellig.teststep.processor.performance.metrics.default

import org.skellig.teststep.processor.performance.metrics.MessageReceptionMetric
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

open class MessageReceptionDefaultMetric(private val name: String) : MessageReceptionMetric {

    private val timeSeriesSuccessfulReceptions = ConcurrentHashMap<Long, AtomicInteger>()
    private val timeSeriesFailedReceptions = ConcurrentHashMap<Long, AtomicInteger>()

    override fun registerMessageReception() {
        timeSeriesSuccessfulReceptions.computeIfAbsent(getCurrentTimeInSeconds()) { AtomicInteger(0) }.incrementAndGet()
    }

    override fun registerMessageFailed() {
        timeSeriesFailedReceptions.computeIfAbsent(getCurrentTimeInSeconds()) { AtomicInteger(0) }.incrementAndGet()
    }

    override fun consumeTimeSeriesRecords(recordConsumer: (String) -> Unit) {
        recordConsumer("\nname: $name")
        timeSeriesSuccessfulReceptions.forEach { recordConsumer("\n${it.key}=${it.value.get()}") }
        recordConsumer("\ntotal passed: ${getTotalPassedRequests()}")
        timeSeriesFailedReceptions.forEach { recordConsumer("\n${it.key}=${it.value.get()}") }
        recordConsumer("\ntotal failed: ${getTotalFailedRequests()}")
    }

    private fun getTotalRequests() = timeSeriesSuccessfulReceptions.values.sumOf { it.get() }

    private fun getTotalPassedRequests() = getTotalRequests() - getTotalFailedRequests()

    private fun getTotalFailedRequests() = timeSeriesFailedReceptions.values.sumOf { it.get() }

    private fun getCurrentTimeInSeconds(): Long = System.currentTimeMillis() / 1000
}