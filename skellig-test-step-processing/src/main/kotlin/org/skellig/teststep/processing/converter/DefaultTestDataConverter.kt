package org.skellig.teststep.processing.converter

import java.util.*

class DefaultTestDataConverter private constructor(private val testDataConverters: List<TestDataConverter>) : TestDataConverter {

    override fun convert(testData: Any?): Any? {
        var result = testData
        for (testDataConverter in testDataConverters) {
            if (result === testData) {
                result = testDataConverter.convert(result)
            } else {
                break
            }
        }
        return result
    }

    class Builder {

        private val testDataConverters: MutableList<TestDataConverter> = arrayListOf()
        private var classLoader: ClassLoader? = null

        fun withClassLoader(classLoader: ClassLoader?) = apply { this.classLoader = classLoader }

        fun withTestDataConverter(testDataConverter: TestDataConverter) = apply { testDataConverters.add(testDataConverter) }

        fun build(): TestDataConverter {
            Objects.requireNonNull(classLoader, "ClassLoader must be provided")

            val testDataFromCsvConverter = TestDataFromCsvConverter(classLoader!!)
            testDataConverters.add(TestDataToJsonConverter())
            testDataConverters.add(TestDataFromFTLConverter(classLoader!!, testDataFromCsvConverter))
            testDataConverters.add(TestDataFromIfStatementConverter())
            testDataConverters.add(testDataFromCsvConverter)
            testDataConverters.add(TestDataToBytesConverter())

            return DefaultTestDataConverter(testDataConverters)
        }
    }
}