package org.skellig.runner

import org.junit.runner.RunWith
import org.skellig.runner.annotation.SkelligOptions

@RunWith(SkelligRunner::class)
@SkelligOptions(features = ["feature/test-feature.sf"],
        testSteps = ["feature", "org.skellig.runner.stepdefs"],
        config = "test.conf")
class DefaultSkelligRunnerTest