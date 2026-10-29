package org.skellig.teststep.runner.fixture.overload

import org.skellig.teststep.runner.annotation.TestStep

class OverloadedSteps {

    fun step(a: String): String = "unannotated-1:$a"

    @TestStep(id = "overloaded", name = "overloaded step")
    fun step(a: String, b: String): String = "annotated:$a$b"

    fun step(a: String, b: String, c: String): String = "unannotated-3:$a$b$c"
}
