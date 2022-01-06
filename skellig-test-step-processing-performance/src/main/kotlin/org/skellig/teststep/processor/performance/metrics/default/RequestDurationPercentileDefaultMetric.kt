package org.skellig.teststep.processor.performance.metrics.default

import org.skellig.teststep.processor.performance.metrics.DurationMetric
import org.skellig.teststep.processor.performance.metrics.DurationPercentileMetric
import org.slf4j.LoggerFactory
import kotlin.concurrent.fixedRateTimer
import kotlin.system.measureTimeMillis

open class RequestDurationPercentileDefaultMetric(private val name: String) : DurationPercentileMetric {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RequestDurationPercentileDefaultMetric::class.java)
        private const val COMPRESSION_TIME_PERIOD = 120 * 1000L
        private const val TIME_SERIES_COMPRESSION_PERCENTAGE = 80
    }

    internal val timeSeriesItems = mutableListOf<RequestDurationDefaultMetric>()
    private var startRangeIndex = 0
    private val startTime = System.currentTimeMillis() / 1000

    private val timer = fixedRateTimer("timer", false, COMPRESSION_TIME_PERIOD, COMPRESSION_TIME_PERIOD) {
        measureTimeMillis {
            compress(TIME_SERIES_COMPRESSION_PERCENTAGE)
        }.also { LOGGER.debug("Successfully compressed time series data in $it ms") }
    }

    override fun createPercentileBucket(): DurationMetric {
        val timeSeriesItem = RequestDurationDefaultMetric()
        timeSeriesItems.add(timeSeriesItem)
        return timeSeriesItem
    }

    override fun close() {
        timer.cancel()
        compress()
    }

    override fun consumeTimeSeriesRecords(recordConsumer: (String) -> Unit) {
        recordConsumer("\nname: $name")
        recordConsumer("\nstart: $startTime")
        recordConsumer("\nbuckets: ${RequestDurationDefaultMetric.bucketPercentages.joinToString(",") { (it * 100).toString() }}")
        timeSeriesItems.forEach {
            recordConsumer("\n${it.getTotalRequests()}=${it.percentiles?.joinToString(",") { p -> p.toString() }}")
        }
    }

    internal fun compress(itemsPercentage: Int = 100) {
        if (itemsPercentage in 1..100) {
            val lastRangeIndex = ((itemsPercentage / 100.0) * timeSeriesItems.size).toInt()
            (startRangeIndex until lastRangeIndex).forEach { timeSeriesItems[it].compress() }
            startRangeIndex = lastRangeIndex
        }
    }

    private fun getTotalRequests() = timeSeriesItems.sumOf { it.getTotalRequests() }

    private fun getTotalPassedRequests() = getTotalRequests() - getTotalFailedRequests()

    private fun getTotalFailedRequests() = timeSeriesItems.sumOf { it.getTotalRequestsFailed() }

}