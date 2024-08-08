package org.skellig.teststep.processor.performance.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.performance.metrics.default.RequestDurationDefaultMetric
import org.skellig.teststep.processor.performance.metrics.default.RequestDurationPercentileDefaultMetric

class RequestDurationPercentileDefaultMetricTest {

    @Test
    internal fun testTimeSeries() {
        val timeSeries = RequestDurationPercentileDefaultMetric("")

        repeat((0 until 30).count()) {
            val timeSeriesItem = timeSeries.createPercentileBucket()
            repeat((0 until 10).count()) { timeSeriesItem.recordTime(it.toLong()) }
        }

        timeSeries.compress(10)
        (0 until 3).forEach { assertNotNull(timeSeries.timeSeriesItems[it].percentiles) }
        (3 until 30).forEach {
            assertNull(timeSeries.timeSeriesItems[it].percentiles)
        }

        timeSeries.compress(60)
        (3 until 18).forEach { assertNotNull(timeSeries.timeSeriesItems[it].percentiles) }
        (18 until 30).forEach { assertNull(timeSeries.timeSeriesItems[it].percentiles) }

        timeSeries.compress(100)
        (15 until 30).forEach { assertNotNull(timeSeries.timeSeriesItems[it].percentiles) }
    }

    @Test
    fun `close metric`() {
        val timeSeries = RequestDurationPercentileDefaultMetric("")

        val createPercentileBucket = timeSeries.createPercentileBucket() as RequestDurationDefaultMetric

        timeSeries.close()

        // verify compress method called before closure
        assertTrue(createPercentileBucket.percentiles?.all { it == 0L } ?: false, "Invalid items in percentiles")
    }


    @Test
    fun `record failed request and verify correct value returned`() {
        val timeSeries = RequestDurationPercentileDefaultMetric("")

        val createPercentileBucket = timeSeries.createPercentileBucket() as RequestDurationDefaultMetric

        createPercentileBucket.recordRequestStatus(false)

        assertEquals(1, createPercentileBucket.getTotalRequestsFailed())
    }

    @Test
    fun `record failed and success request and verify correct value returned for success ones`() {
        val timeSeries = RequestDurationPercentileDefaultMetric("")

        val createPercentileBucket = timeSeries.createPercentileBucket() as RequestDurationDefaultMetric

        createPercentileBucket.recordTime(10)
        createPercentileBucket.recordRequestStatus(false)
        createPercentileBucket.recordRequestStatus(false)

        assertEquals(2, createPercentileBucket.getTotalRequestsFailed())
        assertEquals(1, createPercentileBucket.getTotalRequests())
    }

    @Test
    fun `record success requests and consume time series records then verify correct values`() {
        val timeSeries = RequestDurationPercentileDefaultMetric("metric1")

        val timeSeriesItem = timeSeries.createPercentileBucket() as RequestDurationDefaultMetric
        (0 until 11).forEach { timeSeriesItem.recordTime(10 * it.toLong()) }
        timeSeriesItem.compress()

        var log = ""
        timeSeries.consumeTimeSeriesRecords {  log += it }

        assertTrue(log.matches(Regex("\nname: metric1\n" +
                "start: \\d+\n" +
                "buckets: 0.0,10.0,25.0,50.0,70.0,80.0,90.0,95.0,99.9,99.99,100.0\n" +
                "11=0,10,20,50,70,80,90,100,100,100,100")))
    }

    @DisplayName("Verify time series item compression")
    @Nested
    inner class PercentilePercentileTimeSeriesItemTest {

        @Test
        internal fun testSimple() {
            val timeSeriesItem = RequestDurationDefaultMetric()

            (0 until 11).forEach { timeSeriesItem.recordTime(10 * it.toLong()) }

            timeSeriesItem.compress()

            assertAll(
                { assertEquals(0, timeSeriesItem.percentiles!![0].toInt()) },
                { assertEquals(10, timeSeriesItem.percentiles!![1].toInt()) },
                { assertEquals(20, timeSeriesItem.percentiles!![2].toInt()) },
                { assertEquals(50, timeSeriesItem.percentiles!![3].toInt()) },
                { assertEquals(70, timeSeriesItem.percentiles!![4].toInt()) },
                { assertEquals(80, timeSeriesItem.percentiles!![5].toInt()) },
                { assertEquals(90, timeSeriesItem.percentiles!![6].toInt()) },
                { assertEquals(100, timeSeriesItem.percentiles!![7].toInt()) },
                { assertEquals(100, timeSeriesItem.percentiles!![8].toInt()) },
                { assertEquals(100, timeSeriesItem.percentiles!![9].toInt()) },
                { assertEquals(100, timeSeriesItem.percentiles!![10].toInt()) },
            )
        }

        @Test
        internal fun testWhen100000Items() {
            val timeSeriesItem = RequestDurationDefaultMetric()

            (0 until 100001).forEach { timeSeriesItem.recordTime(it.toLong()) }

            timeSeriesItem.compress()

            assertAll(
                { assertEquals(0, timeSeriesItem.percentiles!![0].toInt()) },
                { assertEquals(10000, timeSeriesItem.percentiles!![1].toInt()) },
                { assertEquals(25000, timeSeriesItem.percentiles!![2].toInt()) },
                { assertEquals(50000, timeSeriesItem.percentiles!![3].toInt()) },
                { assertEquals(70000, timeSeriesItem.percentiles!![4].toInt()) },
                { assertEquals(80000, timeSeriesItem.percentiles!![5].toInt()) },
                { assertEquals(90000, timeSeriesItem.percentiles!![6].toInt()) },
                { assertEquals(95000, timeSeriesItem.percentiles!![7].toInt()) },
                { assertEquals(99900, timeSeriesItem.percentiles!![8].toInt()) },
                { assertEquals(99990, timeSeriesItem.percentiles!![9].toInt()) },
                { assertEquals(100000, timeSeriesItem.percentiles!![10].toInt()) },
            )
        }

        @Test
        internal fun testWhen500Items() {
            val timeSeriesItem = RequestDurationDefaultMetric()

            (0 until 501).forEach { timeSeriesItem.recordTime(it.toLong()) }

            timeSeriesItem.compress()

            assertAll(
                { assertEquals(0, timeSeriesItem.percentiles!![0].toInt()) },
                { assertEquals(50, timeSeriesItem.percentiles!![1].toInt()) },
                { assertEquals(125, timeSeriesItem.percentiles!![2].toInt()) },
                { assertEquals(250, timeSeriesItem.percentiles!![3].toInt()) },
                { assertEquals(350, timeSeriesItem.percentiles!![4].toInt()) },
                { assertEquals(400, timeSeriesItem.percentiles!![5].toInt()) },
                { assertEquals(450, timeSeriesItem.percentiles!![6].toInt()) },
                { assertEquals(475, timeSeriesItem.percentiles!![7].toInt()) },
                { assertEquals(500, timeSeriesItem.percentiles!![8].toInt()) },
                { assertEquals(500, timeSeriesItem.percentiles!![9].toInt()) },
                { assertEquals(500, timeSeriesItem.percentiles!![10].toInt()) },
            )
        }
    }
}