package org.skellig.feature.invalid

import org.skellig.feature.hook.annotation.BeforeTestScenario

class HookClassWithArgsConstructor(val data: String) {

    @BeforeTestScenario
    fun beforeScenario() {
    }
}