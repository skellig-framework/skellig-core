package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.math.NumberUtils
import java.util.*
import javax.script.ScriptEngineManager

class TestDataFromIfStatementConverter : TestStepValueConverter {

    companion object {
        private const val IF_KEYWORD = "if"
        private const val CONDITION_KEYWORD = "condition"
        private const val THEN_KEYWORD = "then"
        private const val ELSE_KEYWORD = "else"
    }

    private val engine = ScriptEngineManager().getEngineByName("JavaScript")

    override fun convert(value: Any?): Any? =
            when (value) {
                is Map<*, *> ->
                    if (value.containsKey(IF_KEYWORD)) {
                        val ifDetails = value[IF_KEYWORD] as Map<String, Any>
                        val condition = ifDetails[CONDITION_KEYWORD] as String?
                        val thenContent = ifDetails[THEN_KEYWORD]
                        val elseContent = ifDetails[ELSE_KEYWORD] ?: ""

                        Objects.requireNonNull(condition, "'condition' is mandatory in 'if' statement")
                        Objects.requireNonNull(thenContent, "'then' is mandatory in 'if' statement")

                        if (isConditionSatisfied(condition)) convert(thenContent)
                        else convert(elseContent)
                    } else value.entries.map { it.key to convert(it.value) }.toMap()

                is Collection<*> -> value.map { convert(it) }.toList()
                else -> value
            }

    private fun isConditionSatisfied(condition: String?): Boolean {
        val newCondition = encloseStringWithQuotes(condition)
        return try {
            engine.eval(newCondition.toString()) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    private fun encloseStringWithQuotes(condition: String?): StringBuilder {
        val newCondition = StringBuilder()
        var i = 0
        while (i < condition!!.length) {
            var c = condition[i]
            if (isSpecialSymbol(c)) {
                newCondition.append(c)
            } else {
                var value = ""
                while (i < condition.length) {
                    c = condition[i]
                    value += if (!isSpecialSymbol(c)) {
                        c
                    } else {
                        i--
                        break
                    }
                    i++
                }
                value = value.trim { it <= ' ' }
                newCondition.append(if (value == "" || NumberUtils.isNumber(value)) value else "'$value'")
            }
            i++
        }
        return newCondition
    }

    private fun isSpecialSymbol(c: Char): Boolean {
        return c == '(' || c == ')' || c == '<' || c == '=' || c == '>' || c == '&' || c == '|'
    }
}