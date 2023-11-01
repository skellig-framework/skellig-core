package org.skellig.teststep.reader.sts

import java.io.InputStream

class StsFileParser {

    fun parse(inputStream: InputStream): List<Map<String, Any?>> {
        val inputAsString = inputStream.bufferedReader().use { it.readText() }
       return SkelligTestStepParser().parse(inputAsString)
        /* val rawTestSteps: MutableList<Map<String, Any?>> = ArrayList()
        try {
            StsFileBufferedReader(inputStream).use { reader ->
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
        }*/
    }
}