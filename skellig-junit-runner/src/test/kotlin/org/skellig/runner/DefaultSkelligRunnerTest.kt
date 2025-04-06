package org.skellig.runner

import org.junit.runner.RunWith
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.plugin.SkelligReportPlugin

@RunWith(SkelligRunner::class)
@SkelligOptions(
    features = ["feature", "tags-tests"],
    testSteps = ["feature", "org.skellig.runner.stepdefs"],
    config = "test.conf"
)
@SkelligOptions.Plugin(name = SkelligReportPlugin::class, args = ["target/report"])
class DefaultSkelligRunnerTest