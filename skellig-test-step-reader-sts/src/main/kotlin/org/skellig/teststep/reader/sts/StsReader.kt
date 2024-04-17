package org.skellig.teststep.reader.sts

import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.reader.value.expression.ValueExpression
import java.io.InputStream


/**
 * Implementation of the [TestStepReader] interface that reads test steps from a file in .sts format.
 * The STS (Skellig Test Step) format uses a custom grammar and parser to parse the file content and
 * convert it into a list of raw test steps represented as [Map] of [ValueExpression] objects.
 *
 * @see TestStepReader
 */
class StsReader : TestStepReader {

    private var parser = StsParser()

    override fun read(inputStream: InputStream): List<Map<ValueExpression, ValueExpression?>> {
        return inputStream.bufferedReader().use { parser.parse(it.readText()) }
    }
}