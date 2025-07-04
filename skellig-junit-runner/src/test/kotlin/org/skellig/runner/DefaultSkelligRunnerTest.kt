package org.skellig.runner

import org.junit.runner.RunWith
import org.skellig.plugin.report.SkelligReportPlugin
import org.skellig.plugin.report.SkelligToCucumberReportPlugin
import org.skellig.runner.annotation.SkelligOptions

@RunWith(SkelligRunner::class)
@SkelligOptions(
    features = ["feature", "tags-tests"],
    testSteps = ["feature", "org.skellig.runner.stepdefs"],
    config = "test.conf"
)
@SkelligOptions.Plugin(name = SkelligReportPlugin::class, args = ["target/report"])
@SkelligOptions.Plugin(name = SkelligToCucumberReportPlugin::class, args = ["target/report/", "cucumber"])
class DefaultSkelligRunnerTest