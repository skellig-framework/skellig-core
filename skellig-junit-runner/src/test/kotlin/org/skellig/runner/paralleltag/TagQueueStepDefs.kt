package org.skellig.runner.paralleltag

import org.skellig.feature.hook.annotation.*
import org.skellig.runner.parallelrun.ParallelRunRecorder
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.runner.annotation.TestStep

class TagQueueStepDefs {
    @BeforeTestFeature(tags = ["@A"])
    fun beforeA(scope: TestScenarioScope) = record(scope, "before A")

    @AfterTestFeature(tags = ["@A"])
    fun afterA(scope: TestScenarioScope) = record(scope, "after A")

    @BeforeTestFeature
    fun beforeCommon(scope: TestScenarioScope) {
        scope.state.set("featureValue", "setup")
        record(scope, "before common")
    }

    @AfterTestFeature
    fun afterCommon(scope: TestScenarioScope) {
        check(scope.state.get("featureValue") == "setup") { "Feature setup state was lost before teardown" }
        check(scope.state.get("scenarioValue") == null) { "Scenario state leaked into feature teardown" }
        record(scope, "after common")
    }

    @BeforeTestFeature(tags = ["@FailSetup"])
    fun failSetup() { error("Feature setup failed") }

    @BeforeTestFeature(tags = ["@Unrelated"])
    fun unrelated() { error("Unrelated feature hook must not run") }

    @BeforeTestScenario
    fun beforeScenario(scope: TestScenarioScope) = record(scope, "before scenario")

    @AfterTestScenario
    fun afterScenario(scope: TestScenarioScope) = record(scope, "after scenario")

    @TestStep(name = "Record tagged scenario (\\w+)")
    fun scenario(name: String, scope: TestScenarioScope) {
        check(scope.state.get("featureValue") == null) { "Feature state leaked into a scenario" }
        check(scope.state.get("scenarioValue") == null) { "Previous scenario state leaked into this scenario" }
        scope.state.set("scenarioValue", name)
        record(scope, name)
    }

    @TestStep(name = "Record tagged feature (setup|teardown)")
    fun feature(phase: String, scope: TestScenarioScope) = record(scope, phase)

    private fun record(scope: TestScenarioScope, what: String) {
        ParallelRunRecorder.record(scope.parallelGroup.key, what, this)
    }
}
