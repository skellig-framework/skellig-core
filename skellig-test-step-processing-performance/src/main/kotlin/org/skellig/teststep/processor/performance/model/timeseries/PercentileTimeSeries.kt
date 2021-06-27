package org.skellig.teststep.processor.performance.model.timeseries

import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

open class PercentileTimeSeries(private val name: String) : TimeSeries {

    private val timeSeriesItems = mutableListOf<PercentileTimeSeriesItem>()
    private var startRangeIndex = 0
    private val startTime = System.currentTimeMillis() / 1000

    fun add(timeSeriesItem: PercentileTimeSeriesItem) {
        timeSeriesItems.add(timeSeriesItem)
    }

    fun compress(itemsPercentage: Int = 100) {
        if (itemsPercentage in 1..100) {
            val lastRangeIndex = ((itemsPercentage / 100.0) * timeSeriesItems.size).toInt()
            (startRangeIndex until lastRangeIndex).forEach { timeSeriesItems[it].compress() }
            startRangeIndex = lastRangeIndex
        }
    }

    override fun consumeTimeSeriesRecords(recordConsumer: (String) -> Unit) {
        recordConsumer("name: $name")
        recordConsumer("\nstart: $startTime")
        recordConsumer("\nbuckets: ${PercentileTimeSeriesItem.bucketPercentages.joinToString(",") { (it * 100).toString() }}")
        timeSeriesItems.forEach {
            recordConsumer("\n${it.getTotalRequests()}=${it.percentiles?.joinToString(",") { p -> p.toString() }}")
        }
    }

    private fun getTotalRequests() = timeSeriesItems.sumOf { it.getTotalRequests() }

    private fun getTotalPassedRequests() = getTotalRequests() - getTotalFailedRequests()

    private fun getTotalFailedRequests() = timeSeriesItems.sumOf { it.getTotalRequestsFailed() }

}

open class PercentileTimeSeriesItem {

    companion object {
        val bucketPercentages = listOf(0.0, 0.1, 0.25, 0.5, 0.7, 0.8, 0.9, 0.95, 0.999, 0.9999, 1.0)
    }

    private var elapsedTimeTmpSet: MutableSet<Long>? = ConcurrentSkipListSet()
    private val totalRequestsFailed = AtomicLong()
    private var totalRequests = AtomicInteger()

    var percentiles: LongArray? = null
        private set
        get() = field

    fun recordTime(time: Long) {
        elapsedTimeTmpSet?.add(time)
        totalRequests.incrementAndGet()
    }

    fun recordRequestStatus(isPassed: Boolean) {
        if (!isPassed) totalRequestsFailed.incrementAndGet()
    }

    fun getTotalRequests(): Int = totalRequests.get()

    fun compress() {
        elapsedTimeTmpSet?.let {
            val elapsedTimeExtractor = ElapsedTimeExtractor(it.iterator())
            percentiles =
                bucketPercentages.map { item ->
                    elapsedTimeExtractor.getLatestTimeForBucket(item)
                }.toLongArray()
            elapsedTimeTmpSet = null
        }
    }

    fun getTotalRequestsFailed() = totalRequestsFailed.toLong()

    inner class ElapsedTimeExtractor(private val timesIterator: MutableIterator<Long>) {

        private var startIndex = 0
        private var lastTakenItem: Long = 0

        internal fun getLatestTimeForBucket(percentileBucket: Double): Long {
            val timeSetSize = elapsedTimeTmpSet!!.size
            var index = (percentileBucket * timeSetSize).toInt()
            index = if (index < timeSetSize) index else timeSetSize - 1

            while (timesIterator.hasNext() && startIndex <= index) {
                lastTakenItem = timesIterator.next()
                startIndex++
            }
            return lastTakenItem
        }
    }

}