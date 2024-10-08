package org.skellig.teststep.processing.value.function

import de.siegmar.fastcsv.reader.CsvContainer
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.CsvRow
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * This class is responsible for executing the 'fromCsv' function which reads a CSV file and returns a [List] of [Map] - csv records.
 *
 * Supported args:
 * - fromCsv(`<path>`) - for fromCsv: fromFile(/tmp/file.csv) will read a file 'file.csv' and returns all records as [List] of [Map].
 * - fromCsv(`<path>`, `<filter>`) - same as above but also applies a filter for each row. The `<filter>` is a [Map] which defines
 * key-value pairs where key is column name and value is an expected value from a csv record of this column.
 *
 */
class FromCsvFunctionExecutor(val classLoader: ClassLoader) : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        if (value != null) throw FunctionExecutionException("Function `${getFunctionName()}` cannot be called from another value")
        return if (args.isNotEmpty()) {
            val csvFile = args[0] as String
            val filter = if (args.size == 2 && args[1] is Map<*, *>) args[1] as Map<String, Any?> else null
            readCsvFile(csvFile, getRowFilter(filter))
        } else throw FunctionExecutionException("Function `${getFunctionName()}` can only accept 1 or 2 arguments. Found ${args.size}")
    }

    private fun getRowFilter(rowFilter: Map<String, Any?>?): Predicate<Map<String, String>> {
        return if (rowFilter != null) {
            Predicate { item: Map<String, String> ->
                rowFilter.entries.all { item.containsKey(it.key) && item[it.key] == it.value }
            }
        } else Predicate { true }
    }

    private fun readCsvFile(fileName: String?, rowFilter: Predicate<Map<String, String>>): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()
        val pathToFile = getPathToFile(fileName)
        readCsvContainer(pathToFile)
            .let { csvContainer ->
                csvContainer?.rows?.forEach(Consumer { csvRow: CsvRow ->
                    val row = csvRow.fieldMap.entries.stream()
                        .collect(
                            Collectors.toMap<Map.Entry<String, String>, String, String>(
                                { entry: Map.Entry<String, String> -> entry.key.trim { it <= ' ' } },
                                { entry: Map.Entry<String, String> -> entry.value.trim { it <= ' ' } })
                        )
                    if (rowFilter.test(row)) {
                        result.add(row)
                    }
                })
            }
        return result
    }

    private fun getPathToFile(fileName: String?): Path {
        val url = classLoader.getResource(fileName)
        return if (url == null) {
            throw FunctionExecutionException(String.format("File '%s' was not found in resources", fileName))
        } else {
            try {
                Paths.get(url.toURI())
            } catch (e: URISyntaxException) {
                throw FunctionExecutionException(String.format("Failed to get path to '%s'", fileName), e)
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

    override fun getFunctionName(): String = "fromCsv"
}