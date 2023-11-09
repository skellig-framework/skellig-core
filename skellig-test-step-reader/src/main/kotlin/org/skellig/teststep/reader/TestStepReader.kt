package org.skellig.teststep.reader

import java.io.InputStream

interface TestStepReader {
    fun read(inputStream: InputStream): List<Map<Any, Any?>>
}