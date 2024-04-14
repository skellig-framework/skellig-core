package org.skellig.teststep.reader.value.expression

import java.util.regex.Pattern


/**
 * A class that represents a value expression based on a regular expression pattern.
 *
 * @property regex The regular expression pattern.
 */
class PatternValueExpression(regex: String) : ValueExpression {

    val pattern: Pattern = Pattern.compile(regex)

    override fun evaluate(context: ValueExpressionContext): Any {
        return context.value?.let { pattern.matcher(it.toString()).matches() } ?: false
    }

}