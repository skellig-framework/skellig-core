package org.skellig.teststep.processing.converter

@FunctionalInterface
interface TestStepValueConverter {

    fun convert(value: String?): Any?
}