package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import java.math.BigDecimal


internal class CalcFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, args: Array<Any?>): Any {
        return if (args.size == 1) MathExpressionParser(args[0].toString()).parseExpression()
        else throw FunctionValueExecutionException("Failed to execute function 'calc' as it must have 1 String argument but has ${args.size}")
    }

    override fun getFunctionName(): String = "calc"

    private inner class MathExpressionParser(private var input: String) {
        private var index = 0

        init {
            this.input = input.replace("\\s".toRegex(), "")
        }

        fun parseExpression(): BigDecimal {
            var result = parseTerm()
            while (true) {
                if (peek() == '+') {
                    consume()
                    result += parseTerm()
                } else if (peek() == '-') {
                    consume()
                    result -= parseTerm()
                } else {
                    return result
                }
            }
        }

        private fun peek(): Char {
            return if (index < input.length) input[index] else ' '
        }

        private fun consume() {
            index++
        }

        private fun parseNumber(): BigDecimal {
            val start = index
            while (Character.isDigit(peek()) || peek() == '.') consume()
            return if (start < index && index <= input.length) input.substring(start, index).toBigDecimal()
            else throw FunctionValueExecutionException("Failed to parse math expression '$input'")
        }

        private fun parseTerm(): BigDecimal {
            var result = parseFactor()
            while (true) {
                if (peek() == '*') {
                    consume()
                    result *= parseFactor()
                } else if (peek() == '/') {
                    consume()
                    result /= parseFactor()
                } else {
                    return result
                }
            }
        }

        private fun parseFactor(): BigDecimal {
            if (peek() == '-') {
                consume()
                return -parseFactor()
            }
            if (peek() == '(') {
                consume()
                val result: BigDecimal = parseExpression()
                if (peek() == ')') {
                    consume()
                    return result
                }
            }
            return parseNumber()
        }
    }

}