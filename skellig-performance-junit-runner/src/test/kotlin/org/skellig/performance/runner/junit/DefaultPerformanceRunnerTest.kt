package org.skellig.performance.runner.junit

import org.junit.runner.RunWith
import org.skellig.performance.runner.junit.annotation.SkelligPerformanceOptions

@RunWith(SkelligPerformanceRunner::class)
@SkelligPerformanceOptions(
    testName = "check performance of storing data in state",
    testSteps = ["steps", "org.skellig.performance"],
    config = "test.conf")
class DefaultPerformanceRunnerTest