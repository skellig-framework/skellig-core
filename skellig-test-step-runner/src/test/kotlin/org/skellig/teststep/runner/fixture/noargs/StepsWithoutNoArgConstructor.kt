package org.skellig.teststep.runner.fixture.noargs

import org.skellig.teststep.runner.annotation.TestStep

class StepsWithoutNoArgConstructor(@Suppress("unused") val data: String) {

    @TestStep(name = "no default constructor")
    fun step() {
    }
}
