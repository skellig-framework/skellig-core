package org.skellig.teststep.processor.performance.metrics

interface DurationMetric {

    fun recordTime(time: Long)

}