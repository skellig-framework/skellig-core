package org.skellig.teststep.reader.sts

import org.skellig.teststep.reader.TestStepReader
import java.io.InputStream
import java.nio.file.Path

class StsTestStepReader : TestStepReader {

    private var parser: StsFileParser = StsFileParser()

    override fun read(inputStream: InputStream): List<Map<String, Any?>> {
        return parser.parse(inputStream)
    }
}