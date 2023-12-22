package org.skellig.teststep.processing.value.converter

interface TestDataConverter {

    fun convert(data: Any?): Any?

    fun getName(): String

    fun isApplicable(data: Any?): Boolean
}