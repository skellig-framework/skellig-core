package org.skellig.teststep.processing.fixture.duplicate

import org.skellig.teststep.processing.value.function.Function

class DuplicateFunctionsA {

    @Function
    fun same(): String = "A"
}

class DuplicateFunctionsB {

    @Function
    fun same(): String = "B"
}
