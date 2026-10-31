package org.skellig.teststep.processing.fixture.instances

import org.skellig.teststep.processing.value.function.Function

class CountingFunctions {

    private var calls = 0

    @Function
    fun nextCall(): Int = ++calls

    @Function
    fun otherFunctionOfTheSameClass(): Int = ++calls
}
