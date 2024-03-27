package org.skellig.teststep.processing.model

import org.skellig.teststep.reader.value.expression.ValueExpression


class TaskTestStep(
    override val name: String,
    val task: ValueExpression?,
    val parameters: Map<String, Any?>?,
    val convertValueDelegate: (ValueExpression?, Map<String, Any?>) -> Any?
) : TestStep {

    override fun toString(): String {
        return name
    }
}