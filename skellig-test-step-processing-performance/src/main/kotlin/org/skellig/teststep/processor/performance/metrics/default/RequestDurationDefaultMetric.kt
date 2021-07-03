package org.skellig.teststep.processor.performance.metrics.default

import org.skellig.teststep.processor.performance.metrics.DurationMetric
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger

open class RequestDurationDefaultMetric : DurationMetric {

    companion object {
        val bucketPercentages = listOf(0.0, 0.1, 0.25, 0.5, 0.7, 0.8, 0.9, 0.95, 0.999, 0.9999, 1.0)
    }

    private var elapsedTimeTmpSet: MutableSet<Long>? = ConcurrentSkipListSet()
    private val totalRequestsFailed = AtomicInteger()
    private var totalRequests = AtomicInteger()

    var percentiles: LongArray? = null
        private set
        get() = field

    override fun recordTime(time: Long) {
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