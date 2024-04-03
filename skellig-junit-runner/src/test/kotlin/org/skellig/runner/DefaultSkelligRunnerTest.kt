package org.skellig.runner

import org.junit.runner.RunWith
import org.skellig.runner.annotation.SkelligOptions

@RunWith(SkelligRunner::class)
@SkelligOptions(features = ["feature/task-test-feature.sf"],
        testSteps = ["feature/task-test-steps.sts", "org.skellig.runner.stepdefs"],
        config = "test.conf")
class DefaultSkelligRunnerTest