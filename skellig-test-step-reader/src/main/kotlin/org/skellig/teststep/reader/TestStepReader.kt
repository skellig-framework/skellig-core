package org.skellig.teststep.reader

import java.nio.file.Path

interface TestStepReader {
    fun read(fileName: Path): List<Map<String, Any?>>
}