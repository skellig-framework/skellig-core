package org.skellig.teststep.reader.sts

import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.reader.sts.value.expression.ValueExpression
import java.io.InputStream

class StsReader : TestStepReader {

    private var parser = StsParser()

    override fun read(inputStream: InputStream): List<Map<Any, Any?>> {
        return inputStream.bufferedReader().use { parser.parse(it.readText()) }
    }
}