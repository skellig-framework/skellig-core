package org.skellig.teststep.processing.converter

import de.siegmar.fastcsv.reader.CsvContainer
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.CsvRow
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.experiment.FunctionValueProcessor
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors

class TestDataFromCsvConverter(val classLoader: ClassLoader) : TestStepValueConverter, FunctionValueProcessor {

    companion object {
        private const val ROW_KEYWORD = "row"
        private const val CSV_KEYWORD = "csv"
        private const val FILE_KEYWORD = "file"
    }

    override fun execute(name: String, args: Array<Any?>): Any? {
        if (args.size == 1) {
            var newTestData = args[0]
            if (newTestData is Map<*, *>) {
                if (newTestData.containsKey(CSV_KEYWORD)) {
                    val csv = newTestData[CSV_KEYWORD] as Map<String, Any?>
                    val csvFile = csv[FILE_KEYWORD] as String?
                    newTestData = readCsvFile(csvFile, getRowFilter(csv))
                }
            } else if (newTestData is String) {
                val csvFile = newTestData
                newTestData = readCsvFile(csvFile, getRowFilter(null))
            }
            return newTestData
        } else {
            throw TestDataConversionException("Function `csv` can only accept 1 argument. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "fromCsv"

    override fun convert(value: Any?): Any? {
        var newTestData = value
        if (newTestData is Map<*, *>) {
            if (newTestData.containsKey(CSV_KEYWORD)) {
                val csv = newTestData[CSV_KEYWORD] as Map<String, Any?>
                val csvFile = csv[FILE_KEYWORD] as String?
                newTestData = readCsvFile(csvFile, getRowFilter(csv))
            }
        }
        return newTestData
    }

    private fun getRowFilter(csvDetails: Map<String, Any?>?): Predicate<Map<String, String>> {
        return if (csvDetails?.containsKey(ROW_KEYWORD) == true) {
            val row = csvDetails[ROW_KEYWORD] as Map<String, Any?>
            Predicate { item: Map<String, String> ->
                row.entries.all { item.containsKey(it.key) && item[it.key] == it.value }
            }
        } else {
            Predicate { true }
        }
    }

    private fun readCsvFile(fileName: String?, rowFilter: Predicate<Map<String, String>>): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()
        val pathToFile = getPathToFile(fileName)
        if (Files.exists(pathToFile)) {
            readCsvContainer(pathToFile)
                    .let { csvContainer ->
                        csvContainer?.rows?.forEach(Consumer { csvRow: CsvRow ->
                            val row = csvRow.fieldMap.entries.stream()
                                    .collect(Collectors.toMap<Map.Entry<String, String>, String, String>(
                                            { entry: Map.Entry<String, String> -> entry.key.trim { it <= ' ' } },
                                            { entry: Map.Entry<String, String> -> entry.value.trim { it <= ' ' } }))
                            if (rowFilter.test(row)) {
                                result.add(row)
                            }
                        })
                    }
        } else {
            throw TestDataConversionException(String.format("File %s does not exist", fileName))
        }
        return result
    }

    private fun getPathToFile(fileName: String?): Path {
        val url = classLoader.getResource(fileName)
        return if (url == null) {
            throw TestDataConversionException(String.format("File '%s' was not found in resources", fileName))
        } else {
            try {
                Paths.get(url.toURI())
            } catch (e: URISyntaxException) {
                throw TestDataConversionException(String.format("Failed to get path to '%s'", fileName), e)
            }
        }
    }

    private fun readCsvContainer(pathToFile: Path): CsvContainer? {
        return try {
            val csvReader = CsvReader()
            csvReader.setContainsHeader(true)
            csvReader.read(pathToFile, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}