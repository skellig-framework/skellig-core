package org.skellig.teststep.reader

import org.skellig.teststep.reader.value.expression.ValueExpression
import java.io.InputStream

interface TestStepReader {

    /**
     * Read file of test steps and return a list of them,
     * where each test step represents a raw format (Map) where key (or property)
     * and value are [ValueExpression] which may be, for example:
     *
     *    1) simple value
     *    2) math or boolean expressions
     *    3) Map or List collections (ex. wrappers [MapValueExpression] or [ListValueExpression])
     */
    fun read(inputStream: InputStream): List<Map<ValueExpression, ValueExpression?>>
}