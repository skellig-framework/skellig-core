package org.skellig.teststep.reader

import org.skellig.teststep.reader.value.expression.ValueExpression
import java.io.InputStream

/**
 * Interface for reading test steps from a file content.
 */
interface TestStepReader {

    /**
     * Read file of test steps and return a list of them, where each test step represents a raw format
     * [Map] where key (or property) and value are [ValueExpression] which may be, for example:
     * 1) simple value
     * 2) math or boolean expressions
     * 3) Map or List collections (ex. wrappers [MapValueExpression][org.skellig.teststep.reader.value.expression.MapValueExpression]
     * or [ListValueExpression][org.skellig.teststep.reader.value.expression.ListValueExpression]).
     *
     * These keys and values of the raw format can be evaluated later using [ValueExpressionContext][org.skellig.teststep.reader.value.expression.ValueExpressionContext]
     * to get a real values.
     */
    fun read(inputStream: InputStream): List<Map<ValueExpression, ValueExpression?>>
}