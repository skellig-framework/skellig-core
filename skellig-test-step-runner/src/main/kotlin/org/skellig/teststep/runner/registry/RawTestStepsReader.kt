package org.skellig.teststep.runner.registry

import org.skellig.teststep.reader.value.expression.ValueExpression
import java.net.URI

internal interface RawTestStepsReader {
    fun getTestStepsFromUri(rootUri: URI): Collection<Map<ValueExpression, ValueExpression?>>
}