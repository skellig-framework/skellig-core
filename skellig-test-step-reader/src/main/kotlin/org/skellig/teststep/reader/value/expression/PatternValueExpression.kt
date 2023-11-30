package org.skellig.teststep.reader.value.expression

import java.util.regex.Pattern

class PatternValueExpression(value: String) : ValueExpression {

    val pattern: Pattern = Pattern.compile(value)

    override fun evaluate(context: ValueExpressionContext): Any {
        return context.value?.let { pattern.matcher(it.toString()).matches() } ?: false
    }

}