package org.skellig.runner

import org.junit.runner.RunWith
import org.skellig.runner.SkelligRunner
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.config.TestSkelligContext

@RunWith(SkelligRunner::class)
@SkelligOptions(features = ["feature"],
        testSteps = ["feature", "org.skellig.runner.stepdefs"],
        context = TestSkelligContext::class)
class DefaultSkelligRunnerTest