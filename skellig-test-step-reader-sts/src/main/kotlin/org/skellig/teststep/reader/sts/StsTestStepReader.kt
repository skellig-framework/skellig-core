package org.skellig.teststep.reader.sts

import org.skellig.teststep.reader.TestStepReader
import java.nio.file.Path

class StsTestStepReader : TestStepReader {

    private var parser: StsFileParser = StsFileParser()

    override fun read(fileName: Path): List<Map<String, Any?>> {
        return parser.parse(fileName)
    }
}