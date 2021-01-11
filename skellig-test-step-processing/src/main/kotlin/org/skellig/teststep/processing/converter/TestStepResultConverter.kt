package org.skellig.teststep.processing.converter

interface TestStepResultConverter {

    fun convert(convertFunction: String, result: Any?): Any?

    fun getConvertFunctionName(): String
}