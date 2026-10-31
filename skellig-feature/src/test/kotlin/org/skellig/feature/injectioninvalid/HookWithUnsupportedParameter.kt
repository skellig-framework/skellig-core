package org.skellig.feature.injectioninvalid

import org.skellig.feature.hook.annotation.BeforeTestScenario

class HookWithUnsupportedParameter {

    @BeforeTestScenario
    fun withObject(state: Any) {
    }
}
