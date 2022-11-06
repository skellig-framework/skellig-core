package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
import java.util.*

class DefaultValueConverter private constructor(
    private val valueConverters: List<TestStepValueConverter>,
    private val extractor: TestStepValueExtractor
) : TestStepValueConverter {

    companion object {
        private val notToParseValues = mutableSetOf<String>()
    }

    override fun convert(value: Any?): Any? {
        val newValue = when (value) {
            is Map<*, *> -> value.entries.map { it.key to convert(it.value) }.toMap()
            is Collection<*> -> value.map { convert(it) }.toList()
            else -> innerConvert(value)
        }
        // If collection then go through conversion again to avoid missed function processing
        return if (newValue is Map<*, *> || newValue is Collection<*>) innerConvert(newValue)
        else newValue
    }

    private fun innerConvert(value: Any?): Any? =
        when (value) {
            is String -> {
                try {
                    if (!notToParseValues.contains(value)) {
                        val convertedValue = processAndConvertStringValue(value)
                        if (convertedValue == value) notToParseValues.add(value)
                        convertedValue
                    } else value
                } catch (ex: ValueExtractionException) {
                    throw ValueExtractionException("${ex.message}. Original value: $value")
                }
            }
            else -> convertWithExtractions(value, null)
        }

    private fun processAndConvertStringValue(value: String): Any? {
        var chunk = ""
        var currentChunkPosition = -1
        var chunks: MutableList<Any?>? = null
        var isInsideQuotes = false
        var closingGroups = 0
        var i = 0
        while (i < value.length) {
            when (value[i]) {
                '"', '\'' -> {
                    if (i == 0 || value[i - 1] != '\\') {
                        isInsideQuotes = !isInsideQuotes
                    }
                    // we still need to keep ' or " because they might be part of extraction path
                    // which will be processed in method 'convertValue'.
                    // Even if not in extraction path, these quotes will be dropped by 'convertValue'.
                    chunk += value[i]
                }
                '#' -> {
                    if (!isInsideQuotes && value[i + 1] == '[') {
                        if (chunk.isNotEmpty()) {
                            chunks = addToChunks(chunk, chunks)
                            chunk = ""
                            currentChunkPosition++
                        }
                        chunk += value[i]
                        closingGroups++
                    } else {
                        chunk += value[i]
                    }
                }
                ']' -> {
                    if (!isInsideQuotes && closingGroups > 0) {
                        chunk += value[i]
                        chunks = addToChunks(chunk, chunks)
                        chunk = ""
                        currentChunkPosition++

                        chunk = pullChunksFromLeft(currentChunkPosition, chunk, chunks)
                        chunks[currentChunkPosition] = convertValue(chunk, closingGroups > 0)

                        chunk = ""
                        closingGroups--
                    } else {
                        chunk += value[i]
                    }
                }
                else -> {
                    chunk += value[i]
                }
            }
            i++
        }

        return if (chunks == null) {
            convertValue(chunk, false)
        } else {
            if (chunks.size == 1 && chunk == "") chunks[0]
            else chunks.joinToString("") + chunk
        }
    }

    private fun pullChunksFromLeft(
        index: Int,
        chunk: String,
        chunks: MutableList<Any?>
    ): String {
        var newChunk = chunk
        for (j in index downTo 0) {
            val chunkAsString = chunks[j].toString()
            newChunk = chunkAsString + newChunk
            if (chunkAsString.length > 1 && chunkAsString[0] == '#' && chunkAsString[1] == '[') {
                chunks[j] = ""
                break;
            } else {
                chunks[j] = ""
            }
        }
        return newChunk
    }

    private fun addToChunks(chunk: String, chunks: MutableList<Any?>?): MutableList<Any?> {
        var newChunks = chunks
        if (newChunks == null) newChunks = mutableListOf()
        newChunks.add(chunk)
        return newChunks
    }

    private fun convertValue(value: String, excludeWrappingChars: Boolean = true): Any? {
        var valueToConvert = ""
        var extractionPath = ""
        var isExtractionPath = false
        var isInsideQuotes = false
        var i = (if (excludeWrappingChars) 2 else 0)
        while (i < value.length - (if (excludeWrappingChars) 1 else 0)) {
            if (isExtractionPath) {
                extractionPath += value[i]
            } else {
                when (value[i]) {
                    '"', '\'' -> {
                        isInsideQuotes = !isInsideQuotes
                    }
                    '\\' -> {
                        if (i < value.length && isQuotes(value[i + 1])) {
                            valueToConvert += value[i + 1]
                            i++
                        } else valueToConvert += value[i]
                    }
                    '.' -> {
                        if (!isInsideQuotes) isExtractionPath = true
                        else valueToConvert += value[i]
                    }
                    else -> valueToConvert += value[i]
                }
            }
            i++
        }

        return convertWithExtractions(valueToConvert, extractionPath)
    }

    private fun isQuotes(character : Char) = character == '\'' || character == '"'

    private fun convertWithExtractions(valueToConvert: Any?, extractionPath: String?): Any? {
        var result: Any? = valueToConvert
        for (valueConverter in valueConverters) {
            result = valueConverter.convert(result)
        }
        return extractionPath?.let { extractor.extract(result, extractionPath) } ?: result
    }

    class Builder {

        private val valueConverters = mutableListOf<TestStepValueConverter>()
        private var testScenarioState: TestScenarioState? = null
        private var testStepValueExtractor: TestStepValueExtractor? = null
        private var getPropertyFunction: ((String) -> Any?)? = null
        private var classLoader: ClassLoader? = null
        private var classPaths: Collection<String>? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) =
            apply { this.testScenarioState = testScenarioState }

        fun withGetPropertyFunction(getPropertyFunction: ((String) -> Any?)?) =
            apply { this.getPropertyFunction = getPropertyFunction }

        fun withClassLoader(classLoader: ClassLoader?) = apply { this.classLoader = classLoader }

        fun withClassPaths(classPaths: Collection<String>) = apply { this.classPaths = classPaths }

        fun withTestStepValueExtractor(testStepValueExtractor: TestStepValueExtractor?) =
            apply { this.testStepValueExtractor = testStepValueExtractor }

        fun withValueConverter(valueConverter: TestStepValueConverter) = apply { valueConverters.add(valueConverter) }

        fun build(): TestStepValueConverter {
            Objects.requireNonNull(testScenarioState, "Test Scenario State must be provided")

            val allValueConverters = mutableListOf<TestStepValueConverter>()
            val defaultValueConverter = DefaultValueConverter(
                allValueConverters,
                testStepValueExtractor ?: error("Test Step Value Extractor must be provided")
            )

            allValueConverters.add(TestDataFromIfStatementConverter())
            allValueConverters.add(TestStepStateValueConverter(testScenarioState!!, testStepValueExtractor))
            allValueConverters.add(FindFromStateValueConverter(testScenarioState!!, testStepValueExtractor))
            classLoader?.let {
                withValueConverter(FileValueConverter(it))
            }

            allValueConverters.add(NumberValueConverter())
            allValueConverters.add(RandomValueConverter())
            allValueConverters.add(IncrementValueConverter())
            allValueConverters.add(CurrentDateTimeValueConverter())
            allValueConverters.add(ToDateTimeValueConverter())
            allValueConverters.add(ListOfValueConverter())
            allValueConverters.add(TestDataToBytesConverter(defaultValueConverter))
            allValueConverters.add(TestDataToJsonConverter())
            classLoader?.let {
                val testDataFromCsvConverter = TestDataFromCsvConverter(it)
                allValueConverters.add(testDataFromCsvConverter)
                allValueConverters.add(TestDataFromCsvConverter(it))
                allValueConverters.add(TestDataFromFTLConverter(it, testDataFromCsvConverter))
                allValueConverters.add(CustomFunctionValueConverter(classPaths, it))
            }

            allValueConverters.addAll(valueConverters)

            return defaultValueConverter
        }
    }
}