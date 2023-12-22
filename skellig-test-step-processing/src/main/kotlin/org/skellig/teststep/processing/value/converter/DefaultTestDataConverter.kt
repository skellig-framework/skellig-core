package org.skellig.teststep.processing.value.converter

import org.skellig.teststep.processing.exception.TestDataConversionException

class DefaultTestDataConverter private constructor(
    private val converters: Map<String, TestDataConverter>,
    private val defaultConverter: String?
) : TestDataConverter {

    override fun convert(data: Any?): Any? {
        return converters.values.find { it.isApplicable(data) }?.convert(data) ?: defaultConverter?.let { convert(it, data) } ?: data
    }

    override fun getName(): String = ""

    override fun isApplicable(data: Any?): Boolean = true

    private fun convert(converterName: String, data: Any?): Any? {
        return data?.let {
            if (converters.containsKey(converterName)) converters[converterName]?.convert(data)
            else throw TestDataConversionException(
                "No data converter found for name '$converterName' " +
                        "from the registered ones: '${converters.keys}'"
            )
        }
    }

    class Builder {

        private val converters = mutableMapOf<String, TestDataConverter>()
        private var classLoader: ClassLoader? = null
        private var defaultConverter: String? = null

        fun withClassLoader(classLoader: ClassLoader?) = apply { this.classLoader = classLoader }

        fun withTestDataConverter(testDataConverter: TestDataConverter) =
            apply { converters[testDataConverter.getName()] = testDataConverter }

        fun withDefaultConverter(converterName: String) = apply { this.defaultConverter = converterName }

        fun build(): TestDataConverter {
            val converter = DefaultTestDataConverter(converters, defaultConverter)

            this.withTestDataConverter(TestDataToJsonConverter())
            this.withTestDataConverter(TestDataFromTemplateConverter(classLoader ?: error("No classloader provided for the DefaultTestDataConverter")))
            this.withTestDataConverter(TestDataToBytesConverter())

            return converter
        }
    }
}