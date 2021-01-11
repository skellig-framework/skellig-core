package org.skellig.teststep.reader.sts

import org.skellig.teststep.reader.exception.TestStepReadException
import java.nio.file.Files
import java.nio.file.Path

class StsFileParser {

    fun parse(filePath: Path?): List<Map<String, Any?>> {
        val rawTestSteps: MutableList<Map<String, Any?>> = ArrayList()
        try {
            StsFileBufferedReader(Files.newBufferedReader(filePath)).use { reader ->
                RawTestStepHandler().use { rawTestStepHandler ->
                    var character: Char
                    while (reader.read().also { character = it.toChar() } > 0) {
                        rawTestStepHandler.handle(character, reader, rawTestSteps)
                    }
                    return rawTestSteps
                }
            }
        } catch (e: Exception) {
            throw TestStepReadException(e)
        }
    }
}