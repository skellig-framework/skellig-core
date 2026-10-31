package org.skellig.teststep.runner.fixture.mismatchmap

import org.skellig.teststep.runner.annotation.TestStep

class ObjectParameterSteps {

    // Any is not injected, so it counts as a regex group and there is one more parameter than groups
    @TestStep(name = "has no groups")
    fun objectIsNotInjected(state: Any, other: String) {
    }
}
