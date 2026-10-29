package org.skellig.teststep.processing.fixture.overload

import org.skellig.teststep.processing.value.function.Function

class OverloadedFunctions {

    fun combine(a: String): String = "unannotated-1:$a"

    @Function
    fun combine(a: String, b: String): String = "annotated:$a$b"

    fun combine(a: String, b: String, c: String): String = "unannotated-3:$a$b$c"
}
