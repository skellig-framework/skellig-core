package org.skellig.teststep.processing.value.function

import org.apache.commons.lang3.math.NumberUtils
import java.util.*
import javax.script.ScriptEngineManager

class IfFunctionExecutor : FunctionValueExecutor {

    private val engine = ScriptEngineManager().getEngineByName("graal.js")

    override fun execute(name: String, args: Array<Any?>): Any? {

        val condition = args[0] as String?
        val thenValue = args[1]
        val elseValue = if (args.size == 3) args[2] else null

        Objects.requireNonNull(condition, "'condition' is mandatory in 'if' statement")
        Objects.requireNonNull(thenValue, "'then' is mandatory in 'if' statement")

        return if (isConditionSatisfied(condition)) thenValue else elseValue
    }

    override fun getFunctionName(): String = "if"


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
                newCondition.append(if (value == "" || NumberUtils.isCreatable(value)) value else "'$value'")
            }
            i++
        }
        return newCondition
    }

    private fun isSpecialSymbol(c: Char): Boolean {
        return c == '(' || c == ')' || c == '<' || c == '=' || c == '>' || c == '&' || c == '|'
    }
}